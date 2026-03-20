package cm

fun main() {

    //a)
    val squares_1 = IntArray(50) {
        i -> (i + 1) * (i + 1)
    }

    //b)
    val squares_2 = (1..50).map { it * it }.toIntArray()

    //c)
    val squares_3 = Array(50) {
        i -> (i + 1) * (i + 1)
    }

    println(squares_1[0])
    println(squares_1[squares_1.size-1])
    println(squares_2[0])
    println(squares_2[squares_2.size-1])
    println(squares_3[0])
    println(squares_3[squares_3.size-1])

}