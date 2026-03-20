package cm

fun main() {
    println("Calculadora Kotlin")

    try {
        print("Digita o primeiro número inteiro: ")
        val input1 = readln()
        val numero1 = input1.toIntOrNull() ?: throw NumberFormatException("'$input1' não é um número válido!")

        print("Digita a operação (+, -, *, /): ")
        val operacao = readln()

        print("Digita o segundo número inteiro: ")
        val input2 = readln()
        val numero2 = input2.toIntOrNull() ?: throw NumberFormatException("'$input2' não é um número válido!")

        val resultado = when (operacao) {
            "+" -> numero1 + numero2
            "-" -> numero1 - numero2
            "*" -> numero1 * numero2
            "/" -> {
                if (numero2 == 0) {
                    throw ArithmeticException("Impossível dividir por zero!")
                }
                numero1 / numero2
            }
            else -> throw IllegalArgumentException("Operação inválida: $operacao")
        }

        val resultadoShiftLeft = resultado shl 1
        val resultadoShiftRight = resultado shr 1
        val resultadohexadecimal = Integer.toHexString(resultado)

        val textoFormatado = """
            
            Resultado: $resultado
            Resultado hexa: $resultadohexadecimal
            Shift left: $resultadoShiftLeft
            Shift right: $resultadoShiftRight
            """

        println(textoFormatado.format(resultado))

    } catch (e: Exception) {
                println("ERRO: ${e.message}")
        println("Por favor, corre o programa novamente e insere dados válidos.")
    }
}