package media.controller.nearplay.di

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.messages.MessagesClient
import com.tfcporciuncula.flow.FlowSharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun sharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("NearPlay", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun flowSharedPreferences(prefs: SharedPreferences) = FlowSharedPreferences(prefs)

    @Provides
    @Singleton
    fun nearbyConnections(@ApplicationContext context: Context): ConnectionsClient = Nearby.getConnectionsClient(context)

    @Provides
    @Singleton
    fun nearbyMessages(@ApplicationContext context: Context): MessagesClient = Nearby.getMessagesClient(context)
}