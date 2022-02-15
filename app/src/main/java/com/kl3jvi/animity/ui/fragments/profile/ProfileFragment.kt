package com.kl3jvi.animity.ui.fragments.profile

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.apollographql.apollo3.api.ApolloResponse
import com.kl3jvi.animity.AnimeListCollectionQuery
import com.kl3jvi.animity.R
import com.kl3jvi.animity.data.model.ui_models.AnimeMetaModel
import com.kl3jvi.animity.databinding.FragmentProfileBinding
import com.kl3jvi.animity.databinding.FragmentProfileGuestBinding
import com.kl3jvi.animity.ui.activities.login.LoginActivity
import com.kl3jvi.animity.ui.activities.main.MainActivity
import com.kl3jvi.animity.ui.adapters.CustomVerticalAdapter
import com.kl3jvi.animity.ui.base.BaseFragment
import com.kl3jvi.animity.utils.Constants.Companion.DEFAULT_COVER
import com.kl3jvi.animity.utils.NetworkUtils.isConnectedToInternet
import com.kl3jvi.animity.utils.hide
import com.kl3jvi.animity.utils.launchActivity
import com.kl3jvi.animity.utils.observeLiveData
import com.kl3jvi.animity.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFragment : BaseFragment<ProfileViewModel, FragmentProfileBinding>(),
    AdapterView.OnItemSelectedListener {

    private lateinit var animeCollectionResponseGlobal: ApolloResponse<AnimeListCollectionQuery.Data>
    override val viewModel: ProfileViewModel by viewModels()
    private val guestBinding: FragmentProfileGuestBinding get() = guestView()
    private val adapter by lazy { CustomVerticalAdapter(playButtonFlag = false) }

    override fun observeViewModel() {
        if (!isGuestLogin()) {
            getProfileData()
            getAnimeListProfileData()
        }
    }

    override fun initViews() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isGuestLogin()) setHasOptionsMenu(true)
        else setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (!isGuestLogin()) binding.root else guestBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.spinner.onItemSelectedListener = this
    }


    private fun getProfileData() {
        observeLiveData(viewModel.profileData, viewLifecycleOwner) {
            binding.bgImage.load(
                if (it.data?.user?.bannerImage.isNullOrEmpty())
                    DEFAULT_COVER
                else
                    it.data?.user?.bannerImage
            )
            binding.userData = it.data
        }
    }

    private fun getAnimeListProfileData() {
        observeLiveData(viewModel.animeList, viewLifecycleOwner) { animeCollectionResponse ->
            binding.animeData = animeCollectionResponse.data
            animeCollectionResponseGlobal = animeCollectionResponse
            val animeRecyclerView = binding.watchedAnime
            animeRecyclerView.layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.HORIZONTAL, false
            )

            animeCollectionResponse.data?.media?.lists?.toList()?.let { addSpinnerItems(it) }

//            when (binding.spinner.selectedItem.toString()) {
//                "Watching" -> {
//                    adapter.submitList(
//                        animeCollectionResponse.data?.media?.lists?.first()
//                            ?.entries?.mapToAnimeMetaModel()
//                    )
//                    animeRecyclerView.adapter = adapter
//                }
//                "Planning" -> {
//                    adapter.submitList(
//                        animeCollectionResponse.data?.media?.lists?.last()
//                            ?.entries?.mapToAnimeMetaModel()
//                    )
//                    animeRecyclerView.adapter = adapter
//                }
//            }

        }
    }


    private fun addSpinnerItems(passedArray: List<AnimeListCollectionQuery.List?>) {
        val list = passedArray.map { it?.name }
        val spinner = binding.spinner
        val spinnerArrayAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                requireContext(), R.layout.spinner_item,
                R.id.textView2,
                list
            ) //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinner.adapter = spinnerArrayAdapter


    }

    override fun getViewBinding(): FragmentProfileBinding =
        FragmentProfileBinding.inflate(layoutInflater)

    private fun guestView(): FragmentProfileGuestBinding =
        FragmentProfileGuestBinding.inflate(layoutInflater)


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_log_out -> {
                viewModel.clearStorage()
                requireActivity().launchActivity<LoginActivity> { }
                requireActivity().finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)?.showBottomNavBar()
        }
    }

    override fun onStart() {
        super.onStart()
        handleNetworkChanges()
    }

    private fun handleNetworkChanges() {
        requireActivity().isConnectedToInternet(viewLifecycleOwner) { isConnected ->
            if (!isGuestLogin() && isConnected) {
                binding.hasInternet.show()
                binding.noInternet.hide()
            } else {
                binding.noInternet.show()
                binding.hasInternet.hide()
            }
        }
    }


    private fun List<AnimeListCollectionQuery.Entry?>.mapToAnimeMetaModel(): List<AnimeMetaModel> {
        return map { animeWatchedData ->
            AnimeMetaModel(
                title = animeWatchedData?.media?.title?.romaji.toString(),
                imageUrl = animeWatchedData?.media?.coverImage?.large.toString(),
                categoryUrl = "category/${
                    animeWatchedData?.media?.title?.romaji
                        .toString()
                        .replace(" ", "-")
                        .replace(":", "")
                        .lowercase(Locale.getDefault())
                }"
            )
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
//        adapter.submitList(
//            animeCollectionResponseGlobal.data
//                ?.media?.lists?.first()?.entries?.mapToAnimeMetaModel()
//        )
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}
