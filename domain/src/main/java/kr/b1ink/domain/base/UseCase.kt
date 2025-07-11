package kr.b1ink.domain.base

abstract class UseCase<in T, out R> {
    abstract suspend fun execute(params: T): Result<R>
    suspend operator fun invoke(params: T): Result<R> = try {
        execute(params)
    } catch (e: Exception) {
        Result.Error(error = e.message ?: "알 수 없는 에러 발생")
    }
}