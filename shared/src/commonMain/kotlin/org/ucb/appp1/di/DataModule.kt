package org.ucb.appp1.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

// Imports de Room
import org.ucb.appp1.exchange.data.database.AppDatabase
import org.ucb.appp1.exchange.data.database.AppDatabaseConstructor
import org.ucb.appp1.exchange.data.datasource.FirestoreDataSource

// Imports de Exchange (Firebase + Room)
import org.ucb.appp1.exchange.data.datasource.RealTimeDataBase
import org.ucb.appp1.exchange.data.datasource.RemoteConfigDataSource // ✅ NUEVO
import org.ucb.appp1.exchange.data.repository.ExchangeRepositoryImpl
import org.ucb.appp1.exchange.domain.repository.ExchangeRepository

// Imports de Github
import org.ucb.appp1.userinformation.data.datasource.GithubRemoteDataSource
import org.ucb.appp1.userinformation.data.repository.GithubRepositoryImpl
import org.ucb.appp1.userinformation.data.service.GitHubApiService
import org.ucb.appp1.userinformation.domain.repository.GithubRepository

val dataModule = module {


    single<AppDatabase> {
        AppDatabaseConstructor.create(androidContext())
    }

    single { get<AppDatabase>().dollarDao() }

    single<GithubRemoteDataSource> { GitHubApiService() }
    single<GithubRepository> { GithubRepositoryImpl(get()) }


    singleOf(::RealTimeDataBase)

    singleOf(::RemoteConfigDataSource)

    single<ExchangeRepository> { ExchangeRepositoryImpl(get(), get()) }
    singleOf(::FirestoreDataSource)

}