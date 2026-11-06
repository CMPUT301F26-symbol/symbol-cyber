/*
 * TaskAwait.kt
 * Lets repository code wait for a Firebase Task inside a coroutine, so
 * callers get the result or the error directly.
 * Known issues: none.
 */

package com.example.code.data

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Suspends until this task finishes and returns its result.
 *
 * @throws Exception the task's exception if it failed.
 */
suspend fun <T> Task<T>.awaitResult(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { error -> continuation.resumeWithException(error) }
    }
