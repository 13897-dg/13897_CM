import kotlin.math.sqrt

data class Vec2(val x: Double, val y: Double) : Comparable<Vec2> {

    operator fun plus(other: Vec2): Vec2 = Vec2(x + other.x, y + other.y)

    operator fun minus(other: Vec2): Vec2 = Vec2(x - other.x, y - other.y)

    operator fun times(scalar: Double): Vec2 = Vec2(x * scalar, y * scalar)

    operator fun unaryMinus(): Vec2 = Vec2(-x, -y)

    fun magnitude(): Double = sqrt(x * x + y * y)

    fun dot(other: Vec2): Double = (x * other.x) + (y * other.y)

    fun normalized(): Vec2 {
        val mag = magnitude()
        if (mag == 0.0) throw IllegalStateException("Cannot normalize a zero vector.")
        return Vec2(x / mag, y / mag)
    }
    override operator fun compareTo(other: Vec2): Int {
        return this.magnitude().compareTo(other.magnitude())
    }

    operator fun get(index: Int): Double {
        return when (index) {
            0 -> x
            1 -> y
            else -> throw IndexOutOfBoundsException("Invalid index $index for Vec2")
        }
    }
}

fun main() {
    val a = Vec2(3.0, 4.0)
    val b = Vec2(1.0, 2.0)

    println("a = $a") // a = Vec2(x=3.0, y=4.0)
    println("b = $b") // b = Vec2(x=1.0, y=2.0)
    println("a + b = ${a + b}") // a + b = Vec2(x=4.0, y=6.0)
    println("a - b = ${a - b}") // a - b = Vec2(x=2.0, y=2.0)
    println("a * 2.0 = ${a * 2.0}") // a * 2.0 = Vec2(x=6.0, y=8.0)
    println("-a = ${-a}") // -a = Vec2(x=-3.0, y=-4.0)
    println("|a| = ${a.magnitude()}") // |a| = 5.0
    println("a dot b = ${a.dot(b)}") // a dot b = 11.0
    println("norm(a) = ${a.normalized()}") // norm(a) = Vec2(x=0.6, y=0.8)
    println("a[0] = ${a[0]}") // a[0] = 3.0
    println("a[1] = ${a[1]}") // a[1] = 4.0
    println("a > b = ${a > b}") // a > b = true
    println("a < b = ${a < b}") // a < b = false

    val vectors = listOf(Vec2(1.0, 0.0), Vec2(3.0, 4.0), Vec2(0.0, 2.0))
    println("Longest = ${vectors.maxOrNull()}") // Longest = Vec2(x=3.0, y=4.0)
    println("Shortest = ${vectors.minOrNull()}") // Shortest = Vec2(x=1.0, y=0.0)
}