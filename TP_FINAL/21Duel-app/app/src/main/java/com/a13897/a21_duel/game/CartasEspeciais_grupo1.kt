package com.a13897.a21_duel.game

// ---------------------------------------------------------------------------
// Cartas que afectam a APOSTA (Escudo, Escudo+, Espada, Espada+, Massacre)
// ---------------------------------------------------------------------------

object Escudo : CartaEspecialJogo("Escudo", TipoEfeitoCarta.PASSIVO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        return estado.copy(aposta = (estado.aposta - 1).coerceAtLeast(0))
    }
}

object EscudoMais : CartaEspecialJogo("Escudo+", TipoEfeitoCarta.PASSIVO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        return estado.copy(aposta = (estado.aposta - 2).coerceAtLeast(0))
    }
}

object Espada : CartaEspecialJogo("Espada", TipoEfeitoCarta.PASSIVO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        return estado.copy(aposta = estado.aposta + 1)
    }
}

object EspadaMais : CartaEspecialJogo("Espada+", TipoEfeitoCarta.PASSIVO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        return estado.copy(aposta = estado.aposta + 2)
    }
}

/**
 * Massacre — aumenta aposta +1, dá carta especial extra a quem jogou,
 * e retira segundos ao timer. Aqui só aplicamos a parte que afecta o EstadoRonda
 * (aposta + timer); a entrega da carta extra é tratada fora, pelo ViewModel,
 * porque envolve o inventário do jogador, que não vive no EstadoRonda.
 */
object Massacre : CartaEspecialJogo("Massacre", TipoEfeitoCarta.PASSIVO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        val novoTempo = (estado.tempoRestanteSegundos - MotorJogo.SEGUNDOS_RETIRADOS_MASSACRE)
            .coerceAtLeast(0)
        return estado.copy(
            aposta = estado.aposta + 1,
            tempoRestanteSegundos = novoTempo
        )
    }
}

// ---------------------------------------------------------------------------
// Cartas que dão cartas numeradas (Shhh, Carta 2-7, Sorte do dealer)
// ---------------------------------------------------------------------------

/** Shhh — recebe uma carta virada para baixo (a carta em si é escolhida ao acaso do baralho). */
object Shhh : CartaEspecialJogo("Shhh", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        if (!MotorJogo.baralhoTemCartas(estado.cartasDisponiveis)) return estado

        val carta = estado.cartasDisponiveis.random()
        val novoBaralho = MotorJogo.removerCartaDoBaralho(estado.cartasDisponiveis, carta)
        val novaCarta = CartaMao(valor = carta, visivel = false)

        return adicionarCartaAMao(estado, quemJogou, novaCarta).copy(cartasDisponiveis = novoBaralho)
    }
}

/**
 * Carta X (2 a 7) — recebe a carta com o número correspondente.
 * Se essa carta já estiver em jogo (não disponível no baralho), não faz nada.
 * Implementada como classe parametrizada porque são 6 cartas quase idênticas.
 */
class CartaNumerada(private val valor: Int) : CartaEspecialJogo("Carta $valor", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        if (valor !in estado.cartasDisponiveis) return estado // já está em campo, não faz nada

        val novoBaralho = MotorJogo.removerCartaDoBaralho(estado.cartasDisponiveis, valor)
        val novaCarta = CartaMao(valor = valor, visivel = true)

        return adicionarCartaAMao(estado, quemJogou, novaCarta).copy(cartasDisponiveis = novoBaralho)
    }
}

val Carta2 = CartaNumerada(2)
val Carta3 = CartaNumerada(3)
val Carta4 = CartaNumerada(4)
val Carta5 = CartaNumerada(5)
val Carta6 = CartaNumerada(6)
val Carta7 = CartaNumerada(7)

/** Sorte do dealer — recebe a melhor carta disponível para o objectivo actual. */
object SorteDoDealer : CartaEspecialJogo("Sorte do dealer", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        val pontuacaoActual = if (quemJogou == "jogador1") estado.pontuacaoJogador1() else estado.pontuacaoJogador2()
        val carta = MotorJogo.melhorCartaParaObjectivo(
            pontuacaoActual, estado.objectivoActual, estado.cartasDisponiveis
        ) ?: return estado

        val novoBaralho = MotorJogo.removerCartaDoBaralho(estado.cartasDisponiveis, carta)
        val novaCarta = CartaMao(valor = carta, visivel = true)

        return adicionarCartaAMao(estado, quemJogou, novaCarta).copy(cartasDisponiveis = novoBaralho)
    }
}

/** Função auxiliar partilhada: adiciona uma carta à mão do jogador correcto. */
internal fun adicionarCartaAMao(estado: EstadoRonda, jogador: String, carta: CartaMao): EstadoRonda {
    return if (jogador == "jogador1") {
        estado.copy(maoJogador1 = estado.maoJogador1 + carta)
    } else {
        estado.copy(maoJogador2 = estado.maoJogador2 + carta)
    }
}
