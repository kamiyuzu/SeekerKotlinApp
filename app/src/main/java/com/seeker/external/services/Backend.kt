package com.seeker.external.services

import android.util.Log
import com.seeker.activities.client
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
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
data class AssetResult(
    val id: String,
    val username: String,
    val set: String,
    val latitude: String,
    val longitude: String,
    val name: String,
    val description: String,
    val tag: String
    )

@Serializable
data class IndexResultOk(val data: List<AssetResult>)

@Serializable
data class Asset(
    val username: String,
    val set: String,
    val latitude: String,
    val longitude: String,
    val tag: String
)

@Serializable
data class AssetResultOk(val data: AssetResult)

const val backendUrl = "http://10.6.0.3:4000"

suspend fun login(username: String, password: String): String {
    val loginResult: String =
        withContext(Dispatchers.IO){
            try {
                val response = client.post("$backendUrl/api/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(username, password))
                }
                Log.println(Log.DEBUG,"Backend Login", "Login response: $response")
                if (response.status.value == 400) Log.println(Log.INFO,"Backend Login", "Login unexpected error: " + response.body<Any?>().toString())
                val loginResultOk = response.body<LoginResultOk>()
                loginResultOk.token
            } catch (e: Exception) {
                // Handle the error (e.g., logging or returning default data)
                Log.println(Log.DEBUG,"Backend Login", "Error ${e.stackTraceToString()}")
                throw RuntimeException(e)
            }
        }
    Log.println(Log.DEBUG,"Backend Login", "result: $loginResult")
    return loginResult
}

suspend fun validateJWT(): String {
    val loginResult: String =
        withContext(Dispatchers.IO){
            try {
                val response = client.get("$backendUrl/api/users/validate") {
                    contentType(ContentType.Application.Json)
                }
                Log.println(Log.DEBUG,"validateJWT", "validateJWT response: $response")
                if (response.status.value == 400) Log.println(Log.INFO,"Backend Login", "Login unexpected error: " + response.body<Any?>().toString())
                "valid"
            } catch (e: Exception) {
                // Handle the error (e.g., logging or returning default data)
                Log.println(Log.DEBUG,"validateJWT", "Error ${e.stackTraceToString()}")
                //throw RuntimeException(e)
                ""
            }
        }
    Log.println(Log.DEBUG,"validateJWT", "result: $loginResult")
    return loginResult
}

suspend fun index(username: String): List<AssetResult> {
    val indexResultOk: List<AssetResult> =
        withContext(Dispatchers.IO){
            try {
                val response = client.get("$backendUrl/api/users/$username/assets") {
                    contentType(ContentType.Application.Json)
                }
                Log.println(Log.DEBUG,"Index", "Index response: $response")
                if (response.status.value == 400) Log.println(Log.DEBUG,"Backend Index", "Index unexpected error: " + response.body<Any?>().toString())
                val indexResultOk = response.body<IndexResultOk>()
                indexResultOk.data
            } catch (e: Exception) {
                // Handle the error (e.g., logging or returning default data)
                Log.println(Log.DEBUG,"Backend Index", "Error ${e.stackTraceToString()}")
                throw RuntimeException(e)
            }
        }
    Log.println(Log.DEBUG,"Backend Index", "result: $indexResultOk")
    return indexResultOk
}

suspend fun assetPost(username: String, latitude: String, longitude: String, set: String, tag: String): AssetResult {
    val assetPostResult: AssetResult =
        withContext(Dispatchers.IO){
            try {
                val response = client.post("$backendUrl/api/users/$username/assets") {
                    contentType(ContentType.Application.Json)
                    setBody(Asset(username, set, latitude, longitude, tag))
                }
                if (response.status.value == 400) Log.println(Log.DEBUG,"Backend Asset post", "Asset post unexpected error: " + response.body<Any?>().toString())
                val assetResultOk = response.body<AssetResultOk>()
                assetResultOk.data
            } catch (e: Exception) {
                // Handle the error (e.g., logging or returning default data)
                Log.println(Log.DEBUG,"Backend Asset Post", "Error ${e.stackTraceToString()}")
                throw RuntimeException(e)
            }
        }
    Log.println(Log.DEBUG,"Backend Asset Post", "result: $assetPostResult")
    return assetPostResult
}

suspend fun assetPatch(username: String, id: String, tag: String): AssetResult {
    Log.println(Log.DEBUG,"Backend Asset patch", "url: $backendUrl/api/users/$username/assets/$id")
    val assetPostResult: AssetResult =
        withContext(Dispatchers.IO) {
            try {
                val response = client.patch("$backendUrl/api/users/$username/assets/$id") {
                    contentType(ContentType.Application.Json)
                    setBody(Asset("", "", "", "", tag))
                }
                if (response.status.value == 400) Log.println(Log.DEBUG,"Backend Asset patch", "Asset post unexpected error: " + response.body<Any?>().toString())
                val assetResultOk = response.body<AssetResultOk>()
                assetResultOk.data
            } catch (e: Exception) {
                // Handle the error (e.g., logging or returning default data)
                Log.println(Log.DEBUG,"Backend Asset patch", "Error ${e.stackTraceToString()}")
                throw RuntimeException(e)
            }
        }
    Log.println(Log.DEBUG,"Backend Asset patch", "result: $assetPostResult")
    return assetPostResult
}
