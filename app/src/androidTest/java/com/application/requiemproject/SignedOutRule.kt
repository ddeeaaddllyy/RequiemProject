package com.application.requiemproject

import androidx.test.platform.app.InstrumentationRegistry
import com.application.requiemproject.data.local.SessionManager
import org.junit.rules.ExternalResource

/** Start authentication scenarios independently of a previous device session. */
class SignedOutRule : ExternalResource() {
    override fun before() {
        SessionManager(InstrumentationRegistry.getInstrumentation().targetContext).clearSession()
    }
}
