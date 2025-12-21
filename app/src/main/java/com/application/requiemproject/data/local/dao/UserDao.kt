package com.application.requiemproject.data.local.dao

import androidx.annotation.Nullable
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.application.requiemproject.data.local.entities.User

@Dao
interface UserDao {

    @Nullable
    @Query(value = "SELECT * FROM user WHERE login = :inputLogin")
    suspend fun getUserByLogin(inputLogin: String): User?

    @Nullable
    @Query(value = "SELECT * FROM user WHERE email = :inputEmail")
    suspend fun getUserByEmail(inputEmail: String): User?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}