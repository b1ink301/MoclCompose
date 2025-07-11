package kr.b1ink.common.ui.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseActivity<T : ViewBinding>(@LayoutRes contentLayoutId: Int) : AppCompatActivity(contentLayoutId) {
    abstract val binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initObserve()
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) = lifecycleScope.launch {
        try {
            onCreateActivity(savedInstanceState)
            initView()
        } catch (e: Exception) {
            Timber.e(e, "Error in init: ${e.message}")
            // 에러 처리 로직 (예: 사용자에게 알림)
        }
    }

    abstract fun initView()

    private fun initObserve() = lifecycleScope.launch {
        lifecycle.addObserver(
            LifecycleObserver(
                callbackOnStart = this@BaseActivity::onStartActivity,
                callbackOnResume = this@BaseActivity::onResumeActivity,
                callbackOnPause = this@BaseActivity::onPauseActivity,
                callbackOnStop = this@BaseActivity::onStopActivity,
                callbackOnDestroy = this@BaseActivity::onDestroyActivity,
            )
        )
    }

    open fun onCreateActivity(savedInstanceState: Bundle?) = Unit
    open fun onStartActivity() = Unit
    open fun onResumeActivity() = Unit
    open fun onPauseActivity() = Unit
    open fun onStopActivity() = Unit
    open fun onDestroyActivity() = Unit

    private inner class LifecycleObserver(
        private val callbackOnStart: (() -> Unit)? = null,
        private val callbackOnResume: (() -> Unit)? = null,
        private val callbackOnPause: (() -> Unit)? = null,
        private val callbackOnStop: (() -> Unit)? = null,
        private val callbackOnDestroy: (() -> Unit)? = null,
    ) : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            callbackOnStart?.invoke()
        }

        override fun onPause(owner: LifecycleOwner) {
            callbackOnPause?.invoke()
        }

        override fun onStop(owner: LifecycleOwner) {
            callbackOnStop?.invoke()
        }

        override fun onResume(owner: LifecycleOwner) {
            callbackOnResume?.invoke()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            lifecycle.removeObserver(this)
            callbackOnDestroy?.invoke()
        }
    }

    protected fun applyTheme(themeResId: Int) {
        setTheme(themeResId)
    }

//    protected fun <T> restoreState(savedInstanceState: Bundle?, key: String, default: T): T {
//        return when (default) {
//            is String -> savedInstanceState?.getString(key, default) as T
//            is Int -> savedInstanceState?.getInt(key, default) as T
//            is Boolean -> savedInstanceState?.getBoolean(key, default) as T
//            // 필요한 다른 타입들 추가
//            else -> default
//        }
//    }
}