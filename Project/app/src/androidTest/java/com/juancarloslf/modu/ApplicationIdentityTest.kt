package com.juancarloslf.modu

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationIdentityTest {
    @Test
    fun applicationId_isTheExpectedValue() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.juancarloslf.modu", appContext.packageName)
    }
}
