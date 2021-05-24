package com.wt.cloudmedia.db.recentMovie

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "recent_movie_table")
data class RecentMovie(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "play_date") val date: Date?,
    @ColumnInfo(name = "time_stamp") val timeStamp: Long)