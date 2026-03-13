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


}