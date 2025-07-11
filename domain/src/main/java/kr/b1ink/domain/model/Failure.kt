package kr.b1ink.domain.model

abstract class Failure(
    open val code: String,
    open val error: String
) : RuntimeException(error)

data class LoginFailure(
    override val code: String = "500", override val error: String = "로그인하지 않았습니다."
) : Failure(code, error)

class NetworkFailure(
    override val code: String = "400", override val error: String = "네트워크 오류가 발생했습니다."
) : Failure(code, error)

class CafeFailure(
    override val code: String, override val error: String
) : Failure(code, error)