package com.example.app.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.app.model.ProductModel
import com.google.firebase.database.FirebaseDatabase

interface ProductRepo {
    fun addProduct(product: ProductModel, imageUri: Uri?, callback: (Boolean, String) -> Unit)
    fun getAllProducts(callback: (List<ProductModel>) -> Unit)
}

class ProductRepoImpl : ProductRepo {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val productsRef = database.reference.child("products")

    override fun addProduct(product: ProductModel, imageUri: Uri?, callback: (Boolean, String) -> Unit) {
        val productId = productsRef.push().key
        if (productId != null) {
            if (imageUri != null) {
                MediaManager.get().upload(imageUri)
                    .unsigned("aarya123") // Using your specified unsigned upload preset
                    .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val imageUrl = resultData["secure_url"] as? String ?: ""
                        val productWithId = product.copy(id = productId, imageUrl = imageUrl)
                        saveProductToDatabase(productId, productWithId, callback)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        callback(false, "Image upload failed: ${error.description}")
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        callback(false, "Image upload rescheduled: ${error.description}")
                    }
                }).dispatch()
            } else {
                val productWithId = product.copy(id = productId)
                saveProductToDatabase(productId, productWithId, callback)
            }
        } else {
            callback(false, "Failed to generate product ID")
        }
    }

    private fun saveProductToDatabase(productId: String, product: ProductModel, callback: (Boolean, String) -> Unit) {
        productsRef.child(productId).setValue(product)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Product Added Successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to add product")
                }
            }
    }

    override fun getAllProducts(callback: (List<ProductModel>) -> Unit) {
        productsRef.get().addOnSuccessListener { snapshot ->
            val productList = mutableListOf<ProductModel>()
            for (child in snapshot.children) {
                val product = child.getValue(ProductModel::class.java)
                if (product != null) {
                    productList.add(product)
                }
            }
            callback(productList)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }
}
