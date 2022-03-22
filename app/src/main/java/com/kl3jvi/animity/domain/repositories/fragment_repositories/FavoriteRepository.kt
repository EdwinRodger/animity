package com.kl3jvi.animity.domain.repositories.fragment_repositories

import com.kl3jvi.animity.data.model.ui_models.test.DetailedAnimeInfo
import com.kl3jvi.animity.utils.NetworkResource
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getGogoUrlFromAniListId(id: Int): Flow<NetworkResource<DetailedAnimeInfo>>
}