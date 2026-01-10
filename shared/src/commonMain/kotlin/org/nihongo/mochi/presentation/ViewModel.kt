package org.nihongo.mochi.presentation

import kotlinx.coroutines.CoroutineScope

expect abstract class ViewModel() {
    val viewModelScope: CoroutineScope
    
    protected open fun onCleared()
}
