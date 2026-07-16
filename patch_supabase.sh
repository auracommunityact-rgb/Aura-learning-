awk '
/val client = createSupabaseClient/ {
    print "    val client = createSupabaseClient("
    print "        supabaseUrl = BuildConfig.SUPABASE_URL,"
    print "        supabaseKey = BuildConfig.SUPABASE_ANON_KEY"
    print "    ) {"
    print "        defaultSerializer = io.github.jan.supabase.serializer.KotlinXSerializer(kotlinx.serialization.json.Json {"
    print "            ignoreUnknownKeys = true"
    print "            coerceInputValues = true"
    print "        })"
    print "        install(Postgrest)"
    print "        install(Auth)"
    print "        install(Storage)"
    print "        install(Realtime)"
    print "    }"
    skip = 1
    next
}
skip == 1 && /}/ {
    skip = 0
    next
}
skip == 1 { next }
{ print }
' app/src/main/java/com/example/data/supabase/SupabaseService.kt > temp.kt
mv temp.kt app/src/main/java/com/example/data/supabase/SupabaseService.kt
