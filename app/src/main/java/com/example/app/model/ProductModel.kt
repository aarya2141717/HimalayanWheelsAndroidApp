package com.example.app.model

data class ProductModel(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val type: String = "", // e.g., Car, Bike, Jeep
    val description: String = "",
    val imageUrl: String = "" // For now, we'll just store a URL or empty string
)
