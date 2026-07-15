package moe.manooch.najib4x.proxy.openvpn

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
