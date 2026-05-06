package app
import annotations.Extract
abstract class DataProcessor(val input: String) {
    @Extract(regex = "Name: (\\w+)")
    abstract fun getName(): String?
    @Extract(regex = "Address: (.+)")
    abstract fun getAddress(): String?
}