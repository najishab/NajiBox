package io.najishab.najibox.bg.proto

import io.najishab.najibox.database.DataStore
import io.najishab.najibox.database.ProxyEntity

class UrlTest {

    val link = DataStore.connectionTestURL
    private val timeout = 5000

    suspend fun doTest(profile: ProxyEntity): Int {
        return TestInstance(profile, link, timeout).doTest()
    }

}