package com.a13897.a21_duel.game

/**
 * Lógica pura do jogo — não depende de Android nem do Firebase.
 * Recebe estado, devolve novo estado ou resultado. Fácil de testar isoladamente.
 */
object MotorJogo {

    const val VIDAS_INICIAIS = 8
    const val OBJECTIVO_PADRAO = 21
    const val TIMER_RONDA_SEGUNDOS = 60
    const val SEGUNDOS_RETIRADOS_MASSACRE = 15

    /** Calcula a pontuação total de uma mão (soma simples das cartas). */
    fun calcularPontuacao(mao: List<Int>): Int = mao.sum()

    /**
     * Decide o vencedor de uma ronda quando ambos os jogadores deram stay.
     * Devolve null em caso de empate (ninguém perde vidas; nova ronda).
     * Empate ocorre quando ambos têm a mesma pontuação válida, ou quando
     * ambos ultrapassam o objectivo ("double bust").
     */
    fun decidirVencedorRonda(
        pontosJogador1: Int,
        pontosJogador2: Int,
        objectivo: Int
    ): ResultadoRonda {
        val j1Valido = pontosJogador1 <= objectivo
        val j2Valido = pontosJogador2 <= objectivo

        return when {
            j1Valido && !j2Valido -> ResultadoRonda.VenceJogador1
            j2Valido && !j1Valido -> ResultadoRonda.VenceJogador2
            j1Valido && j2Valido -> {
                when {
                    pontosJogador1 > pontosJogador2 -> ResultadoRonda.VenceJogador1
                    pontosJogador2 > pontosJogador1 -> ResultadoRonda.VenceJogador2
                    else -> ResultadoRonda.Empate
                }
            }
            else -> ResultadoRonda.Empate // os dois ultrapassaram o objectivo
        }
    }

    /** Aplica o efeito das cartas de objectivo "até X" — substitui qualquer "até X" anterior. */
    fun aplicarCartaAteX(novoObjectivoBase: Int): Int = novoObjectivoBase

    /** Aplica "mais um" / "menos um" — acumula sobre o objectivo actual. */
    fun aplicarAjusteObjectivo(objectivoActual: Int, ajuste: Int): Int {
        return objectivoActual + ajuste
    }

    /** Determina se o baralho ainda tem cartas disponíveis para "pedir carta". */
    fun baralhoTemCartas(cartasDisponiveis: List<Int>): Boolean {
        return cartasDisponiveis.isNotEmpty()
    }

    /**
     * "Sorte do dealer" — devolve a melhor carta disponível para chegar
     * o mais perto possível do objectivo actual sem ultrapassar.
     * Se nenhuma carta couber sem ultrapassar, devolve a mais baixa disponível.
     */
    fun melhorCartaParaObjectivo(
        pontuacaoActual: Int,
        objectivo: Int,
        cartasDisponiveis: List<Int>
    ): Int? {
        if (cartasDisponiveis.isEmpty()) return null
        val maximoUtil = objectivo - pontuacaoActual
        return cartasDisponiveis
            .filter { it <= maximoUtil }
            .maxOrNull()
            ?: cartasDisponiveis.minOrNull()
    }

    /** Aplica perda de vidas ao perdedor da ronda (nunca desce abaixo de 0). */
    fun aplicarPerdaVidas(vidasActuais: Int, aposta: Int): Int {
        return (vidasActuais - aposta).coerceAtLeast(0)
    }

    /** Calcula a aposta da próxima ronda (regra actual: +1 por ronda). */
    fun calcularProximaAposta(apostaActual: Int): Int = apostaActual + 1

    /** Verifica se um jogador já perdeu o jogo (chegou a 0 vidas). */
    fun jogoTerminou(vidas: Int): Boolean = vidas <= 0

    /** Remove uma carta do baralho disponível (quando é "pedida" por um jogador). */
    fun removerCartaDoBaralho(cartasDisponiveis: List<Int>, carta: Int): List<Int> {
        val novaLista = cartasDisponiveis.toMutableList()
        novaLista.remove(carta)
        return novaLista
    }

    /** Insere uma carta de volta ao baralho numa posição aleatória (usado por "Retorno"/"Remove"). */
    fun inserirCartaAleatoria(cartasDisponiveis: List<Int>, carta: Int): List<Int> {
        val novaLista = cartasDisponiveis.toMutableList()
        val posicao = (0..novaLista.size).random()
        novaLista.add(posicao, carta)
        return novaLista
    }

    /** Gera 4 cartas aleatórias (1-11) para adicionar ao baralho — usado por "Mais quatro". */
    fun gerarCartasAleatorias(quantidade: Int = 4): List<Int> {
        return (1..quantidade).map { (1..11).random() }
    }
}

/** Resultado possível do fim de uma ronda. */
sealed class ResultadoRonda {
    object VenceJogador1 : ResultadoRonda()
    object VenceJogador2 : ResultadoRonda()
    object Empate : ResultadoRonda()
}
