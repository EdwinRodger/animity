package com.kl3jvi.animity.utils

import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


fun <T> observeLiveData(
    liveData: LiveData<T>,
    owner: LifecycleOwner,
    observer: (T) -> Unit
) {
    liveData.runCatching {
        observe(owner) {
            observer(it)
        }
    }.onFailure {
        it.printStackTrace()
    }

}

inline fun <T> LifecycleOwner.collectFlow(
    flow: Flow<T>,
    crossinline collector: suspend (T) -> Unit
) {
    lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.catch { e -> e.printStackTrace() }
                .collect {
                    collector(it)
                }
        }
    }
}

fun <T> Flow<NetworkResource<T>>.mapToState(): Flow<State<T>> = map { resource ->
    State.fromResource(resource)
}
