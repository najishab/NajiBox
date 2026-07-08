package io.najishab.najibox.ui.profile

import io.najishab.najibox.fmt.http.HttpBean

class HttpSettingsActivity : StandardV2RaySettingsActivity() {

    override fun createEntity() = HttpBean()

}
