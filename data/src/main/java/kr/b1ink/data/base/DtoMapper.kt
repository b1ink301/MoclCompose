package kr.b1ink.data.base

interface DtoMapper<in T, out R> {
    fun T.mapping(): R
}