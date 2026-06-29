package com.a13897.a21_duel.game

// ---------------------------------------------------------------------------
// Cartas de OBJECTIVO (Até 17, Até 24, Até 27, Mais um, Menos um)
// ---------------------------------------------------------------------------

/**
 * Cartas "até X" — só pode haver uma activa de cada vez; a mais recente
 * substitui a anterior. Afecta o objectivoBase (não o ajuste acumulado).
 */
class CartaAteX(private val valorObjectivo: Int) :
    CartaEspecialJogo("Até $valorObjectivo", TipoEfeitoCarta.PASSIVO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        return estado.copy(
            objectivoBase = valorObjectivo,
            cartaAteXActiva = nome
        )
    }
}

val Ate17 = CartaAteX(17)
val Ate24 = CartaAteX(24)
val Ate27 = CartaAteX(27)

/** Mais um / Menos um — acumulam entre si e aplicam-se sobre o objectivo actual. */
object MaisUm : CartaEspecialJogo("Mais um", TipoEfeitoCarta.PASSIVO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        return estado.copy(ajusteObjectivo = estado.ajusteObjectivo + 1)
    }
}

object MenosUm : CartaEspecialJogo("Menos um", TipoEfeitoCarta.PASSIVO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        return estado.copy(ajusteObjectivo = estado.ajusteObjectivo - 1)
    }
}

// ---------------------------------------------------------------------------
// Cartas de MANIPULAÇÃO de mãos (Troca, Vai à pesca, Destroi, Copia, Amizade)
// ---------------------------------------------------------------------------

/** Troca — troca a carta mais recente dos dois jogadores. */
object Troca : CartaEspecialJogo("Troca", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        val ultimaJogador1 = estado.maoJogador1.lastOrNull() ?: return estado
        val ultimaJogador2 = estado.maoJogador2.lastOrNull() ?: return estado

        val novaMao1 = estado.maoJogador1.dropLast(1) + ultimaJogador2
        val novaMao2 = estado.maoJogador2.dropLast(1) + ultimaJogador1

        return estado.copy(maoJogador1 = novaMao1, maoJogador2 = novaMao2)
    }
}

/** Vai à pesca — obriga o oponente a ir buscar 1 carta. Não funciona sem cartas disponíveis. */
object VaiAPesca : CartaEspecialJogo("Vai à pesca", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        if (!MotorJogo.baralhoTemCartas(estado.cartasDisponiveis)) return estado

        val oponente = oponenteDe(quemJogou)
        val carta = estado.cartasDisponiveis.random()
        val novoBaralho = MotorJogo.removerCartaDoBaralho(estado.cartasDisponiveis, carta)
        val novaCarta = CartaMao(valor = carta, visivel = true)

        return adicionarCartaAMao(estado, oponente, novaCarta).copy(cartasDisponiveis = novoBaralho)
    }
}

/**
 * Destroi — destrói a última carta especial usada pelo oponente.
 * Os efeitos passivos dessa carta cessam imediatamente (deixa de constar em "cartasEspeciaisEmCampo").
 * Nota: a reversão do efeito numérico (ex: retirar o +1 de aposta que essa carta tinha dado)
 * é tratada pelo ViewModel, que recalcula o estado a partir das cartas activas remanescentes,
 * em vez de tentar desfazer manualmente cada efeito aqui.
 */
object Destroi : CartaEspecialJogo("Destroi", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        val ultimaCartaOponente = estado.cartasEspeciaisEmCampo.lastOrNull() ?: return estado
        return estado.copy(
            cartasEspeciaisEmCampo = estado.cartasEspeciaisEmCampo - ultimaCartaOponente
        )
    }
}

/** Copia — recebe uma carta igual à última que o oponente recebeu. */
object Copia : CartaEspecialJogo("Copia", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        val oponente = oponenteDe(quemJogou)
        val maoOponente = if (oponente == "jogador1") estado.maoJogador1 else estado.maoJogador2
        val ultimaCartaOponente = maoOponente.lastOrNull() ?: return estado

        val novaCarta = CartaMao(valor = ultimaCartaOponente.valor, visivel = true)
        return adicionarCartaAMao(estado, quemJogou, novaCarta)
    }
}

/**
 * Amizade — ambos os jogadores recebem +2 cartas especiais.
 * Tal como o Massacre, isto afecta o INVENTÁRIO (fora do EstadoRonda),
 * por isso esta função não altera nada aqui — é tratada directamente pelo ViewModel.
 */
object Amizade : CartaEspecialJogo("Amizade", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        return estado // sem efeito sobre o EstadoRonda; tratado no ViewModel
    }
}
