package com.a13897.a21_duel.game

/**
 * Registo central de todas as 24 cartas especiais do jogo.
 * Usado para: obter uma carta pelo nome (ex: ao ler do Firestore),
 * listar todas (ex: IA a escolher aleatoriamente), e obter a descrição
 * para o tooltip de long press no inventário.
 */
object RegistoCartasEspeciais {

    val todas: List<CartaEspecialJogo> = listOf(
        Shhh,
        Carta2, Carta3, Carta4, Carta5, Carta6, Carta7,
        SorteDoDealer,
        Massacre,
        Amizade,
        Escudo, EscudoMais,
        Espada, EspadaMais,
        Ate17, Ate24, Ate27,
        MaisUm, MenosUm,
        Renasce,
        Troca,
        VaiAPesca,
        Destroi,
        Resistente,
        Retorno,
        Remove,
        Renovar,
        MaisQuatro,
        Copia,
        Super8
    )

    /** Obtém uma carta pelo nome exacto (usado ao ler dados guardados no Firestore). */
    fun porNome(nome: String): CartaEspecialJogo? {
        return todas.find { it.nome == nome }
    }

    /** Descrições para o tooltip de long press no inventário (ACD secção 3.5). */
    val descricoes: Map<String, String> = mapOf(
        "Shhh" to "Recebe uma carta virada para baixo.",
        "Carta 2" to "Recebe a carta 2, se ainda estiver disponível.",
        "Carta 3" to "Recebe a carta 3, se ainda estiver disponível.",
        "Carta 4" to "Recebe a carta 4, se ainda estiver disponível.",
        "Carta 5" to "Recebe a carta 5, se ainda estiver disponível.",
        "Carta 6" to "Recebe a carta 6, se ainda estiver disponível.",
        "Carta 7" to "Recebe a carta 7, se ainda estiver disponível.",
        "Sorte do dealer" to "Recebe a melhor carta disponível para o objectivo actual.",
        "Massacre" to "Aumenta a aposta em 1, dá-te uma carta especial extra e retira 15 segundos ao timer.",
        "Amizade" to "Ambos os jogadores recebem +2 cartas especiais.",
        "Escudo" to "Diminui a aposta em 1.",
        "Escudo+" to "Diminui a aposta em 2.",
        "Espada" to "Aumenta a aposta em 1.",
        "Espada+" to "Aumenta a aposta em 2.",
        "Até 17" to "O objectivo passa a 17 para ambos os jogadores.",
        "Até 24" to "O objectivo passa a 24 para ambos os jogadores.",
        "Até 27" to "O objectivo passa a 27 para ambos os jogadores.",
        "Mais um" to "O objectivo aumenta em 1 para ambos os jogadores.",
        "Menos um" to "O objectivo diminui em 1 para ambos os jogadores.",
        "Renasce" to "Apaga a carta especial mais recente da mesa e recebes uma carta especial.",
        "Troca" to "Troca a tua última carta com a última carta do oponente.",
        "Vai à pesca" to "Obriga o oponente a ir buscar 1 carta ao baralho.",
        "Destroi" to "Destrói a última carta especial usada pelo oponente.",
        "Resistente" to "Se chegares a 0 vidas no fim da ronda, recebes 1 vida.",
        "Retorno" to "Devolve a tua última carta ao baralho, numa posição aleatória.",
        "Remove" to "Devolve a última carta do oponente ao baralho, numa posição aleatória.",
        "Renovar" to "Devolve todas as tuas cartas ao baralho e recebes 2 novas.",
        "Mais quatro" to "Adiciona 4 cartas aleatórias (1-11) ao baralho.",
        "Copia" to "Recebes uma carta igual à última que o oponente recebeu.",
        "Super 8" to "Se tiveres o 8 e ganhares a ronda, causas +2 de dano extra."
    )
}
