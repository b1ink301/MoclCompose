package kr.b1ink.benchmark

import android.graphics.Point
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import junit.framework.TestCase
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeActivityBaselineProfileGenerator : BaselineProfileGeneratorScaffold() {

    override fun MacrobenchmarkScope.profileBlock() {
        // Start into the Compose Activity
//        startActivityAndWait(
//            Intent()
//                .setAction("$TARGET_PACKAGE")
//        )

        pressHome()
        startActivityAndWait()

        with(device) {
            wait(Until.hasObject(By.res("MainScreenContents")), 10_000)
            val mainContents = findObject(By.res("MainScreenContents"))
            if (mainContents == null) {
                TestCase.fail("Not found MainScreenContents")
            }

            mainContents.setGestureMargin(device.displayWidth / 5)
//            mainContents.scroll(Direction.DOWN, 0.5f)

//            mainContents.drag(Point(10, 100))
//            mainContents.drag(Point(10, -100))
            mainContents.fling(Direction.DOWN)
            mainContents.fling(Direction.UP)
//            mainContents.click()
//            click(400, 200)

            wait(Until.hasObject(By.res("MainScreenContents:Item1")), 5_000)
            val mainContentsItem = mainContents.findObject(By.res("MainScreenContents:Item1"))
            if (mainContentsItem == null) {
                TestCase.fail("Not found MainScreenContents:Item")
            }
            mainContentsItem.click()
//            waitForIdle()

            wait(Until.hasObject(By.res("ListScreenContents")), 10_000)
            val listContents = findObject(By.res("ListScreenContents"))
            if (listContents == null) {
                TestCase.fail("Not found ListScreenContents")
            }

            listContents.setGestureMargin(device.displayWidth / 5)

//            if (listContents.isScrollable) {
//            listContents.drag(Point(10, 100))
//            listContents.drag(Point(10, 100))
//            listContents.drag(Point(10, 100))
//            listContents.drag(Point(10, -100))
//            listContents.drag(Point(10, -100))
//                mainContents.fling(Direction.UP)
//            }

            listContents.fling(Direction.DOWN)
            listContents.fling(Direction.DOWN)
            listContents.fling(Direction.DOWN)
            listContents.fling(Direction.DOWN)
            listContents.fling(Direction.UP)
            listContents.fling(Direction.UP)
//            listContents.scroll(Direction.DOWN, 1f)
//            listContents.scroll(Direction.DOWN, 1f)
//            listContents.scroll(Direction.DOWN, 1f)
//            listContents.scroll(Direction.DOWN, 1f)
//            listContents.scroll(Direction.DOWN, 1f)
//            listContents.scroll(Direction.UP, 1f)
//            listContents.scroll(Direction.UP, 1f)

            listContents.click()
//            click(400, 200)
//            listContents.fling(Direction.DOWN)
//            waitForIdle()

            wait(Until.hasObject(By.res("DetailScreenContents")), 10_000)
            val detailContents = findObject(By.res("DetailScreenContents"))
            if (detailContents == null) {
                TestCase.fail("Not found DetailScreenContents")
            }
            detailContents.setGestureMargin(device.displayWidth / 5)
//            detailContents.drag(Point(10, 100))
//            detailContents.drag(Point(10, 100))
//            detailContents.drag(Point(10, -100))
//                mainContents.fling(Direction.UP)
//            detailContents.scroll(Direction.DOWN, 1f)
//            detailContents.scroll(Direction.DOWN, 1f)
//            detailContents.scroll(Direction.DOWN, 1f)
//            detailContents.scroll(Direction.DOWN, 1f)
//            detailContents.scroll(Direction.UP, 1f)
//            detailContents.scroll(Direction.UP, 1f)
//            detailContents.setGestureMargin(device.displayWidth / 5)
            detailContents.fling(Direction.DOWN)
            detailContents.fling(Direction.DOWN)
            detailContents.fling(Direction.DOWN)
            detailContents.fling(Direction.DOWN)
            detailContents.fling(Direction.UP)
            detailContents.fling(Direction.UP)
        }
    }
}