package media.controller.nearplay.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import media.controller.nearplay.R
import media.controller.nearplay.databinding.FragmentSearchBinding
import media.controller.nearplay.viewModels.SearchViewModel
import reactivecircus.flowbinding.android.widget.textChangeEvents
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@FlowPreview
@ExperimentalTime
@AndroidEntryPoint
class Search : Fragment(R.layout.fragment_search) {

    private val vm: SearchViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(FragmentSearchBinding.bind(view)) {
        searchBar.textChangeEvents()
            .debounce(500.milliseconds)
            .map { it.view.text }
            .filter { it.isNotEmpty() }
            .onEach {
                vm.search(it.toString())
            }.launchIn(lifecycleScope)

        vm.searchResults.observe(viewLifecycleOwner, Observer {
            results.text = it.tracks?.first()?.name
        })

        Unit
    }

}