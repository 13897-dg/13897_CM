# Application Design Document (ADD)
## 21 Duel
**Versão:** 1.0  
**Data:** Maio 2026  
**Unidade Curricular:** Computação Móvel  
**Instituição:** Escola Náutica Infante D. Henrique  

> Este documento complementa o ACD (Application Concept Document). Enquanto o ACD define **o quê** e **porquê**, o ADD define **como** a aplicação vai ser construída.

---

## 1. Arquitectura Geral

A aplicação segue o padrão **MVVM (Model-View-ViewModel)**:

- **Model** — classes de dados (`Utilizador`, `Partida`, `Ronda`, ...) e acesso ao Firebase
- **View** — Activities/Fragments, responsáveis apenas por mostrar dados e capturar interacções
- **ViewModel** — ponte entre Model e View; guarda o estado do ecrã e contém a lógica de apresentação; sobrevive a mudanças de configuração (ex: rotação do ecrã)

### Porquê MVVM
Comparado com MVC (lógica tende a misturar-se com a interface) e MVP (o Presenter manipula a View directamente), o MVVM separa melhor as responsabilidades e é o padrão recomendado pela Google para apps Android modernas.

---

## 2. Estrutura de Packages

```
com.enautica.duel21/
│
├── data/
│   ├── model/
│   │   ├── Utilizador.kt
│   │   ├── Partida.kt
│   │   ├── Ronda.kt
│   │   ├── CartaEspecial.kt
│   │   ├── CartaUsada.kt
│   │   ├── Cosmetico.kt
│   │   └── SubscricaoPRO.kt
│   │
│   └── repository/
│       ├── UtilizadorRepository.kt
│       ├── PartidaRepository.kt
│       └── CosmeticoRepository.kt
│
├── ui/
│   ├── login/
│   │   ├── LoginActivity.kt
│   │   └── LoginViewModel.kt
│   ├── menu/
│   │   ├── MenuActivity.kt
│   │   └── MenuViewModel.kt
│   ├── lobby/
│   │   ├── LobbyActivity.kt
│   │   └── LobbyViewModel.kt
│   ├── jogo/
│   │   ├── JogoActivity.kt
│   │   └── JogoViewModel.kt
│   ├── perfil/
│   │   ├── PerfilActivity.kt
│   │   └── PerfilViewModel.kt
│   ├── tutorial/
│   │   └── TutorialActivity.kt
│   └── resultados/
│       ├── ResultadosActivity.kt
│       └── ResultadosViewModel.kt
│
├── game/
│   ├── MotorJogo.kt                  (lógica core: pontuação, vencedor, validações)
│   ├── IAJogador.kt                  (comportamento da IA)
│   └── CartasEspeciaisEfeitos.kt     (aplica cada efeito de carta especial)
│
└── util/
    └── Constantes.kt                  (valores fixos: timer=60s, vidas=8, etc.)
```

**Notas:**
- `data/model` mapeia directamente as entidades do diagrama E-A
- `data/repository` isola o resto da app de saber como os dados são guardados no Firebase
- `game/` contém lógica pura, sem dependências de Android — mais fácil de testar isoladamente
- `Constantes.kt` centraliza valores ajustáveis após feedback de utilizadores (timer, vidas, incremento de aposta)
- O suporte multilíngue (português, inglês, terceira língua) usa a estrutura standard do Android — `res/values/strings.xml`, `res/values-en/strings.xml`, etc. — e não entra na árvore de packages Kotlin

---

## 3. Fluxo Detalhado dos Ecrãs

### 3.1 Login
**Estados:** ecrã inicial / a autenticar (loading) / erro de autenticação  
**Interacções:**
- Login com email/password → valida no Firebase Auth → navega para Menu
- "Criar conta" → diálogo de registo (email, password, username)
- "Entrar com Google" → fluxo OAuth do Firebase → navega para Menu

**Dados que saem:** `idUtilizador` (guardado na sessão Firebase Auth, disponível globalmente)

---

### 3.2 Menu Principal
**Estados:** normal  
**Interacções:**
- "Jogar Online" → navega para Lobby
- "vs IA" → cria partida local contra IA, navega directo para Jogo
- "Tutorial" → navega para Tutorial
- "Loja" / "Perfil" → navega para Perfil/Loja
- "Definições" → diálogo simples (idioma, conta)

**Dados que entram:** `idUtilizador`

---

