package cm

fun main() {
    println("Bem-vindo à Calculadora Kotlin!")

    try {
        // 1. Pedir o primeiro número
        print("Digita o primeiro número inteiro: ")
        val input1 = readln()
        // O toIntOrNull() tenta converter para número. Se o utilizador escrever texto, devolve null.
        // O operador ?: (Elvis operator) atira uma Exception se o resultado for null.
        val numero1 = input1.toIntOrNull() ?: throw NumberFormatException("'$input1' não é um número válido!")

        // 2. Pedir a operação
        print("Digita a operação (+, -, *, /): ")
        val operacao = readln()

        // 3. Pedir o segundo número
        print("Digita o segundo número inteiro: ")
        val input2 = readln()
        val numero2 = input2.toIntOrNull() ?: throw NumberFormatException("'$input2' não é um número válido!")

        println("\nA calcular...\n")

        // O resto da lógica mantém-se igual, processando as variáveis inseridas
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

        // Operadores Booleanos
        val isPositivo = resultado > 0
        val isPar = resultado % 2 == 0
        val representacaoBooleana = isPositivo && isPar

        // Shift Left e Right
        val resultadoShiftLeft = resultado shl 1
        val resultadoShiftRight = resultado shr 1

        // String Templates e Formatting
        val textoFormatado = """
            |=== RESULTADOS DA CALCULADORA ===
            |Conta feita: $numero1 $operacao $numero2
            |
            |-> Decimal: $resultado
            |-> Hexadecimal: 0x%X
            |-> Booleano (É Positivo E Par?): $representacaoBooleana
            |
            |=== OPERAÇÕES DE BITS ===
            |-> Shift Left (<< 1): $resultadoShiftLeft
            |-> Shift Right (>> 1): $resultadoShiftRight
            |=================================
        """.trimMargin()

        println(textoFormatado.format(resultado))

    } catch (e: Exception) {
        // Apanha erros de divisão por zero, texto em vez de números, ou operadores errados
        println("ERRO APANHADO: ${e.message}")
        println("Por favor, corre o programa novamente e insere dados válidos.")
    }
}