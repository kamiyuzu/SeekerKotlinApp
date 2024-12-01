package com.seeker.external.services

import android.util.Log
import com.seeker.activities.client
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class LoginResultOk(val token: String)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AssetResult(val id: String, val username: String, val set: String, val latitude: String, val longitude: String)

@Serializable
data class IndexResultOk(val data: List<AssetResult>)

@Serializable
data class Asset(val username: String, val set: String, val latitude: String, val longitude: String)

@Serializable
data class AssetResultOk(val data: AssetResult)

val backendUrl = "http://10.6.0.3:4000"

suspend fun login(username: String, password: String): String {
    val loginResult: String =
        withContext(Dispatchers.IO){
            try {
                val response = client.post("$backendUrl/api/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(username, password))
                }
                if (response.status.value == 400) throw RuntimeException("Login unexpected error: " + response.body<Any?>().toString())
                val loginResultOk = response.body<LoginResultOk>()
                loginResultOk.token
            } catch (e: Exception) {
                Log.println(Log.INFO,"Backend Login", "Error ${e.stackTraceToString()}")
                // Handle the error (e.g., logging or returning default data)
                throw RuntimeException("Error ${e.stackTraceToString()}")
            }
        }
    Log.println(Log.DEBUG,"Backend Login", "result: $loginResult")
    return loginResult
}

suspend fun index(username: String): List<AssetResult> {
    val indexResultOk: List<AssetResult> =
        withContext(Dispatchers.IO){
            try {
                val response = client.get("$backendUrl/api/users/$username/assets") {
                    contentType(ContentType.Application.Json)
                }
                if (response.status.value == 400) throw RuntimeException("Login unexpected error: " + response.body<Any?>().toString())
                val indexResultOk = response.body<IndexResultOk>()
                indexResultOk.data
            } catch (e: Exception) {
                Log.println(Log.INFO,"Backend Index", "Error ${e.stackTraceToString()}")
                // Handle the error (e.g., logging or returning default data)
                throw RuntimeException("Error ${e.stackTraceToString()}")
            }
        }
    Log.println(Log.DEBUG,"Backend Login", "result: $indexResultOk")
    return indexResultOk
}

suspend fun assetPost(username: String, latitude: String, longitude: String, set: String): AssetResult {
    val loginResult: AssetResult =
        withContext(Dispatchers.IO){
            try {
                val response = client.post("$backendUrl/api/users/$username/assets") {
                    contentType(ContentType.Application.Json)
                    setBody(Asset(username, set, latitude, longitude))
                }
                if (response.status.value == 400) throw RuntimeException("Login unexpected error: " + response.body<Any?>().toString())
                val assetResultOk = response.body<AssetResultOk>()
                assetResultOk.data
            } catch (e: Exception) {
                Log.println(Log.INFO,"Backend Login", "Error ${e.stackTraceToString()}")
                // Handle the error (e.g., logging or returning default data)
                throw RuntimeException("Error ${e.stackTraceToString()}")
            }
        }
    Log.println(Log.DEBUG,"Backend Login", "result: $loginResult")
    return loginResult
}
