package moe.manooch.najib4x.proxy.shadowtls

import io.najishab.najibox.fmt.v2ray.buildSingBoxOutboundTLS
import moe.manooch.najib4x.SingBoxOptions

fun buildSingBoxOutboundShadowTLSBean(bean: ShadowTLSBean): SingBoxOptions.Outbound_ShadowTLSOptions {
    return SingBoxOptions.Outbound_ShadowTLSOptions().apply {
        type = "shadowtls"
        server = bean.serverAddress
        server_port = bean.serverPort
        version = bean.version
        password = bean.password
        tls = buildSingBoxOutboundTLS(bean)
    }
}
