package com.example

import com.example.data.models.Book
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

suspend fun testListInsert(client: SupabaseClient, books: List<Book>) {
    val json = Json { encodeDefaults = true }
    val list = books.map { book -> 
        val map = json.encodeToJsonElement(book).jsonObject.toMutableMap()
        map.remove("id")
        map
    }
    client.postgrest["books"].insert(list)
}
