package com.seeker.auth

import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth

fun createAnonymousAccount(onResult: (Throwable?) -> Unit) {
    Firebase.auth.signInAnonymously()
        .addOnCompleteListener { onResult(it.exception) }
}

fun authenticate(email: String, password: String, onResult: (Throwable?) -> Unit) {
    Firebase.auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { onResult(it.exception) }
}

//Create a new account
fun linkAccount(email: String, password: String, onResult: (Throwable?) -> Unit) {
    val credential = EmailAuthProvider.getCredential(email, password)

    Firebase.auth.currentUser!!.linkWithCredential(credential)
        .addOnCompleteListener { onResult(it.exception) }
}