package com.takuchan.uwbconnect.repository

import android.util.Log
import com.takuchan.uwbconnect.data.TrilaterationResult
import dagger.hilt.android.scopes.ActivityRetainedScoped
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@ActivityRetainedScoped
class UwbDataRepository @Inject constructor() {

    // SharedFlowを使い、新しいデータが来たことをイベントとして通知します。
    private val _trilaterationFlow = MutableSharedFlow<TrilaterationResult>()
    val trilaterationFlow = _trilaterationFlow.asSharedFlow()

    /**
     * UsbSerialViewModelから呼び出され、新しい測位結果をFlowに流します。
     */
    suspend fun updateResult(result: TrilaterationResult) {
        Log.d("UwbDataRepository","データは受け取るよ${result}")
        _trilaterationFlow.emit(result)
    }
}