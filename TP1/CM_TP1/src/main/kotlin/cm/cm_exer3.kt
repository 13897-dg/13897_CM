package cm

fun main() {
    val valores = generateSequence(100.0) { anterior -> anterior * 0.60 }
        .takeWhile { it > 1.0 }       // a sequência para se o valor for <= 1
        .take(15)                  // primeiros 15 valores no máximo
        .map { "%.2f".format(it) }    // 2 casas decimais
        .toList()                     // Guarda tudo numa Lista

    println("Foram guardados ${valores.size} valores na lista:")
    println(valores)
}