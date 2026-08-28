// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  // Update your KSP version to 2.3.6 (matching your specific Kotlin version)
  id("com.google.devtools.ksp") version "2.3.6-1.0.XX" 
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}
