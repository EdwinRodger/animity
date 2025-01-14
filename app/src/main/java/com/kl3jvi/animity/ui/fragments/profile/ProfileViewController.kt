package com.kl3jvi.animity.ui.fragments.profile

import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.carousel
import com.kl3jvi.animity.CardAnimeBindingModel_
import com.kl3jvi.animity.data.model.ui_models.AniListMedia
import com.kl3jvi.animity.data.model.ui_models.ProfileData
import com.kl3jvi.animity.noAnime
import com.kl3jvi.animity.profileCard
import com.kl3jvi.animity.title
import com.kl3jvi.animity.utils.Constants.Companion.randomId
import com.kl3jvi.animity.utils.navigateSafe

const val DEFAULT_COVER = "https://bit.ly/3p6DE28"
fun EpoxyController.buildProfile(
    userData: ProfileData?
) {
    profileCard {
        id(randomId())
        userData?.userData?.let {
            backgroundImage(it.bannerImage.ifEmpty { DEFAULT_COVER })
            userData(it)
        }
    }
    userData?.profileRow?.forEach { profileRow ->
        title {
            id(randomId())
            title(profileRow.title)
        }
        carousel {
            id(randomId())
            models(profileRow.anime.modelCardAnimeProfile())
        }
    } ?: noAnime { id(randomId()) }
}

/* A function that takes a list of AniListMedia and returns a list of CardAnimeBindingModel_ */
fun List<AniListMedia>.modelCardAnimeProfile(): List<CardAnimeBindingModel_> {
    return map { media ->
        CardAnimeBindingModel_()
            .id(randomId())
            .clickListener { view ->
                val direction =
                    ProfileFragmentDirections.actionNavigationProfileToNavigationDetails(
                        media,
                        0
                    )
                view.navigateSafe(direction)
            }.animeInfo(media)
    }
}
