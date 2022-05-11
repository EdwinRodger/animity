package com.kl3jvi.animity.ui.fragments.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kl3jvi.animity.data.model.ui_models.HomeData
import com.kl3jvi.animity.domain.use_cases.GetAnimesUseCase
import com.kl3jvi.animity.utils.NetworkResource
import com.kl3jvi.animity.utils.logError
import com.kl3jvi.animity.utils.logMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAnimesUseCase: GetAnimesUseCase,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private var _homeData = MutableLiveData<HomeData>()
    var homeData: LiveData<HomeData> = _homeData

    init {
        getHomePageData()
        getHomeNewData()
    }

    /**
     * It fetches data from the server and updates the UI.
     */
    private fun getHomePageData() {
        getAnimesUseCase().flowOn(ioDispatcher).catch { e ->
            logError(e)
        }.onEach {
            when (it) {
                is NetworkResource.Failed -> {
                    logMessage(it.message)
                }
                is NetworkResource.Success -> {
                    _homeData.value = it.data
                }
            }
        }.launchIn(viewModelScope)
    }

    private val _homeNewData = MutableStateFlow(HomeData())
    val homeNewData = _homeNewData.asStateFlow()

    /**
     * It gets the data from the repository and sets it to the homeNewData variable.
     */
    private fun getHomeNewData() {
        viewModelScope.launch(ioDispatcher) {
            getAnimesUseCase().catch { e -> logError(e) }
//                .collect { result ->
////                _homeNewData.value = State.fromResource(result)
//            }
        }
    }
}
