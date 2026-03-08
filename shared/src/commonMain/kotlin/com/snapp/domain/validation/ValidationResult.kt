package com.snapp.domain.validation

/**
 * Result of validating a single field or form.
 * Reusable across shared and platform code for consistent messages and rules.
 */
sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()

    val isValid: Boolean get() = this is Valid
}
