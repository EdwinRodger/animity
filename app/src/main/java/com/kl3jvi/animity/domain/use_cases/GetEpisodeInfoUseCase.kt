package com.kl3jvi.animity.domain.use_cases

import android.util.Log
import com.kl3jvi.animity.data.repository.activity_repositories.PlayerRepositoryImpl
import com.kl3jvi.animity.utils.Constants.Companion.REFERER
import com.kl3jvi.animity.utils.Constants.Companion.getNetworkHeader
import com.kl3jvi.animity.utils.Resource
import com.kl3jvi.animity.utils.logError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetEpisodeInfoUseCase @Inject constructor(
    private val playerRepository: PlayerRepositoryImpl,
    private val ioDispatcher: CoroutineDispatcher
) {

    operator fun invoke(url: String) = flow {
        emit(Resource.Loading())
        try {
            val response = playerRepository.fetchEpisodeMediaUrl(getNetworkHeader(), url)
            emit(Resource.Success(data = response))
        } catch (e: Exception) {
            emit(Resource.Error("Oops an error occurred, try again!"))
        }
    }.flowOn(ioDispatcher)

    //
    fun fetchM3U8(url: String?) = flow {
        emit(Resource.Loading())
        try {
            val response = playerRepository.fetchM3u8Url(getNetworkHeader(), url ?: "")
            Log.e("response", response.toString())
            emit(Resource.Success(data = response.last()))
        } catch (e: Exception) {
            logError(e)
            emit(Resource.Error("Couldn't find a Stream for this Anime"))
        }
    }.flowOn(ioDispatcher)

    //
    fun fetchEncryptedAjaxUrl(url: String?) = flow {
        emit(Resource.Loading())
        try {
            val response = playerRepository.fetchEncryptedAjaxUrl(getNetworkHeader(), url ?: "")
            val streamUrl = "${REFERER}encrypt-ajax.php?${response}"
            emit(Resource.Success(data = streamUrl))
        } catch (e: Exception) {
            logError(e)
            emit(Resource.Error("Couldn't find a Stream for this Anime"))
        }
    }.flowOn(ioDispatcher)

}
