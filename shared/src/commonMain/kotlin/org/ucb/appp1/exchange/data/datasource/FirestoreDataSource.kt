package org.ucb.appp1.exchange.data.datasource

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import org.ucb.appp1.exchange.data.entity.DollarEntity

class FirestoreDataSource {

    suspend fun saveDollarToCloud(dollar: DollarEntity) {
        val firestore = Firebase.firestore
        firestore.collection("dollars").add(dollar)
    }

    suspend fun getDollarsFromCloud(): List<DollarEntity> {
        val firestore = Firebase.firestore
        val snapshot = firestore.collection("dollars").get()

        return snapshot.documents.map { it.data<DollarEntity>() }
    }
}