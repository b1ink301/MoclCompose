package kr.b1ink.domain.base

sealed interface Result<out T> {
    data class Success<out T>(val data: T) : Result<T>
    data class Error(val code: String = "", val error: String = "") : Result<Nothing>
    data class LoginError(val code: String = "", val error: String = "") : Result<Nothing>
    data object NetworkError : Result<Nothing>
    data object Loading : Result<Nothing>
}