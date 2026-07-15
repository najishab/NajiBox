package moe.manooch.najib4x.proxy.openvpn

import io.najishab.najibox.ktx.applyDefaultValues
import moe.manooch.najib4x.SingBoxOptions.CustomSingBoxOption
import moe.manooch.najib4x.SingBoxOptions.SingBoxOption
import moe.manooch.najib4x.utils.JavaUtil.gson

// Builds a raw "openvpn" outbound JSON object matching the schema of
// option.OpenVPNOutboundOptions (ported into najishab/sing-box, see APPLY_INSTRUCTIONS.md),
// then wraps it via the existing CustomSingBoxOption passthrough (same mechanism ConfigBean
// uses), since OpenVPN has no dedicated generated Outbound_*Options Kotlin class.
fun buildSingBoxOutboundOpenVpnBean(bean: OpenVpnBean): SingBoxOption {
    val obj = LinkedHashMap<String, Any?>()
    obj["type"] = "openvpn"
    obj["servers"] = listOf(
        linkedMapOf(
            "server" to bean.serverAddress,
            "server_port" to bean.serverPort,
        )
    )
    if (bean.protocol.isNotBlank()) obj["proto"] = bean.protocol
    if (bean.cipher.isNotBlank()) obj["cipher"] = bean.cipher
    if (bean.auth.isNotBlank()) obj["auth"] = bean.auth
    if (bean.username.isNotBlank()) obj["username"] = bean.username
    if (bean.password.isNotBlank()) obj["password"] = bean.password

    if (bean.tlsAuthKey.isNotBlank()) {
        obj["tls_auth"] = bean.tlsAuthKey
    }
    if (bean.tlsCryptKey.isNotBlank()) {
        obj["tls_crypt"] = bean.tlsCryptKey
    } else if (bean.tlsCryptV2Key.isNotBlank()) {
        // tls-crypt-v2 uses the same "tls_crypt" field, with tls_crypt_v2 as a mode flag
        obj["tls_crypt"] = bean.tlsCryptV2Key
        obj["tls_crypt_v2"] = true
    }
    bean.keyDirection.toIntOrNull()?.let { obj["key_direction"] = it }

    val tls = LinkedHashMap<String, Any?>()
    if (bean.caCertificate.isNotBlank()) tls["ca"] = bean.caCertificate
    if (bean.clientCertificate.isNotBlank()) tls["certificate"] = bean.clientCertificate
    if (bean.clientKey.isNotBlank()) tls["key"] = bean.clientKey
    if (tls.isNotEmpty()) obj["tls"] = tls

    return CustomSingBoxOption(gson.toJson(obj))
}

// Parses a standard .ovpn (OpenVPN client config) file into an OpenVpnBean.
// Inline <ca>/<cert>/<key>/<tls-auth>/<tls-crypt>/<tls-crypt-v2>/<auth-user-pass> blocks are
// supported. References to external files (e.g. "ca ca.crt") are not resolved, since we only
// have the text of the single imported file; those fields are simply left blank and the full
// original text is kept in rawConfig for troubleshooting / re-export.
fun parseOpenVpn(text: String): OpenVpnBean {
    val bean = OpenVpnBean().applyDefaultValues()
    bean.rawConfig = text

    fun extractBlock(tag: String): String? {
        val regex = Regex("<$tag>([\\s\\S]*?)</$tag>", RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    extractBlock("ca")?.let { bean.caCertificate = it }
    extractBlock("cert")?.let { bean.clientCertificate = it }
    extractBlock("key")?.let { bean.clientKey = it }
    extractBlock("tls-auth")?.let { bean.tlsAuthKey = it }
    extractBlock("tls-crypt")?.let { bean.tlsCryptKey = it }
    extractBlock("tls-crypt-v2")?.let { bean.tlsCryptV2Key = it }
    extractBlock("auth-user-pass")?.let { block ->
        val lines = block.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isNotEmpty()) bean.username = lines[0]
        if (lines.size > 1) bean.password = lines[1]
    }

    // Strip inline blocks before line-based directive scanning, so their content
    // (which may itself contain lines that look like directives) isn't misparsed.
    val withoutBlocks = text.replace(Regex("<[a-zA-Z0-9-]+>[\\s\\S]*?</[a-zA-Z0-9-]+>"), "")

    var remoteFound = false
    for (rawLine in withoutBlocks.lines()) {
        val line = rawLine.trim()
        if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) continue
        val parts = line.split(Regex("\\s+"))
        when (parts[0].lowercase()) {
            "remote" -> if (!remoteFound && parts.size >= 2) {
                bean.serverAddress = parts[1]
                bean.serverPort = parts.getOrNull(2)?.toIntOrNull() ?: 1194
                if (parts.size >= 4) {
                    bean.protocol = if (parts[3].lowercase().startsWith("tcp")) {
                        OpenVpnBean.PROTOCOL_TCP
                    } else {
                        OpenVpnBean.PROTOCOL_UDP
                    }
                }
                remoteFound = true
            }

            "proto" -> if (parts.size >= 2) {
                bean.protocol = if (parts[1].lowercase().startsWith("tcp")) {
                    OpenVpnBean.PROTOCOL_TCP
                } else {
                    OpenVpnBean.PROTOCOL_UDP
                }
            }

            "cipher", "data-ciphers" -> if (parts.size >= 2) {
                bean.cipher = parts[1].substringBefore(":")
            }

            "auth" -> if (parts.size >= 2) bean.auth = parts[1]

            "key-direction" -> if (parts.size >= 2) bean.keyDirection = parts[1]
        }
    }

    if (!remoteFound) error("No 'remote' directive found in .ovpn file")
    return bean
}
