class Cache<K : Any, V : Any> {
    private val storage = mutableMapOf<K, V>()
    fun put(key: K, value: V) {
        storage.put(key, value)
    }
    fun get(key: K): V? {
        return storage[key]
    }
    fun evict(key: K) {
        storage.remove(key)
    }
    fun size(): Int {
        return storage.size
    }
    fun getOrPut(key: K, default: () -> V): V {
        return storage.getOrPut(key, default)
    }
    fun transform(key: K, action: (V) -> V): Boolean {
        val currentValue = storage[key]

        if (currentValue != null) {

            val newValue = action(currentValue)
            storage.put(key, newValue)
            return true

        } else {
            return false
        }
    }
    fun snapshot(): Map<K, V> {
        return storage.toMap()
    }
}
fun main() {

    println("Word frequency cache")
    val wordCache = Cache<String, Int>()

    wordCache.put("kotlin", 1)
    wordCache.put("scala", 1)
    wordCache.put("haskell", 1)

    println("Size: ${wordCache.size()}")
    println("Frequency of \"kotlin\": ${wordCache.get("kotlin")}")
    println("getOrPut \"kotlin\": ${wordCache.getOrPut("kotlin") { 0 }}")
    println("getOrPut \"java\": ${wordCache.getOrPut("java") { 0 }}")
    println("Size after getOrPut: ${wordCache.size()}")

    println("Transform \"kotlin\" (+1): ${wordCache.transform("kotlin") { it + 1 }}")
    println("Transform \"cobol\" (+1): ${wordCache.transform("cobol") { it + 1 }}")

    println("Snapshot: ${wordCache.snapshot()}")

    println("\nId registry cache")

    val idCache = Cache<Int, String>()
    idCache.put(1, "Alice")
    idCache.put(2, "Bob")
    println("Id 1 -> ${idCache.get(1)}")
    println("Id 2 -> ${idCache.get(2)}")
    idCache.evict(1)
    println("After evict id 1, size: ${idCache.size()}")
    println("Id 1 after evict -> ${idCache.get(1)}")
}
