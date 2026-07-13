package io.najishab.najibox.fmt.wireguard

import io.najishab.najibox.ktx.*
import moe.manooch.najib4x.SingBoxOptions
import moe.manooch.najib4x.utils.Util
import moe.manooch.najib4x.utils.listByLineOrComma
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.URLDecoder

fun WireGuardBean.toUri(): String {
    val builder = linkBuilder()
        .host(serverAddress)
        .port(serverPort ?: 51820)

    if (privateKey.isNotBlank()) {
        builder.username(privateKey)
    }

    builder.addQueryParameter("publickey", peerPublicKey)
    if (peerPreSharedKey.isNotBlank()) builder.addQueryParameter("presharedkey", peerPreSharedKey)
    if (localAddress.isNotBlank()) builder.addQueryParameter("address", localAddress.replace("\n", ","))
    if (mtu != null && mtu != 1420) builder.addQueryParameter("mtu", mtu.toString())
    if (reserved.isNotBlank()) builder.addQueryParameter("reserved", reserved)

    if (name.isNotBlank()) {
        builder.encodedFragment(name.urlSafe())
    }

    return builder.toLink("wireguard")
}

fun parseWireGuard(link: String): WireGuardBean {
    val url = link.replace("wireguard://", "https://")
        .replace("wg://", "https://")
        .toHttpUrlOrNull()
        ?: error("invalid wireguard link $link")

    return WireGuardBean().apply {
        serverAddress = url.host
        serverPort = url.port
        name = url.fragment?.let { runCatching { URLDecoder.decode(it, "UTF-8") }.getOrDefault(it) }

        if (url.username.isNotBlank()) {
            privateKey = runCatching { URLDecoder.decode(url.username, "UTF-8") }.getOrDefault(url.username)
        }

        fun getParam(vararg names: String): String? = names.firstNotNullOfOrNull { url.queryParameter(it) }

        getParam("publickey", "publicKey", "pk")?.let { peerPublicKey = it }
        getParam("privatekey", "privateKey")?.let { privateKey = it }
        getParam("address", "addr")?.let {
            localAddress = runCatching { URLDecoder.decode(it, "UTF-8") }.getOrDefault(it).replace(",", "\n")
        }
        getParam("presharedkey", "preSharedKey", "psk")?.let { peerPreSharedKey = it }
        getParam("mtu")?.let { mtu = it.toIntOrNull() ?: 1420 }
        getParam("reserved")?.let { reserved = it }

    }.apply { initializeDefaultValues() }
}

fun genReserved(anyStr: String): String {
    try {
        val list = anyStr.listByLineOrComma()
        val ba = ByteArray(3)
        if (list.size == 3) {
            list.forEachIndexed { index, s ->
                val i = s
                    .replace("[", "")
                    .replace("]", "")
                    .replace(" ", "")
                    .toIntOrNull() ?: return anyStr
                ba[index] = i.toByte()
            }
            return Util.b64EncodeOneLine(ba)
        } else {
            return anyStr
        }
    } catch (e: Exception) {
        return anyStr
    }
}

fun buildSingBoxOutboundWireguardBean(bean: WireGuardBean): SingBoxOptions.Outbound_WireGuardOptions {
    return SingBoxOptions.Outbound_WireGuardOptions().apply {
        type = "wireguard"
        server = bean.serverAddress
        server_port = bean.serverPort
        local_address = bean.localAddress.listByLineOrComma()
        private_key = bean.privateKey
        peer_public_key = bean.peerPublicKey
        pre_shared_key = bean.peerPreSharedKey
        mtu = bean.mtu
        if (bean.reserved.isNotBlank()) reserved = genReserved(bean.reserved)
    }
}
