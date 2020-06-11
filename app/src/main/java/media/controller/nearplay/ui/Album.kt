package media.controller.nearplay.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import media.controller.nearplay.R
import media.controller.nearplay.databinding.FragmentAlbumBinding

@AndroidEntryPoint
class Album : Fragment(R.layout.fragment_album) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(FragmentAlbumBinding.bind(view)) {

    }
}