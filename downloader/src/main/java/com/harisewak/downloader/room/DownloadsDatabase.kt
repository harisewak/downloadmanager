package com.harisewak.downloader.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.harisewak.downloader.Request

@Database(entities = [Request::class], version = 1)
abstract class DownloadsDatabase : RoomDatabase() {

    abstract fun requestDao(): RequestDao

}