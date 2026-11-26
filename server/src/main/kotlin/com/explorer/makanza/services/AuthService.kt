package com.explorer.makanza.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.explorer.makanza.database.DatabaseFactory.dbQuery
import com.explorer.makanza.database.UsersTable
import com.explorer.makanza.models.*
import com.explorer.makanza.plugins.JwtConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.Date
import java.util.UUID

class AuthService(
    private val jwtSecret: String = JwtConfig.secret,
    private val jwtIssuer: String = "makanza-api",
    private val jwtAudience: String = "makanza-users"
) {

    private val algorithm = Algorithm.HMAC256(jwtSecret)

    fun generateToken(userId: String, email: String, role: UserRole): String {
        val expirationTime = Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000) // 30 days
        return JWT.create()
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("role", role.name)
            .withExpiresAt(expirationTime)
            .sign(algorithm)
    }

    suspend fun register(req: RegisterRequest): AuthResponse {
        val normalizedEmail = req.email.trim().lowercase()

        val existingUser = dbQuery {
            UsersTable.selectAll().where(UsersTable.email eq normalizedEmail).singleOrNull()
        }
        if (existingUser != null) {
            throw IllegalArgumentException("A user with this email address already exists")
        }

        if (req.password != req.confirmPassword) {
            throw IllegalArgumentException("Passwords do not match")
        }

        if (req.password.length < 6) {
            throw IllegalArgumentException("Password must be at least 6 characters long")
        }

        // Prevent public self-registration as ADMIN
        val assignedRole = if (req.role == UserRole.ADMIN) UserRole.STUDENT else req.role

        val hashedPassword = withContext(Dispatchers.Default) {
            BCrypt.withDefaults().hashToString(10, req.password.toCharArray())
        }
        val userId = UUID.randomUUID().toString()

        dbQuery {
            UsersTable.insert {
                it[id] = userId
                it[fullName] = req.fullName.trim()
                it[email] = normalizedEmail
                it[phone] = req.phone.trim()
                it[passwordHash] = hashedPassword
                it[role] = assignedRole.name
            }
        }

        val userDto = getUserById(userId) ?: throw IllegalStateException("User creation failed")
        val token = generateToken(userId, normalizedEmail, userDto.role)
        return AuthResponse(token = token, user = userDto)
    }

    suspend fun login(req: LoginRequest): AuthResponse {
        val normalizedEmail = req.email.trim().lowercase()

        val row = dbQuery {
            UsersTable.selectAll().where(UsersTable.email eq normalizedEmail).singleOrNull()
        } ?: throw IllegalArgumentException("Invalid email or password")

        val storedHash = row[UsersTable.passwordHash]
        val result = withContext(Dispatchers.Default) {
            BCrypt.verifyer().verify(req.password.toCharArray(), storedHash)
        }
        if (!result.verified) {
            throw IllegalArgumentException("Invalid email or password")
        }

        val userId = row[UsersTable.id]
        val userRole = UserRole.valueOf(row[UsersTable.role])

        val userDto = UserDto(
            id = userId,
            fullName = row[UsersTable.fullName],
            email = row[UsersTable.email],
            phone = row[UsersTable.phone],
            role = userRole,
            createdAt = row[UsersTable.createdAt].toString()
        )

        val token = generateToken(userId, normalizedEmail, userRole)
        return AuthResponse(token = token, user = userDto)
    }

    suspend fun getUserById(userId: String): UserDto? = dbQuery {
        UsersTable.selectAll().where(UsersTable.id eq userId)
            .singleOrNull()
            ?.let { row ->
                UserDto(
                    id = row[UsersTable.id],
                    fullName = row[UsersTable.fullName],
                    email = row[UsersTable.email],
                    phone = row[UsersTable.phone],
                    role = UserRole.valueOf(row[UsersTable.role]),
                    createdAt = row[UsersTable.createdAt].toString()
                )
            }
    }

    suspend fun getAllUsers(): List<UserDto> = dbQuery {
        UsersTable.selectAll().map { row ->
            UserDto(
                id = row[UsersTable.id],
                fullName = row[UsersTable.fullName],
                email = row[UsersTable.email],
                phone = row[UsersTable.phone],
                role = UserRole.valueOf(row[UsersTable.role]),
                createdAt = row[UsersTable.createdAt].toString()
            )
        }
    }

    suspend fun updateProfile(userId: String, req: UpdateProfileRequest): UserDto {
        dbQuery {
            UsersTable.update({ UsersTable.id eq userId }) {
                it[fullName] = req.fullName.trim()
                it[phone] = req.phone.trim()
            }
        }
        return getUserById(userId) ?: throw IllegalStateException("User profile update failed")
    }
}
