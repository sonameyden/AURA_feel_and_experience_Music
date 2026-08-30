package com.aura.feature.library

import androidx.lifecycle.ViewModel
import com.aura.core.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
    // TODO: expose observeByOwner(currentUserId) as StateFlow once auth/user identity exists
) : ViewModel()
