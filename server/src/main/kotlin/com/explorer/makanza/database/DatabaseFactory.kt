package com.explorer.makanza.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {
    fun init() {
        val jdbcUrl = System.getProperty("JDBC_DATABASE_URL")
            ?: System.getenv("JDBC_DATABASE_URL")
            ?: System.getenv("DATABASE_URL")
            ?: "jdbc:h2:file:./data/makanzadb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;AUTO_SERVER=TRUE"
        val driverClassName = if (jdbcUrl.contains("postgresql")) "org.postgresql.Driver" else "org.h2.Driver"
        val username = System.getenv("POSTGRES_USER") ?: System.getenv("DATABASE_USER") ?: "sa"
        val password = System.getenv("POSTGRES_PASSWORD") ?: System.getenv("DATABASE_PASSWORD") ?: ""

        if (jdbcUrl.contains("h2:file:")) {
            File("./data").mkdirs()
        }

        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.driverClassName = driverClassName
            this.username = username
            this.password = password
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                UsersTable,
                ApartmentsTable,
                ApartmentImagesTable,
                BookingsTable
            )
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
