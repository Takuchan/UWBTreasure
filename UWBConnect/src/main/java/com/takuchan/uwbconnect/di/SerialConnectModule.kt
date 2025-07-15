package com.takuchan.uwbconnect.di

import com.takuchan.uwbconnect.repository.SerialConnectRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

//@Module
//@InstallIn(SingletonComponent::class) // アプリケーションのライフタイムでインスタンスが単一になるように指定
//object SerialConnectModule {
//
//    @Singleton
//    @Provides
//    fun provideSerialDataRepository(): SerialConnectRepository {
//        return SerialConnectRepository()
//    }
//}