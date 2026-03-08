package com.snapp.domain.validation

/**
 * Shared login form validation rules. Use from AuthSharedViewModel and platform UI
 * for consistent messages and rules (e.g. password min length).
 */
object LoginValidation {

    const val MIN_PASSWORD_LENGTH = 6

    fun validateUsername(username: String): ValidationResult =
        when {
            username.isBlank() -> ValidationResult.Invalid("Username is required")
            else -> ValidationResult.Valid
        }

    fun validatePassword(password: String): ValidationResult =
        when {
            password.isBlank() -> ValidationResult.Invalid("Password is required")
            password.length < MIN_PASSWORD_LENGTH -> ValidationResult.Invalid("Password must be at least $MIN_PASSWORD_LENGTH characters")
            else -> ValidationResult.Valid
        }

    fun validateLogin(username: String, password: String): Pair<ValidationResult, ValidationResult> =
        validateUsername(username) to validatePassword(password)

    fun isLoginValid(username: String, password: String): Boolean {
        val (u, p) = validateLogin(username, password)
        return u.isValid && p.isValid
    }
}
