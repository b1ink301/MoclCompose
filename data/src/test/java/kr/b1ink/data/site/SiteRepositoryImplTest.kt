@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("NonAsciiCharacters")

package kr.b1ink.data.site

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kr.b1ink.data.CoroutinesTestRule
import kr.b1ink.domain.data.SiteRepository
import kr.b1ink.domain.model.MainItem
import kr.b1ink.domain.model.SiteType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

@RunWith(JUnit4::class)
class SiteRepositoryImplTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesRule = CoroutinesTestRule()

    @Test
    fun `메인 목록이 없을때`() = runTest {
        val mock = mockk<SiteRepository>()
        coEvery { mock.getMainData(any()) } returns kr.b1ink.domain.base.Result.Success(emptyList())
        mock.getMainData(SiteType.Damoang)
        coVerify { mock.getMainData(SiteType.Damoang) }
        assertTrue(mock.getMainData(SiteType.Damoang) is kr.b1ink.domain.base.Result.Success)
    }

    @Test
    fun `메인 목록이 있을때`() = runTest {
        val mock = mockk<SiteRepository>()
        val jsonString = readJsonFromFile("test_board_link.json")
        val list = Gson().fromJson(jsonString, Array<MainItem>::class.java).toList()
        coEvery { mock.getMainData(any()) } returns kr.b1ink.domain.base.Result.Success(list)
//        confirmVerified()
        mock.getMainData(SiteType.Damoang)
        coVerify { mock.getMainData(SiteType.Damoang) }
//        verify { mock.main(SiteType.Damoang) }
//        assertTrue(mock.main(SiteType.Damoang).isEmpty())
    }

    fun readJsonFromFile(filename: String): String {
        val classLoader = javaClass.classLoader ?: return ""
        val file = File(classLoader.getResource(filename).file)
        return file.readText()
    }
}