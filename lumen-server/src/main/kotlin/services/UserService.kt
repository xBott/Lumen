package me.bottdev.lumenserver.services

import me.bottdev.lumenserver.logger
import me.bottdev.lumenserver.models.User
import me.bottdev.lumenserver.repositories.UserRepository
import me.bottdev.lumenserver.utils.verify

object UserService {

    fun authenticate(username: String, password: String): User? {
        val user = UserRepository.findByUsername(username) ?: return null
        logger.info("Trying to authenticate as $username...")
        return if (verify(password, user.password)) user else null
    }

}