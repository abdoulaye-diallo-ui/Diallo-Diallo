package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.CryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Samawakhtane", appName)
  }

  @Test
  fun `verify AES-256 E2EE encryption and decryption`() {
    val plainMessage = "Salaam Aleykoum depuis Dakar !"
    val encrypted = CryptoManager.encrypt(plainMessage)
    assertNotEquals(plainMessage, encrypted)

    val decrypted = CryptoManager.decrypt(encrypted)
    assertEquals(plainMessage, decrypted)
  }
}
