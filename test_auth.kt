import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.runBlocking

fun main() {
    val client = createSupabaseClient("url", "key") { install(Auth) }
    runBlocking {
        val result = client.auth.signUpWith(Email) {
            email = "test@test.com"
            password = "test"
        }
        println(result)
    }
}
