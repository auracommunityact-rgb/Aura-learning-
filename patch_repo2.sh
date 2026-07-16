cat << 'INNER_EOF' >> app/src/main/java/com/example/data/repository/AuraRepository.kt

    fun subscribeToCourses(onChanged: () -> Unit) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val channel = client.io.github.jan.supabase.realtime.realtime.channel("courses-updates")
                val changes = channel.io.github.jan.supabase.realtime.postgresChangeFlow<io.github.jan.supabase.realtime.PostgresAction>("public") {
                    table = "courses"
                }
                kotlinx.coroutines.launch {
                    changes.collect {
                        onChanged()
                    }
                }
                channel.subscribe()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
INNER_EOF
