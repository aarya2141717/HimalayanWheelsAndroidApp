package com.example.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginComposeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<SignUpActivity>()

    @Test
    fun loginFlow_typingAndClicking() {

        // Type email
        composeTestRule
            .onNodeWithTag("emailField")
            .performTextInput("test@gmail.com")

        // Type password
        composeTestRule
            .onNodeWithTag("passwordField")
            .performTextInput("123456")

        // Click login button
        composeTestRule
            .onNodeWithTag("loginButton")
            .performClick()
    }
}