### 3.3 Lobby
**Estados:** à procura de jogo / sala criada à espera / a entrar com código / erro (código inválido)  
**Interacções:**
- "Procurar jogo" → matchmaking automático no Firebase → ao encontrar oponente, navega para Jogo
- "Criar sala" → gera código, fica à espera
- "Entrar com código" → valida no Firebase → navega para Jogo
- "Cancelar procura" → cancela matchmaking, volta ao estado inicial

**Dados que saem:** `idPartida`

---

### 3.4 Jogo
**Estados:**
1. À espera do oponente (sala privada, raro)
2. Teu turno — "Pedir carta" e "Parar" activos, inventário clicável
3. Turno do oponente — controlos desactivados, timer a contar
4. Ronda terminada — cartas reveladas por 2-3 segundos antes da ronda seguinte
5. Partida terminada — navega automaticamente para Resultados

**Interacções:**
- "Pedir carta" → pede carta ao motor de jogo, actualiza mão, passa turno
- "Parar" → regista stay, passa turno (ou revela mãos se o oponente já tinha dado stay)
- Tocar carta especial → joga, aplica efeito, actualiza UI
- Long press em carta especial → mostra tooltip com descrição (sem jogar)
- Timer chega a 0 → stay automático; 3 consecutivos = derrota por abandono

**Dados que entram:** `idPartida`  
**Dados que saem:** resultado da partida (para Resultados)

---

### 3.5 Resultados
**Estados:** vitória / derrota  
**Interacções:**
- "Jogar outra vez" → cria nova partida → navega para Lobby (online) ou Jogo (IA)
- "Menu principal" → navega para Menu

**Dados que entram:** `idPartida`

---

### 3.6 Perfil / Loja
**Estados:** normal / a processar pagamento PRO (loading)  
**Interacções:**
- "Subscrever PRO" → fluxo de pagamento → actualiza `SubscricaoPRO`
- Tocar cosmético → equipa (se desbloqueado) ou mostra opção de subscrição (se PRO)

**Dados que entram:** `idUtilizador`

---

### 3.7 Tutorial
**Estados:** passo 1 a 6  
**Interacções:**
- "Seguinte" / "Anterior" → navega entre passos
- "Saltar tutorial" → volta ao Menu

**Dados:** nenhum dado externo necessário, conteúdo estático

---

## 4. Modelo de Dados (Kotlin)

```kotlin
// data/model/Utilizador.kt
data class Utilizador(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val idAvatar: String = "default_avatar",
    val vitorias: Int = 0,
    val derrotas: Int = 0
)
// nota: "password" não entra aqui — gerido directamente pelo Firebase Authentication

// data/model/Partida.kt
data class Partida(
    val id: String = "",
    val idJogador1: String = "",
    val idJogador2: String = "",
    val estado: EstadoPartida = EstadoPartida.EM_CURSO,
    val vencedor: String? = null,
    val vidasJogador1: Int = 8,
    val vidasJogador2: Int = 8
)

enum class EstadoPartida { EM_CURSO, TERMINADA }

// data/model/Ronda.kt
data class Ronda(
    val id: String = "",
    val idPartida: String = "",
    val numero: Int = 1,
    val vencedor: String? = null, // null enquanto a ronda decorre
    val aposta: Int = 1,
    val objectivo: Int = 21,
    val cartasDisponiveis: List<Int> = (1..11).toList(),
    val timestampInicio: Long = System.currentTimeMillis()
)

// data/model/CartaEspecial.kt
data class CartaEspecial(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val tipo: TipoEfeito = TipoEfeito.IMEDIATO,
    val idJogador: String = "",
    val usada: Boolean = false
)

enum class TipoEfeito { IMEDIATO, PASSIVO }

// data/model/CartaUsada.kt
data class CartaUsada(
    val id: String = "",
    val idRonda: String = "",
    val idCartaEspecial: String = "",
    val idJogador: String = "",
    val turno: Int = 0
)

// data/model/Cosmetico.kt
data class Cosmetico(
    val id: String = "",
    val nome: String = "",
    val tipo: TipoCosmetico = TipoCosmetico.AVATAR,
    val exclusivoPRO: Boolean = false,
    val desbloqueio: TipoDesbloqueio = TipoDesbloqueio.DEFAULT
)

enum class TipoCosmetico { AVATAR, BARALHO, TEMA }
enum class TipoDesbloqueio { DEFAULT, NIVEL, PRO }

// data/model/SubscricaoPRO.kt
data class SubscricaoPRO(
    val id: String = "",
    val idUtilizador: String = "",
    val dataInicio: Long = System.currentTimeMillis(),
    val dataRenovacao: Long = 0L,
    val activa: Boolean = false
)
```

---

## 5. Motor de Jogo

Lógica pura, sem dependências de Android nem do Firebase — facilita testes isolados.

