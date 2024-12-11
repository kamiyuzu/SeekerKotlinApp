package com.seeker.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "AssetEntity")
data class AssetEntity(
    @PrimaryKey()
    @SerialName("id")
    val id: Int = 0,

    @SerialName("username")
    val username: String = "",

    @SerialName("set")
    val set: String = "",

    @SerialName("latitude")
    val latitude: String = "",

    @SerialName("longitude")
    val longitude: String = "",

    @SerialName("name")
    val name: String = "",

    @SerialName("description")
    val description: String = "",

    @SerialName("tag")
    val tag: String = "",
)