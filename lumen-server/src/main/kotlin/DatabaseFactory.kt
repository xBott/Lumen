package me.bottdev.lumenserver

import com.typesafe.config.ConfigFactory
import me.bottdev.lumenserver.models.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        val config = ConfigFactory.load().getConfig("database")
        val url = config.getString("url")
        val driver = config.getString("driver")
        val user = config.getString("user")
        val password = config.getString("password")

        Database.connect(url, driver, user, password)

        transaction {
            SchemaUtils.create(Users)
        }
    }

}