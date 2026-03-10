package cm

fun main() {
    // 1. generateSequence com o valor inicial (100.0)
    // 2. A cada iteração, multiplicamos por 0.60 (reduzindo assim 40%)
    val valores = generateSequence(100.0) { anterior -> anterior * 0.60 }
        .takeWhile { it > 1.0 }       // Garante que a sequência para se o valor for <= 1
        .take(15)                     // Pega apenas nos primeiros 15 valores no máximo
        .map { "%.2f".format(it) }    // Formata cada número para ter apenas 2 casas decimais
        .toList()                     // Guarda tudo numa Lista final

    // Mostrar os resultados na consola
    println("Foram guardados ${valores.size} valores na lista:")
    println(valores)
}