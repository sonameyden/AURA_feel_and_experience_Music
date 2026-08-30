package com.aura.core.data.di

import android.content.Context
import androidx.room.Room
import com.aura.core.data.local.AuraDatabase
import com.aura.core.data.local.dao.PlaylistDao
import com.aura.core.data.local.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAuraDatabase(@ApplicationContext context: Context): AuraDatabase =
        Room.databaseBuilder(context, AuraDatabase::class.java, "aura.db")
            .fallbackToDestructiveMigration() // fine for prototype; replace with real migrations before release
            .build()

    @Provides
    fun provideSongDao(database: AuraDatabase): SongDao = database.songDao()

    @Provides
    fun providePlaylistDao(database: AuraDatabase): PlaylistDao = database.playlistDao()
}
