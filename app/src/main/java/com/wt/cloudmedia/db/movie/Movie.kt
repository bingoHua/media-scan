package com.wt.cloudmedia.db.movie

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "movie_table")
data class Movie(@PrimaryKey val id: String,
                 @ColumnInfo(name = "url") val url: String?,
                 @ColumnInfo(name = "thumbnail_url") val thumbnail: String,
                 @ColumnInfo(name = "name") val name: String,
                 @ColumnInfo(name = "create_time") val time: Long,
                 @ColumnInfo(name = "time_stamp") val timeStamp:Long,
                 @ColumnInfo(name = "play_date") val playDate: Date?)
