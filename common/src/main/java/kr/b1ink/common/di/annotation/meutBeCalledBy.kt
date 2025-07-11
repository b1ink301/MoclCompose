package kr.b1ink.common.di.annotation

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class MustBeCalledBy(val className: String)