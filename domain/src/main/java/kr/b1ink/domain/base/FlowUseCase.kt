package kr.b1ink.domain.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

abstract class FlowUseCase<in T, out R> {
    abstract suspend fun execute(params: T): Flow<Result<R>>
    operator fun invoke(params: T): Flow<Result<R>> = flow {
        emitAll(execute(params))
    }
}