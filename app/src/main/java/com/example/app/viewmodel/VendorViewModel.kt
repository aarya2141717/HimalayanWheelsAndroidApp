package com.example.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.VehicleModel
import com.example.app.repository.VendorRepository
import kotlinx.coroutines.launch

class VendorViewModel(private val repo: VendorRepository = VendorRepository()) : ViewModel() {

    private val _vehicles = MutableLiveData<List<VehicleModel>>()
    val vehicles: LiveData<List<VehicleModel>> = _vehicles

    fun observeVendor(vendorId: String) {
        val live = repo.observeVehiclesForVendor(vendorId)
        live.observeForever { _vehicles.postValue(it) }
    }

    fun addVehicle(vehicle: VehicleModel, cb: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repo.addVehicle(vehicle)
            if (res.isSuccess) cb(true, res.getOrNull()) else cb(false, res.exceptionOrNull()?.message)
        }
    }

    fun updateVehicle(id: String, map: Map<String, Any>, cb: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repo.updateVehicle(id, map)
                cb(true, null)
            } catch (e: Exception) {
                cb(false, e.message)
            }
        }
    }

    fun deleteVehicle(id: String, cb: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repo.deleteVehicle(id)
                cb(true, null)
            } catch (e: Exception) {
                cb(false, e.message)
            }
        }
    }
}
