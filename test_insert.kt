package com.example

import com.example.data.models.Book
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

suspend fun testInsert(client: SupabaseClient, book: Book) {
    val json = Json { encodeDefaults = true }
    val bookMap = json.encodeToJsonElement(book).jsonObject.toMutableMap()
    bookMap.remove("id")
    client.postgrest["books"].insert(bookMap) // or insert(JsonObject(bookMap))
}
