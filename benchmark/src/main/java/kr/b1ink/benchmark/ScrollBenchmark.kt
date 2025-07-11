package kr.b1ink.benchmark

import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMetricApi::class)
class ScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    @SdkSuppress(minSdkVersion = 24)
    fun noCompilation() = scroll(CompilationMode.None())

    @Test
    fun defaultCompilation() = scroll(CompilationMode.DEFAULT)

    @Test
    fun full() = scroll(CompilationMode.Full())

    private fun scroll(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(
                TraceSectionMetric("ClickTrace"),
                StartupTimingMetric(),
                FrameTimingMetric()
            ),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = DEFAULT_ITERATIONS,
            setupBlock = {
//                if (firstStart) {
//                    val intent = Intent("$packageName.SCROLL_VIEW_ACTIVITY")

                pressHome()
                startActivityAndWait()
//                    firstStart = false
//                }
            }
        ) {
//            device.wait(Until.hasObject(By.scrollable(true)), 5_000)
//
//            val scrollableObject = device.findObject(By.scrollable(true))
//            if (scrollableObject == null) {
//                TestCase.fail("No scrollable view found in hierarchy")
//            }
//            scrollableObject.setGestureMargin(device.displayWidth / 10)
//            scrollableObject?.apply {
//                repeat(2) {
//                    fling(Direction.DOWN)
//                }
//                repeat(2) {
//                    fling(Direction.UP)
//                }
//            }

            with(device) {
                wait(Until.hasObject(By.res("MainScreenContents")), 5_000)
                val mainContents = findObject(By.res("MainScreenContents"))
                if (mainContents == null) {
                    TestCase.fail("Not found MainScreenContents")
                }

                mainContents.setGestureMargin(device.displayWidth / 5)
//                repeat(1) {
//                    mainContents.fling(Direction.DOWN)
//                }
//                mainContents.fling(Direction.DOWN)
//                waitForIdle()
//                mainContents.fling(Direction.UP)
//                waitForIdle()

                mainContents.scroll(Direction.DOWN, 0.5f)
                waitForIdle()

                val mainContentsItem = mainContents.findObject(By.res("MainScreenContents:Item2"))
                if (mainContentsItem == null) {
                    TestCase.fail("Not found MainScreenContents:Item")
                }
                mainContentsItem.click()
                waitForIdle(5_000)

                wait(Until.hasObject(By.res("ListScreenContents")), 5_000)
                val listContents = findObject(By.res("ListScreenContents"))
                if (listContents == null) {
                    TestCase.fail("Not found ListScreenContents")
                }
                listContents.scroll(Direction.DOWN, 1f)
//                waitForIdle()
                listContents.scroll(Direction.DOWN, 1f)
//                waitForIdle()
                listContents.scroll(Direction.DOWN, 1f)
//                waitForIdle()
                listContents.scroll(Direction.DOWN, 1f)
//                waitForIdle()
                listContents.scroll(Direction.DOWN, 1f)
                listContents.scroll(Direction.UP, 1f)
                listContents.scroll(Direction.UP, 1f)
//                waitForIdle()
//                listContents.setGestureMargin(device.displayHeight / 5)
//                repeat(5) {
//                    listContents.fling(Direction.DOWN)
//                }
//                repeat(3) {
//                    listContents.fling(Direction.UP)
//                }
                waitForIdle()
                listContents.click()
                waitForIdle()

//                wait(Until.hasObject(By.res("ListScreenContents:Item")), 5_000)
//                val listContentsItem = listContents.findObject(By.res("ListScreenContents:Item"))
//                if (listContentsItem == null) {
//                    TestCase.fail("Not found ListScreenContents:Item")
//                }
//                listContentsItem.click()
//                waitForIdle()

                wait(Until.hasObject(By.res("DetailScreenContents")), 5_000)
                val detailContents = findObject(By.res("DetailScreenContents"))
                if (detailContents == null) {
                    TestCase.fail("Not found DetailScreenContents")
                }
                detailContents.setGestureMargin(device.displayHeight / 5)
                detailContents.scroll(Direction.DOWN, 1f)
//                waitForIdle()
                detailContents.scroll(Direction.DOWN, 1f)
//                waitForIdle()
                detailContents.scroll(Direction.DOWN, 1f)
//                waitForIdle()
                detailContents.scroll(Direction.DOWN, 1f)
                detailContents.scroll(Direction.UP, 1f)
                detailContents.scroll(Direction.UP, 1f)
                waitForIdle()

//                repeat(5) {
//                    detailContents.fling(Direction.DOWN)
//                }
//                repeat(3) {
//                    detailContents.fling(Direction.UP)
//                }
//                waitForIdle()

//                killProcess()
            }
        }
    }
}