package moe.manooch.najib4x.proxy.openvpn;

import androidx.annotation.NonNull;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

import org.jetbrains.annotations.NotNull;

import io.najishab.najibox.fmt.AbstractBean;
import io.najishab.najibox.fmt.KryoConverters;

public class OpenVpnBean extends AbstractBean {

    public static final String PROTOCOL_UDP = "udp";
    public static final String PROTOCOL_TCP = "tcp";

    public static final Creator<OpenVpnBean> CREATOR = new CREATOR<OpenVpnBean>() {
        @NonNull
        @Override
        public OpenVpnBean newInstance() {
            return new OpenVpnBean();
        }

        @Override
        public OpenVpnBean[] newArray(int size) {
            return new OpenVpnBean[size];
        }
    };

    // Transport
    public String protocol; // "udp" | "tcp"

    // Certificates / keys, stored inline as PEM text (same way .ovpn embeds them)
    public String caCertificate;
    public String clientCertificate;
    public String clientKey;

    // Extra HMAC / obfuscation layer (mutually exclusive in practice, both kept for flexibility)
    public String tlsAuthKey;
    public String tlsCryptKey;
    public String tlsCryptV2Key;
    public String keyDirection; // "0", "1", or "" (unspecified)

    // auth-user-pass
    public String username;
    public String password;

    // Crypto negotiation
    public String cipher;   // e.g. AES-256-GCM
    public String auth;     // e.g. SHA256
    public String compress; // e.g. lzo, empty = disabled

    // Full original .ovpn content, kept verbatim for directives not modeled above,
    // troubleshooting, and re-export.
    public String rawConfig;

    @Override
    public void initializeDefaultValues() {
        super.initializeDefaultValues();
        if (protocol == null) protocol = PROTOCOL_UDP;
        if (caCertificate == null) caCertificate = "";
        if (clientCertificate == null) clientCertificate = "";
        if (clientKey == null) clientKey = "";
        if (tlsAuthKey == null) tlsAuthKey = "";
        if (tlsCryptKey == null) tlsCryptKey = "";
        if (tlsCryptV2Key == null) tlsCryptV2Key = "";
        if (keyDirection == null) keyDirection = "";
        if (username == null) username = "";
        if (password == null) password = "";
        if (cipher == null) cipher = "";
        if (auth == null) auth = "";
        if (compress == null) compress = "";
        if (rawConfig == null) rawConfig = "";
    }

    @Override
    public void serialize(ByteBufferOutput output) {
        output.writeInt(0);
        super.serialize(output);
        output.writeString(protocol);
        output.writeString(caCertificate);
        output.writeString(clientCertificate);
        output.writeString(clientKey);
        output.writeString(tlsAuthKey);
        output.writeString(tlsCryptKey);
        output.writeString(tlsCryptV2Key);
        output.writeString(keyDirection);
        output.writeString(username);
        output.writeString(password);
        output.writeString(cipher);
        output.writeString(auth);
        output.writeString(compress);
        output.writeString(rawConfig);
    }

    @Override
    public void deserialize(ByteBufferInput input) {
        int version = input.readInt();
        super.deserialize(input);
        protocol = input.readString();
        caCertificate = input.readString();
        clientCertificate = input.readString();
        clientKey = input.readString();
        tlsAuthKey = input.readString();
        tlsCryptKey = input.readString();
        tlsCryptV2Key = input.readString();
        keyDirection = input.readString();
        username = input.readString();
        password = input.readString();
        cipher = input.readString();
        auth = input.readString();
        compress = input.readString();
        rawConfig = input.readString();
    }

    @NotNull
    @Override
    public OpenVpnBean clone() {
        return KryoConverters.deserialize(new OpenVpnBean(), KryoConverters.serialize(this));
    }
}
