package me.bottdev.lumenserver.models

import org.jetbrains.exposed.sql.Table
import java.util.*

object Users : Table() {
    val id = uuid("id").autoGenerate().uniqueIndex()
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 255)
}

data class User(
    val id: UUID,
    val username: String,
    val password: String
)