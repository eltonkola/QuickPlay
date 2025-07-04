package com.eltonkola.quickplay

import android.app.Application
import androidx.room.Room
import com.eltonkola.quickplay.data.WebServerManager
import com.eltonkola.quickplay.data.local.AppDatabase
import com.eltonkola.quickplay.data.local.GameDao
import com.eltonkola.quickplay.data.remote.RomRepository
import com.eltonkola.quickplay.data.remote.RomRepositoryImpl
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

    @Provides
    @Singleton
    fun provideRomRepository(app: Application): RomRepository {
        return RomRepositoryImpl(app)
    }


    @Provides
    @Singleton
    fun provideWebServerManager(
        app: Application,
        gameDao: GameDao
    ): WebServerManager {
        return WebServerManager(app, gameDao)
    }


}
