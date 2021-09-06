package com.harisewak.downloader.room

import android.content.Context
import androidx.room.Room

object DatabaseUtil {

    private var requestDao: RequestDao? = null

    fun requestDao(context: Context): RequestDao {

        if (requestDao == null) {

            requestDao = Room.databaseBuilder(
                context,
                DownloadsDatabase::class.java, "downloads-database"
            )
                .build()
                .requestDao()

        }

        // todo find a better way to initialize requestDao
        return requestDao!!
    }
}
