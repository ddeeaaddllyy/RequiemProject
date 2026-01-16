package com.application.requiemproject.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    var login: String,

    var email: String? = null,

    var password: String,

    var privilege: List<String>? = null

)