sealed class Event {
    data class Login(val username: String, val timestamp: Long) : Event()
    data class Purchase(val username: String, val amount: Double, val timestamp:
            Long) : Event()
    data class Logout(val username: String, val timestamp: Long) : Event()
}

fun List<Event>.filterByUser(username: String): List<Event> {
    return this.filter { event ->
        when (event){
            is Event.Login -> event.username == username
            is Event.Purchase -> event.username == username
            is Event.Logout -> event.username == username
        }
    }
}

fun List<Event>.totalSpent(username: String): Double {
    return this
        .filterIsInstance<Event.Purchase>()
        .filter { spent-> spent.username == username }
        .sumOf { spent -> spent.amount }
}

fun processEvents(events: List<Event>, handler: (Event) -> Unit) {
    for (event in events){
        handler(event)
    }
}

fun main() {
    val events = listOf(
        Event.Login("alice", 1_000),
        Event.Purchase("alice", 49.99, 1_100),
        Event.Purchase("bob", 19.99, 1_200),
        Event.Login("bob", 1_050),
        Event.Purchase("alice", 15.00, 1_300),
        Event.Logout("alice", 1_400),
        Event.Logout("bob", 1_500)
    )

    processEvents(events) { event ->
        when (event) {
            is Event.Login -> println(" [LOGIN] Utilizador ${event.username} iniciou sessão às ${event.timestamp}.")
            is Event.Purchase -> println(" [PURCHASE] Utilizador ${event.username} gastou ${event.amount} às ${event.timestamp}.")
            is Event.Logout -> println(" [LOGOUT] Utilizador ${event.username} deu logout às ${event.timestamp}.")

        }
    }

    val totalAlice = events.totalSpent("alice")
    val totalBob = events.totalSpent("bob")
    println("Total gasto pela alice: $totalAlice")
    println("Total gasto pelo bob: $totalBob")
}