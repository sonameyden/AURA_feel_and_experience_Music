package com.aura.core.data.local

import androidx.room.TypeConverter

/** Simple List<String> <-> String converter for Room, used by PlaylistEntity.songIds. */
class RoomConverters {
    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString(separator = "|")

    @TypeConverter
    fun toStringList(raw: String): List<String> = if (raw.isEmpty()) emptyList() else raw.split("|")
}
