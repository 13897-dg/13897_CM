sealed class Event {
    data class Login(val username: String, val timestamp: Long) : Event()
    data class Purchase(val username: String, val amount: Double, val timestamp:
            Long) : Event()
    data class Logout(val username: String, val timestamp: Long) : Event()
}