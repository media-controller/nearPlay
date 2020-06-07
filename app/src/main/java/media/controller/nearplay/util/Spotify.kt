package media.controller.nearplay.util

import com.spotify.protocol.client.CallResult
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T : Any> CallResult<T>.awaitResult(
    context: CoroutineContext = Dispatchers.IO
): T = withContext<T>(context) {
    return@withContext suspendCancellableCoroutine { cancellableContinuation ->
        setResultCallback { result ->
            Timber.v("[%s|reqID:%s] Event: %s", this@awaitResult, this@awaitResult.requestId, result)
            cancellableContinuation.resume(result)
        }
        setErrorCallback { error ->
            Timber.e(error, "[%s|reqID:%s]", this@awaitResult, this@awaitResult.requestId)
            cancellableContinuation.resumeWithException(error)
        }
        cancellableContinuation.invokeOnCancellation {
            cancel()
            Timber.v("[%s|reqID:%s] cancelled", this@awaitResult, this@awaitResult.requestId)
        }
    }
}

suspend fun CallResult<Empty>.awaitCompletion(
    context: CoroutineContext = Dispatchers.IO
) {
    awaitResult(context)
}

@ExperimentalCoroutinesApi
fun <T : Any> Subscription<T>.asFlow(
    context: CoroutineContext = Dispatchers.IO
): Flow<T> = callbackFlow {
    setEventCallback { result ->
        Timber.v("[%s|reqID:%s] Event: %s", this@asFlow, this@asFlow.requestId, result)
        offer(result)
    }
    setLifecycleCallback(object : Subscription.LifecycleCallback {
        override fun onStart() {
            Timber.v("[%s|reqID:%s]", this@asFlow, this@asFlow.requestId)
        }

        override fun onStop() {
            Timber.v("[%s|reqID:%s]", this@asFlow, this@asFlow.requestId)
            close()
        }
    })
    setErrorCallback { error ->
        Timber.e(error, "[%s|reqID:%s]", this@asFlow, this@asFlow.requestId)
        close(error)
    }
    awaitClose {
        this@asFlow.cancel() //TODO: This may be the wrong
        Timber.v("[%s|reqID:%s] cancelled", this@asFlow, this@asFlow.requestId)
    }
}.flowOn(context)