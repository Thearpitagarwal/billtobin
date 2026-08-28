package com.example

import com.example.data.model.ItemEntity
import com.example.data.model.ItemLocationEntity
import com.example.data.model.ItemWithLocations
import com.example.data.model.PackageType
import com.example.ocr.LevenshteinMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testLevenshteinDistanceCalculation() {
    assertEquals(0, LevenshteinMatcher.calculateDistance("Parle-G", "Parle-G"))
    assertEquals(1, LevenshteinMatcher.calculateDistance("Parle-G", "ParleG"))
    assertEquals(2, LevenshteinMatcher.calculateDistance("Detergent", "Dtergnt"))
  }

  @Test
  fun testPackageTypeExtraction() {
    assertEquals(PackageType.CARTON, LevenshteinMatcher.extractPackageType("Detergent 1kg CTN"))
    assertEquals(PackageType.CARTON, LevenshteinMatcher.extractPackageType("Bleach Cartons BOX"))
    assertEquals(PackageType.CARTON, LevenshteinMatcher.extractPackageType("Soda Can CS"))
    assertEquals(PackageType.LOOSE, LevenshteinMatcher.extractPackageType("Parle-G 50g 2x"))
    assertEquals(PackageType.LOOSE, LevenshteinMatcher.extractPackageType("Apple iPhone 15"))
  }

  @Test
  fun testCleanProductName() {
    val raw1 = "1. Parle-G 50g 2x $12.99"
    val clean1 = LevenshteinMatcher.cleanProductName(raw1)
    assertEquals("Parle-G 50g", clean1)

    val raw2 = "Detergent 1kg CTN 45.00"
    val clean2 = LevenshteinMatcher.cleanProductName(raw2)
    assertEquals("Detergent 1kg", clean2)
  }

  @Test
  fun testFuzzyMatching() {
    val items = listOf(
      ItemWithLocations(
        item = ItemEntity(itemId = 1, itemName = "Parle-G 50g", aliases = "Parle G, ParleG"),
        locations = listOf(
          ItemLocationEntity(
            locationId = 1,
            itemId = 1,
            packageType = PackageType.LOOSE,
            floor = "1",
            row = "A",
            barrack = "01",
            shelf = "A",
            locationCode = "1 A 01 A"
          )
        )
      ),
      ItemWithLocations(
        item = ItemEntity(itemId = 2, itemName = "Detergent Powder 1kg", aliases = "Surf, Detergent"),
        locations = listOf(
          ItemLocationEntity(
            locationId = 2,
            itemId = 2,
            packageType = PackageType.CARTON,
            floor = "2",
            row = "B",
            barrack = "04",
            shelf = "C",
            locationCode = "2 B 04 C"
          )
        )
      )
    )

    val match = LevenshteinMatcher.findBestMatch("ParleG 50g", items)
    assertNotNull(match)
    assertEquals(1L, match?.item?.item?.itemId)
  }
}
