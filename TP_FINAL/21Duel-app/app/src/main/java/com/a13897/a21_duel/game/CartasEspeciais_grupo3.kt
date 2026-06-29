package com.a13897.a21_duel.game

// ---------------------------------------------------------------------------
// Cartas de MANIPULAÇÃO do baralho (Retorno, Remove, Renovar, Mais quatro)
// ---------------------------------------------------------------------------

/** Retorno — devolve a tua última carta ao baralho em posição aleatória. */
object Retorno : CartaEspecialJogo("Retorno", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        val mao = if (quemJogou == "jogador1") estado.maoJogador1 else estado.maoJogador2
        val ultimaCarta = mao.lastOrNull() ?: return estado

        val novaMao = mao.dropLast(1)
        val novoBaralho = MotorJogo.inserirCartaAleatoria(estado.cartasDisponiveis, ultimaCarta.valor)

        val estadoComMaoActualizada = if (quemJogou == "jogador1") {
            estado.copy(maoJogador1 = novaMao)
        } else {
            estado.copy(maoJogador2 = novaMao)
        }
        return estadoComMaoActualizada.copy(cartasDisponiveis = novoBaralho)
    }
}

/** Remove — devolve a última carta do oponente ao baralho em posição aleatória. */
object Remove : CartaEspecialJogo("Remove", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        val oponente = oponenteDe(quemJogou)
        val maoOponente = if (oponente == "jogador1") estado.maoJogador1 else estado.maoJogador2
        val ultimaCarta = maoOponente.lastOrNull() ?: return estado

        val novaMaoOponente = maoOponente.dropLast(1)
        val novoBaralho = MotorJogo.inserirCartaAleatoria(estado.cartasDisponiveis, ultimaCarta.valor)

        val estadoComMaoActualizada = if (oponente == "jogador1") {
            estado.copy(maoJogador1 = novaMaoOponente)
        } else {
            estado.copy(maoJogador2 = novaMaoOponente)
        }
        return estadoComMaoActualizada.copy(cartasDisponiveis = novoBaralho)
    }
}

/**
 * Renovar — devolve todas as tuas cartas ao baralho e recebe 2 novas
 * (uma virada para baixo, como no início da ronda).
 */
object Renovar : CartaEspecialJogo("Renovar", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        val mao = if (quemJogou == "jogador1") estado.maoJogador1 else estado.maoJogador2

        // devolve todas as cartas actuais ao baralho, cada uma em posição aleatória
        var novoBaralho = estado.cartasDisponiveis
        mao.forEach { carta ->
            novoBaralho = MotorJogo.inserirCartaAleatoria(novoBaralho, carta.valor)
        }

        if (novoBaralho.size < 2) {
            // não há cartas suficientes para dar 2 novas — devolve só o baralho actualizado, mão vazia
            return aplicarMaoVazia(estado, quemJogou, novoBaralho)
        }

        val cartaVisivel = novoBaralho.random()
        novoBaralho = MotorJogo.removerCartaDoBaralho(novoBaralho, cartaVisivel)
        val cartaOculta = novoBaralho.random()
        novoBaralho = MotorJogo.removerCartaDoBaralho(novoBaralho, cartaOculta)

        val novaMao = listOf(
            CartaMao(valor = cartaVisivel, visivel = true),
            CartaMao(valor = cartaOculta, visivel = false)
        )

        return if (quemJogou == "jogador1") {
            estado.copy(maoJogador1 = novaMao, cartasDisponiveis = novoBaralho)
        } else {
            estado.copy(maoJogador2 = novaMao, cartasDisponiveis = novoBaralho)
        }
    }

    private fun aplicarMaoVazia(estado: EstadoRonda, jogador: String, baralho: List<Int>): EstadoRonda {
        return if (jogador == "jogador1") {
            estado.copy(maoJogador1 = emptyList(), cartasDisponiveis = baralho)
        } else {
            estado.copy(maoJogador2 = emptyList(), cartasDisponiveis = baralho)
        }
    }
}

/** Mais quatro — adiciona 4 cartas aleatórias (1-11) ao baralho em posições aleatórias. */
object MaisQuatro : CartaEspecialJogo("Mais quatro", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        var novoBaralho = estado.cartasDisponiveis
        val novasCartas = MotorJogo.gerarCartasAleatorias(4)
        novasCartas.forEach { carta ->
            novoBaralho = MotorJogo.inserirCartaAleatoria(novoBaralho, carta)
        }
        return estado.copy(cartasDisponiveis = novoBaralho)
    }
}

// ---------------------------------------------------------------------------
// Cartas de CONDIÇÃO (Resistente, Super 8) — não alteram o EstadoRonda directamente,
// são verificadas em momentos específicos do fim da ronda/partida pelo ViewModel.
// ---------------------------------------------------------------------------

/**
 * Resistente — se quem a jogou chegar a 0 vidas no fim da ronda, recebe 1 vida.
 * Esta carta não tem efeito imediato sobre o EstadoRonda: fica registada em
 * "cartasEspeciaisEmCampo" e é o ViewModel que, ao calcular a perda de vidas
 * no fim da ronda, verifica se esta carta está activa para o jogador que perdeu.
 */
object Resistente : CartaEspecialJogo("Resistente", TipoEfeitoCarta.PASSIVO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        return estado // efeito condicional tratado no ViewModel, no momento da perda de vidas
    }
}

/**
 * Super 8 — se quem a jogou tiver o 8 na mão e ganhar a ronda, dá +2 de dano extra.
 * Tal como o Resistente, é uma condição verificada no fim da ronda pelo ViewModel
 * (precisa de saber o vencedor da ronda, que só é calculado depois de ambos darem stay).
 */
object Super8 : CartaEspecialJogo("Super 8", TipoEfeitoCarta.PASSIVO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        return estado // efeito condicional tratado no ViewModel, no momento de aplicar dano
    }
}

/**
 * Renasce — apaga a carta especial mais recente da mesa e recebe uma carta especial.
 * A parte "apaga a carta mais recente da mesa" pode referir-se à própria carta deste
 * jogador ou à do oponente — usamos a última carta em campo, seja de quem for.
 * A parte "recebe uma carta especial" é tratada pelo ViewModel (inventário).
 */
object Renasce : CartaEspecialJogo("Renasce", TipoEfeitoCarta.IMEDIATO) {
    override fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda {
        val ultimaCartaEmCampo = estado.cartasEspeciaisEmCampo.lastOrNull() ?: return estado
        return estado.copy(
            cartasEspeciaisEmCampo = estado.cartasEspeciaisEmCampo - ultimaCartaEmCampo
        )
    }
}
