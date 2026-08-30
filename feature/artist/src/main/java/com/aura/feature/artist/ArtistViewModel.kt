package com.aura.feature.artist

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor() : ViewModel()
// TODO Phase 4: inject an ArtistRepository (mirrors SongRepository) once CatalogApi.getArtist is wired end-to-end
