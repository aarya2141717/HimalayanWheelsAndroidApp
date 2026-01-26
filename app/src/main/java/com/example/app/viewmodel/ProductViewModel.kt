package com.example.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.app.model.ProductModel
import com.example.app.repository.ProductRepo
import com.example.app.repository.ProductRepoImpl

class ProductViewModel : ViewModel() {
    private val repo: ProductRepo = ProductRepoImpl()

    fun addProduct(name: String, price: String, type: String, description: String, imageUri: Uri?, callback: (Boolean, String) -> Unit) {
        val product = ProductModel(
            name = name,
            price = price,
            type = type,
            description = description
        )
        repo.addProduct(product, imageUri, callback)
    }

    fun getAllProducts(callback: (List<ProductModel>) -> Unit) {
        repo.getAllProducts(callback)
    }
}
