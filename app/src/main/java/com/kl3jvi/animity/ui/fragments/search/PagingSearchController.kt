package com.kl3jvi.animity.ui.fragments.search

import androidx.navigation.findNavController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging3.PagingDataEpoxyController
import com.kl3jvi.animity.SearchLayoutBindingModel_
import com.kl3jvi.animity.data.model.ui_models.AniListMedia
import com.kl3jvi.animity.utils.logError

class PagingSearchController : PagingDataEpoxyController<AniListMedia>() {
    /**
     * It creates a new EpoxyModel for each item in the list.
     *
     * @param currentPosition Int - The current position of the item in the list
     * @param item AniListMedia? - The item that is being bound to the view.
     * @return A EpoxyModel<*>
     */
    override fun buildItemModel(currentPosition: Int, item: AniListMedia?): EpoxyModel<*> {
        return SearchLayoutBindingModel_()
            .id(item?.idAniList)
            .clickListener { view ->
                try {
                    item?.let {
                        val directions =
                            SearchFragmentDirections.actionNavigationExploreToNavigationDetails(item)
                        view.findNavController().navigate(directions)
                    }
                } catch (e: Exception) {
                    logError(e)
                }
            }
            .animeInfo(item)
    }
}