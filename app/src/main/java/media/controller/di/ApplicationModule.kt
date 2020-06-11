package media.controller.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@Module
@InstallIn(ApplicationComponent::class)
object ApplicationModule {

//    @Provides
//    fun provideAnalyticsService(
//        // Potential dependencies of this type
//    ): AnalyticsService {
//        return Retrofit.Builder()
//            .baseUrl("https://example.com")
//            .build()
//            .create(AnalyticsService::class.java)
//    }

}