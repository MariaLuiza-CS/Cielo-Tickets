package com.cielotickets.app.data.di

import android.content.Context
import androidx.room.Room
import com.cielotickets.app.data.local.AppDatabase
import com.cielotickets.app.data.local.dao.EventDao
import com.cielotickets.app.data.local.dao.PendingPurchaseDao
import com.cielotickets.app.data.local.dao.TicketDao
import com.cielotickets.app.data.repository.EventRepositoryImpl
import com.cielotickets.app.data.repository.PaymentRepositoryImpl
import com.cielotickets.app.data.repository.TicketRepositoryImpl
import com.cielotickets.app.domain.repository.EventRepository
import com.cielotickets.app.domain.repository.PaymentRepository
import com.cielotickets.app.domain.repository.TicketRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEventRepository(eventRepositoryImpl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(paymentRepositoryImpl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindTicketRepository(ticketRepositoryImpl: TicketRepositoryImpl): TicketRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "cielo_tickets_db",
    ).fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideEventDao(database: AppDatabase): EventDao = database.eventDao()

    @Provides
    fun providePendingPurchaseDao(database: AppDatabase): PendingPurchaseDao = database.pendingPurchaseDao()

    @Provides
    fun provideTicketDao(database: AppDatabase): TicketDao = database.ticketDao()
}
