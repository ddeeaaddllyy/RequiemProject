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

    // -1 - deleted user
    // 0 - just user without premium
    // 1 - user with premium
    // 2 - moderator (idk why it is needed)
    var privilege: Short = 0

)