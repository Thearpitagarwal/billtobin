package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.InventoryDisplayItem
import com.example.data.model.PackageType
import com.example.ui.screens.ProductInventoryCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        ProductInventoryCard(
          item = InventoryDisplayItem(
            itemId = 1,
            itemName = "Parle-G 50g",
            aliases = "Parle G",
            packageType = PackageType.LOOSE,
            floor = "3",
            row = "A",
            barrack = "3",
            shelf = "B",
            locationCode = "3 A 3 B",
            priority = 1,
            stockQty = 142
          ),
          onCheckedChange = {},
          onCardClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
