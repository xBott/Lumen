package me.bottdev.lumenserver.repositories

import me.bottdev.lumenserver.models.User
import me.bottdev.lumenserver.models.Users
import me.bottdev.lumenserver.utils.hash
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object UserRepository {

    fun contains(username: String): Boolean = Users.select { Users.username eq username }.count() > 0

    fun register(username: String, password: String): User? = transaction {

        if (contains(username)) return@transaction null

        val id = UUID.randomUUID()
        val hashedPassword = hash(password)

        Users.insert {
            it[this.id] = id
            it[this.username] = username
            it[this.password] = hashedPassword
        }

        User(id, username, hashedPassword)
    }

    fun findByUsername(username: String): User? = transaction {
        Users.select { Users.username eq username }
            .map {
                User(
                    it[Users.id],
                    it[Users.username],
                    it[Users.password]
                )
            }
            .singleOrNull()
    }

}