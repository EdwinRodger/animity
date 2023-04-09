package com.kl3jvi.animity.domain.repositories

import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import com.kl3jvi.animity.data.model.ui_models.GogoAnimeKeys
import com.kl3jvi.animity.data.model.ui_models.HomeData
import com.kl3jvi.animity.data.model.ui_models.Keys
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getHomeData(): Flow<HomeData>
    fun getEncryptionKeys(): Flow<Keys>
    fun getNotifications(): Flow<WorkInfo>
    var work: PeriodicWorkRequest
}
