package com.example.app.model

data class VehicleModel(
    val id: String = "",
    val vendorId: String = "",
    val name: String = "",
    val type: String = "",
    val pricePerDay: Double = 0.0,
    val vehicleNumber: String = "",
    val totalCount: Int = 0,
    val description: String = "",
    val imageUrl: String = "",
    val available: Boolean = true
)
