package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GeoChronicle", appName)

    val radarTab = context.getString(R.string.nav_radar)
    assertEquals("Radar", radarTab)

    for (cat in PlaceCategory.entries) {
      val str = context.getString(cat.stringRes)
      assertNotEquals("", str)
    }
  }
}
