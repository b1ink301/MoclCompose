package kr.b1ink.data.base

import android.content.Context
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kr.b1ink.domain.base.Result
import kr.b1ink.domain.model.LoginFailure
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException

internal suspend inline fun <T> safeApiCallResponse(
    context: Context,
    coroutineDispatcher: CoroutineDispatcher,
    crossinline block: suspend () -> T,
): Result<T> {
    return withContext(coroutineDispatcher) {
        try {
            if (!isInternetConnected(context)) {
                return@withContext Result.NetworkError
            }
            Result.Success(block.invoke())
        } catch (throwable: Throwable) {
            errorHandler(throwable)
        }
    }
}

internal suspend inline fun <T : DtoMapper<T, R>, R> safeApiCall(
    context: Context,
    coroutineDispatcher: CoroutineDispatcher,
    crossinline block: suspend () -> T,
): Result<R> {
    return withContext(coroutineDispatcher) {
        try {
            if (!isInternetConnected(context)) {
                return@withContext Result.NetworkError
            }
            Result.Success(
                block.invoke()
                    .run { mapping() })
        } catch (throwable: Throwable) {
            errorHandler(throwable)
        }
    }
}

internal suspend inline fun <T : DtoMapper<T, R>, R> safeApiCallResponseNullable(
    context: Context,
    coroutineDispatcher: CoroutineDispatcher,
    defaultValue: R,
    crossinline block: suspend () -> T,
): Result<R> {
    return withContext(coroutineDispatcher) {
        try {
            if (!isInternetConnected(context)) {
                return@withContext Result.NetworkError
            }
            Result.Success(
                block.invoke()
                    .run { mapping() })
        } catch (e: NullPointerException) {
            Result.Success(defaultValue)
        } catch (throwable: Throwable) {
            errorHandler(throwable)
        }
    }
}

internal suspend inline fun <T : DtoMapper<T, R>, R> safeApiCallList(
    context: Context,
    coroutineDispatcher: CoroutineDispatcher,
    crossinline block: suspend () -> List<T>,
): Result<List<R>> {
    return withContext(coroutineDispatcher) {
        try {
            if (!isInternetConnected(context)) {
                return@withContext Result.NetworkError
            }
            Result.Success(
                block.invoke()
                    .map { it.run { mapping() } })
        } catch (throwable: Throwable) {
            errorHandler(throwable)
        }
    }
}

internal inline fun <T : ResponseBody> safeApiStream(
    context: Context,
    coroutineDispatcher: CoroutineDispatcher,
    crossinline block: suspend () -> T,
): Flow<Result<String>> = flow {
    if (!isInternetConnected(context)) {
        emit(Result.NetworkError)
        return@flow
    }
    val streamSource = block.invoke()
        .source()
    while (currentCoroutineContext().isActive && !streamSource.exhausted()) {
        yield()
        val data = streamSource.readUtf8Line()
        val payload = data?.trim()
            ?.replace("data:", "")
        if (!payload.isNullOrEmpty() && !checkPingEvent(payload)) {
            emit(Result.Success(payload))
        }
    }
    throw RuntimeException("Stream failed")
}.flowOn(coroutineDispatcher)

private fun checkPingEvent(payload: String?): Boolean {
    val parseEvent = JsonParser.parseString(payload).asJsonObject
    return parseEvent.get("event").asString == "Ping"
}

private fun errorHandler(throwable: Throwable): Result<Nothing> {
    throwable.printStackTrace()
    return when (throwable) {
        is IOException -> Result.NetworkError
        is HttpException -> throwable.mapToError()
        is LoginFailure -> Result.LoginError(code = throwable.code, error = throwable.message ?: "Unknown Error")
        else -> Result.Error(error = throwable.message ?: "Unknown Error")
    }
}

private fun HttpException.mapToError(): Result.Error {
    return try {
        Result.Error(code().toString(), message())
    } catch (e: Exception) {
        Result.Error()
    }
}