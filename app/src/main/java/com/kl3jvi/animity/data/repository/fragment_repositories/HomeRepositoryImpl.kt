package com.kl3jvi.animity.data.repository.fragment_repositories

import com.kl3jvi.animity.data.model.ui_models.toAnimeHorizontal
import com.kl3jvi.animity.data.model.ui_models.toAnimeVertical
import com.kl3jvi.animity.data.network.anime_service.AnimeApiClient
import com.kl3jvi.animity.domain.repositories.fragment_repositories.HomeRepository
import com.kl3jvi.animity.ui.adapters.testAdapter.HomeRecyclerViewItem
import com.kl3jvi.animity.utils.Constants.Companion.TYPE_MOVIE
import com.kl3jvi.animity.utils.Constants.Companion.TYPE_NEW_SEASON
import com.kl3jvi.animity.utils.Constants.Companion.TYPE_POPULAR_ANIME
import com.kl3jvi.animity.utils.Constants.Companion.TYPE_RECENT_SUB
import com.kl3jvi.animity.utils.parser.HtmlParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("BlockingMethodInNonBlockingContext")
class HomeRepositoryImpl @Inject constructor(
    private val apiClient: AnimeApiClient,
    private val ioDispatcher: CoroutineDispatcher
) : HomeRepository {
    override val parser: HtmlParser
        get() = HtmlParser

    override suspend fun fetchRecentSubOrDub(
        header: Map<String, String>,
        page: Int,
        type: Int
    ): List<HomeRecyclerViewItem.Anime> = withContext(ioDispatcher) {
        parser.parseRecentSubOrDub(
            apiClient.fetchRecentSubOrDub(header = header, page = page, type = type).string(),
            TYPE_RECENT_SUB
        ).map { it.toAnimeHorizontal() }
    }

    override suspend fun fetchPopularFromAjax(
        header: Map<String, String>,
        page: Int
    ): List<HomeRecyclerViewItem.Anime> = withContext(ioDispatcher) {
        parser.parsePopular(
            apiClient.fetchPopularFromAjax(header = header, page = page).string(),
            TYPE_POPULAR_ANIME
        ).map { it.toAnimeVertical() }
    }

    override suspend fun fetchNewSeason(
        header: Map<String, String>,
        page: Int
    ): List<HomeRecyclerViewItem.Anime> = withContext(ioDispatcher) {
        parser.parseMovie(
            apiClient.fetchNewSeason(header = header, page = page).string(),
            TYPE_NEW_SEASON
        ).map { it.toAnimeHorizontal() }
    }

    override suspend fun fetchMovies(
        header: Map<String, String>,
        page: Int
    ): List<HomeRecyclerViewItem.Anime> = withContext(ioDispatcher) {
        parser.parseMovie(apiClient.fetchMovies(header = header, page = page).string(), TYPE_MOVIE)
            .map { it.toAnimeHorizontal() }
    }
}
