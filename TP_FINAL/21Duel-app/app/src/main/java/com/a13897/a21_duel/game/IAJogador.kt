package com.a13897.a21_duel.game

/**
 * Comportamento simples da IA — definido no ACD secção 4.1.
 * Dificuldade única, previsível e fácil de testar.
 */
object IAJogador {

    /** Decide se a IA pede carta ou fica, com base na pontuação actual. */
    fun decidirAccao(pontuacaoActual: Int, objectivo: Int): AccaoIA {
        return if (pontuacaoActual <= objectivo - 3) {
            AccaoIA.PEDIR_CARTA
        } else {
            AccaoIA.FICAR
        }
    }

    /** Escolhe aleatoriamente 1 carta especial do inventário para jogar nesta ronda (ou nenhuma). */
    fun escolherCartaEspecial(inventario: List<String>): String? {
        if (inventario.isEmpty()) return null
        return inventario.random()
    }
}

enum class AccaoIA { PEDIR_CARTA, FICAR }
