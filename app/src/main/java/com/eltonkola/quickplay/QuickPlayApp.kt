package com.eltonkola.quickplay

import android.app.Application
import androidx.room.Room
import com.eltonkola.quickplay.data.local.AppDatabase
import com.eltonkola.quickplay.data.local.GameDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@HiltAndroidApp
class QuickPlayApp : Application() {
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {

        return Room.databaseBuilder(
            app.applicationContext,
            AppDatabase::class.java,
            "quickplay-db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideGameDao(db: AppDatabase): GameDao {
        return db.gameDao()
    }


}
