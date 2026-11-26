package com.explorer.makanza

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        System.setProperty("JDBC_DATABASE_URL", "jdbc:h2:mem:apptestdb_${System.currentTimeMillis()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Makanza Apartment Marketplace API v1.0.0 is running.", response.bodyAsText())
    }
}
