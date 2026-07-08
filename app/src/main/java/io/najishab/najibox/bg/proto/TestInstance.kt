package io.najishab.najibox.bg.proto

import io.najishab.najibox.BuildConfig
import io.najishab.najibox.bg.GuardedProcessPool
import io.najishab.najibox.database.ProxyEntity
import io.najishab.najibox.fmt.buildConfig
import io.najishab.najibox.ktx.Logs
import io.najishab.najibox.ktx.runOnDefaultDispatcher
import io.najishab.najibox.ktx.tryResume
import io.najishab.najibox.ktx.tryResumeWithException
import kotlinx.coroutines.delay
import libcore.Libcore
import moe.manooch.najib4x.net.LocalResolverImpl
import kotlin.coroutines.suspendCoroutine

class TestInstance(profile: ProxyEntity, val link: String, private val timeout: Int) :
    BoxInstance(profile) {

    suspend fun doTest(): Int {
        return suspendCoroutine { c ->
            processes = GuardedProcessPool {
                Logs.w(it)
                c.tryResumeWithException(it)
            }
            runOnDefaultDispatcher {
                use {
                    try {
                        init()
                        launch()
                        if (processes.processCount > 0) {
                            // wait for plugin start
                            delay(500)
                        }
                        c.tryResume(Libcore.urlTest(box, link, timeout))
                    } catch (e: Exception) {
                        c.tryResumeWithException(e)
                    }
                }
            }
        }
    }

    override fun buildConfig() {
        config = buildConfig(profile, true)
    }

    override suspend fun loadConfig() {
        // don't call destroyAllJsi here
        if (BuildConfig.DEBUG) Logs.d(config.config)
        box = Libcore.newSingBoxInstance(config.config, LocalResolverImpl)
    }

}
