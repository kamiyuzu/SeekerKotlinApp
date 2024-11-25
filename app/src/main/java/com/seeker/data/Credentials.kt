package com.seeker.data

data class Credentials(
    var user: String = "",
    var pwd: String = "",
    var remember: Boolean = false,
) {
    fun isNotEmpty(): Boolean {
        return user.isNotEmpty() && pwd.isNotEmpty()
    }
}