```kotlin
// game/MotorJogo.kt
object MotorJogo {

    /** Calcula a pontuação total de uma mão (soma simples das cartas). */
    fun calcularPontuacao(mao: List<Int>): Int {
        return mao.sum()
    }

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
    ): String? {
        val j1Valido = pontosJogador1 <= objectivo
        val j2Valido = pontosJogador2 <= objectivo

        return when {
            j1Valido && !j2Valido -> "jogador1"
            j2Valido && !j1Valido -> "jogador2"
            j1Valido && j2Valido -> {
                when {
                    pontosJogador1 > pontosJogador2 -> "jogador1"
                    pontosJogador2 > pontosJogador1 -> "jogador2"
                    else -> null // empate exacto
                }
            }
            else -> null // os dois ultrapassaram o objectivo — empate
        }
    }

    /** Aplica o efeito das cartas de objectivo "até X" — substitui a anterior. */
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
     */
    fun melhorCartaParaObjectivo(
        pontuacaoActual: Int,
        objectivo: Int,
        cartasDisponiveis: List<Int>
    ): Int? {
        val maximoUtil = objectivo - pontuacaoActual
        return cartasDisponiveis
            .filter { it <= maximoUtil }
            .maxOrNull()
            ?: cartasDisponiveis.minOrNull()
    }

    /** Aplica perda de vidas ao perdedor da ronda. */
    fun aplicarPerdaVidas(vidasActuais: Int, aposta: Int): Int {
        return (vidasActuais - aposta).coerceAtLeast(0)
    }
}

// game/IAJogador.kt
object IAJogador {

    /** Decide se a IA pede carta ou fica, com base na pontuação actual. */
    fun decidirAccao(pontuacaoActual: Int, objectivo: Int): AccaoIA {
        return if (pontuacaoActual <= objectivo - 3) {
            AccaoIA.PEDIR_CARTA
        } else {
            AccaoIA.FICAR
        }
    }

    /** Escolhe aleatoriamente 1 carta especial do inventário para jogar nesta ronda. */
    fun escolherCartaEspecial(inventario: List<String>): String? {
        if (inventario.isEmpty()) return null
        return inventario.random()
    }
}

enum class AccaoIA { PEDIR_CARTA, FICAR, JOGAR_ESPECIAL }
```

> Nota: a implementação de cada uma das 24 cartas especiais individuais (em `CartasEspeciaisEfeitos.kt`) é feita durante a fase de Produção, sobre esta base.

---

## 6. Navegação entre Ecrãs

Navegação feita com **Intents** e dados passados como *extras*.

```kotlin
// Lobby → Jogo (encontrou oponente / sala preenchida)
val intent = Intent(this, JogoActivity::class.java)
intent.putExtra("idPartida", partidaId)
startActivity(intent)

// Jogo → Resultados (partida terminou)
val intent = Intent(this, ResultadosActivity::class.java)
intent.putExtra("idPartida", partidaId)
startActivity(intent)
finish() // remove o ecrã de Jogo da pilha de navegação
```

O `idUtilizador` não precisa de ser passado entre ecrãs — está disponível globalmente via `FirebaseAuth.getInstance().currentUser?.uid` em qualquer Activity.

**Mapa de navegação:**

| Origem | Destino | Dados passados |
|---|---|---|
| Login | Menu | `idUtilizador` (sessão) |
| Menu | Lobby | `idUtilizador` |
| Menu | Jogo (vs IA) | `idPartida` (criada localmente) |
| Lobby | Jogo | `idPartida` (matchmaking/sala) |
| Jogo | Resultados | `idPartida` |
| Resultados | Lobby/Jogo | — (jogar outra vez) |
| Resultados | Menu | — |

---

## 7. Perfis de Utilizador

Ver secção 2 do ACD para a descrição completa das personas (Casual, Competitivo, Social).

---

## 8. Diagrama Entidade-Associação Completo

Ver secção 9.1 do ACD — diagrama com atributos disponível em `/docs/concept/entity_diagram_full.png`.

---

## 9. Parâmetros de Design Confirmados

Ver secção 10 do ACD:
- Limite de cartas especiais no inventário — sem limite
- Timer por ronda — 60 segundos
- Aposta — +1 a cada ronda
- "Massacre" — retira 15 segundos ao timer

---

## 10. Próximos Passos

- Configuração e teste do Firebase (Authentication + Firestore)
- Protótipo mínimo de navegação entre ecrãs (dados hardcoded)
- Implementação individual das 24 cartas especiais em `CartasEspeciaisEfeitos.kt`
