package kr.b1ink.benchmark

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A scaffold for creating a baseline profile user journey. Implementing classes can
 * start generating a profile directly by implementing [MacrobenchmarkScope.profileBlock].
 */
@OptIn(ExperimentalBaselineProfilesApi::class)
@RunWith(AndroidJUnit4::class)
abstract class BaselineProfileGeneratorScaffold {

    @get:Rule
    val rule = BaselineProfileRule()

    /**
     * Generate a baseline profile in this function.
     */
    abstract fun MacrobenchmarkScope.profileBlock()

    @Test
    fun profileGenerator() {
        rule.collectBaselineProfile(
            packageName = TARGET_PACKAGE,
        ) {
            profileBlock()
        }
    }
}