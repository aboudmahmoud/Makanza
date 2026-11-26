package com.explorer.makanza.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.openApiRoutes() {
    get("/api/v1/openapi.json") {
        val openApiJson = """
        {
          "openapi": "3.0.3",
          "info": {
            "title": "Makanza Apartment Marketplace API",
            "description": "REST API for Makanza KMP Apartment Marketplace MVP",
            "version": "1.0.0"
          },
          "servers": [
            {
              "url": "http://localhost:8080",
              "description": "Local development server"
            }
          ],
          "paths": {
            "/api/v1/auth/register": {
              "post": {
                "summary": "Register a new user account",
                "responses": { "201": { "description": "Created" } }
              }
            },
            "/api/v1/auth/login": {
              "post": {
                "summary": "Authenticate user and receive JWT token",
                "responses": { "200": { "description": "Authenticated" } }
              }
            },
            "/api/v1/auth/me": {
              "get": {
                "summary": "Get authenticated user profile",
                "responses": { "200": { "description": "Success" } }
              }
            },
            "/api/v1/apartments": {
              "get": {
                "summary": "List and filter apartments with pagination",
                "responses": { "200": { "description": "Success" } }
              },
              "post": {
                "summary": "Publish a new apartment listing",
                "responses": { "201": { "description": "Created" } }
              }
            },
            "/api/v1/apartments/featured": {
              "get": {
                "summary": "Get featured apartments",
                "responses": { "200": { "description": "Success" } }
              }
            },
            "/api/v1/apartments/{id}": {
              "get": { "summary": "Get apartment details" },
              "put": { "summary": "Update apartment listing" },
              "delete": { "summary": "Delete apartment listing" }
            },
            "/api/v1/bookings": {
              "post": { "summary": "Create a rental booking request" }
            },
            "/api/v1/users/me/bookings": {
              "get": { "summary": "Get user's submitted booking requests" }
            },
            "/api/v1/users/me/booking-requests": {
              "get": { "summary": "Get booking requests received for user's apartments" }
            }
          }
        }
        """.trimIndent()

        call.respondText(openApiJson, ContentType.Application.Json)
    }
}
