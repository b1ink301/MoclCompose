//package kr.b1ink.data
//
//import androidx.test.platform.app.InstrumentationRegistry
//import androidx.test.ext.junit.runners.AndroidJUnit4
//
//import org.junit.Test
//import org.junit.runner.RunWith
//
//import org.junit.Assert.*
//
///**
// * Instrumented test, which will execute on an Android device.
// *
// * See [testing documentation](http://d.android.com/tools/testing).
// */
//@RunWith(AndroidJUnit4::class)
//class ExampleInstrumentedTest {
//    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("kr.b1ink.data.test", appContext.packageName)
//    }
//}

//@HiltAndroidTest
//@RunWith(AndroidJUnit4::class)
//class MoclHiltTest {
//    @get:Rule
//    var hiltRule = HiltAndroidRule(this)
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @get:Rule
//    var coroutinesRule = CoroutinesTestRule()
//
//    @Before
//    fun setUp() {
//        hiltRule.inject() // 테스트 클래스에 의존성 주입
//    }
//
//    @Inject
//    lateinit var repository: SiteRepository
//
//    @Test
//    fun getMain() = runTest {
//
//        val mockRepository = mock<SiteRepository>
//        val main = repository.main(SiteType.Damoang)
//        assertNotNull(main)
//    }
//}