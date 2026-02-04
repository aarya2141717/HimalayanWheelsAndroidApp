package com.example.app.repository

import androidx.lifecycle.MutableLiveData
import com.example.app.model.VehicleModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class VendorRepository {
    private val db = FirebaseFirestore.getInstance()

    fun observeVehiclesForVendor(vendorId: String): MutableLiveData<List<VehicleModel>> {
        val live = MutableLiveData<List<VehicleModel>>()
        db.collection("vehicles").whereEqualTo("vendorId", vendorId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    live.postValue(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toObject(VehicleModel::class.java)?.copy(id = it.id) } ?: emptyList()
                live.postValue(list)
            }
        return live
    }

    suspend fun addVehicle(vehicle: VehicleModel): Result<String> {
        return try {
            val doc = db.collection("vehicles").add(vehicle).await()
            Result.success(doc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVehicle(id: String, map: Map<String, Any>) {
        db.collection("vehicles").document(id).update(map).await()
    }

    suspend fun deleteVehicle(id: String) {
        db.collection("vehicles").document(id).delete().await()
    }
}
