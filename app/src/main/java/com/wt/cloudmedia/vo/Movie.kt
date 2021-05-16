package com.wt.cloudmedia.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_table")
data class Movie(@PrimaryKey val id: String,
                 @ColumnInfo(name = "url") val url: String?,
                 @ColumnInfo(name = "thumbnail_url") val thumbnail: String,
                 @ColumnInfo(name = "name") val name: String,
                 @ColumnInfo(name = "create_time") val time: Long)
