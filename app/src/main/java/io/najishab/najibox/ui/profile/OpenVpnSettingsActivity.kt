package io.najishab.najibox.ui.profile

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import io.najishab.najibox.R
import io.najishab.najibox.database.preference.EditTextPreferenceModifiers
import moe.manooch.najib4x.proxy.PreferenceBinding
import moe.manooch.najib4x.proxy.PreferenceBindingManager
import moe.manooch.najib4x.proxy.Type
import moe.manooch.najib4x.proxy.openvpn.OpenVpnBean

class OpenVpnSettingsActivity : ProfileSettingsActivity<OpenVpnBean>() {

    override fun createEntity() = OpenVpnBean()

    private val pbm = PreferenceBindingManager()
    private val name = pbm.add(PreferenceBinding(Type.Text, "name"))
    private val serverAddress = pbm.add(PreferenceBinding(Type.Text, "serverAddress"))
    private val serverPort = pbm.add(PreferenceBinding(Type.TextToInt, "serverPort"))
    private val protocol = pbm.add(PreferenceBinding(Type.Text, "protocol"))
    private val username = pbm.add(PreferenceBinding(Type.Text, "username"))
    private val password = pbm.add(PreferenceBinding(Type.Text, "password"))
    private val caCertificate = pbm.add(PreferenceBinding(Type.Text, "caCertificate"))
    private val clientCertificate = pbm.add(PreferenceBinding(Type.Text, "clientCertificate"))
    private val clientKey = pbm.add(PreferenceBinding(Type.Text, "clientKey"))
    private val tlsAuthKey = pbm.add(PreferenceBinding(Type.Text, "tlsAuthKey"))
    private val tlsCryptKey = pbm.add(PreferenceBinding(Type.Text, "tlsCryptKey"))
    private val tlsCryptV2Key = pbm.add(PreferenceBinding(Type.Text, "tlsCryptV2Key"))
    private val keyDirection = pbm.add(PreferenceBinding(Type.Text, "keyDirection"))
    private val cipher = pbm.add(PreferenceBinding(Type.Text, "cipher"))
    private val auth = pbm.add(PreferenceBinding(Type.Text, "auth"))

    override fun OpenVpnBean.init() {
        pbm.writeToCacheAll(this)
    }

    override fun OpenVpnBean.serialize() {
        pbm.fromCacheAll(this)
    }

    override fun PreferenceFragmentCompat.createPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        addPreferencesFromResource(R.xml.openvpn_preferences)
        pbm.setPreferenceFragment(this)

        (serverPort.preference as EditTextPreference)
            .setOnBindEditTextListener(EditTextPreferenceModifiers.Port)
        (password.preference as EditTextPreference).summaryProvider = PasswordSummaryProvider
    }

}
