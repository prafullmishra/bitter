package `in`.wordmug.bitter

import android.app.Application
import timber.log.Timber

class BitterApplication : Application()
{
    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}