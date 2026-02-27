package com.example.app

import org.junit.Assert.*
import org.junit.Test

class LoginValidationTest {

    fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    @Test
    fun email_isValid_returnsTrue() {
        assertTrue(isEmailValid("test@gmail.com"))
    }

    @Test
    fun email_isInvalid_returnsFalse() {
        assertFalse(isEmailValid("wrongemail"))
    }
}