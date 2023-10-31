package com.gray.vulf.savemealerter.utility

fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@(.+)$")
    return emailRegex.matches(email)
}

fun isValidName(name: String): Boolean {
    return name.length >= 1
}

fun isValidPassword(password: String): Boolean {
    return password.length > 6
}