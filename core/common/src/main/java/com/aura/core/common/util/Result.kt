package com.aura.core.common.util

/**
 * Generic sealed result wrapper. Repositories return this instead of throwing
 * raw exceptions, per the "design for failure" best practice — ViewModels
 * map this into an explicit sealed UiState, never expose it directly to Compose.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
    data object Loading : AppResult<Nothing>
}

/** Converted from raw exceptions at the repository boundary — see best-practices doc. */
sealed interface AppError {
    data object Network : AppError
    data object Unauthorized : AppError
    data object NotFound : AppError
    data object Server : AppError
    data class Unknown(val message: String? = null) : AppError
}
