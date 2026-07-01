# Relatório Final — 21 Duel
## Projecto Final de Computação Móvel

**Autor:** David Gonçalves  
**Docente:** Pedro Fazenda  
**Unidade Curricular:** Computação Móvel  
**Curso:** Engenharia Informática e de Computadores  
**Instituição:** Escola Superior Náutica Infante D. Henrique  
**Data:** Junho 2026  

---

## 1. Introdução

Este relatório documenta o processo completo de desenvolvimento da aplicação **21 Duel**, um jogo de cartas 1v1 para Android desenvolvido como projecto final da unidade curricular de Computação Móvel.

A aplicação foi desenvolvida em **Vibe Coding** com o apoio do modelo de inteligência artificial Claude (Anthropic), seguindo uma metodologia centrada na experiência do utilizador com as fases de Concept, Pre-production e Production.

### 1.1 Inspiração

21 Duel tem como base o mini-jogo "21" presente na franchise *Resident Evil*, desenvolvido originalmente pela Capcom. O conceito, as mecânicas de jogo e as cartas especiais foram adaptados, expandidos e transformados numa experiência casual independente para telemóvel, sem qualquer ligação à franchise original.

### 1.2 Conceito

O jogador compete contra outro jogador online ou contra uma IA, tentando aproximar-se o mais possível de 21 pontos sem ultrapassar esse valor, enquanto utiliza cartas especiais para influenciar o estado do jogo. O jogo é 1v1 em tempo real, com sistema de vidas e apostas crescentes que mantém a tensão ao longo da partida.

---

## 2. Processo de Desenvolvimento

O desenvolvimento seguiu a metodologia definida no enunciado do projecto, organizada em fases sequenciais com documentação própria em cada etapa.

### 2.1 Fase Concept

Na fase Concept foram definidos os fundamentos da aplicação:

- Nome e conceito geral — **21 Duel**
- Público-alvo e personas de utilizador
- Mecânicas de jogo completas, incluindo as 25 cartas especiais
- Modelo de negócio (subscrição PRO — 1€/mês)
- Wireframes e mockups de todos os ecrãs (Figma)
- Mapa de navegação
- Diagrama E-A simplificado (sem atributos)
- Configuração e teste do Firebase (Authentication + Firestore)
- Produção do ACD (Application Concept Document)

Os wireframes/mockups foram desenvolvidos em simultâneo no Figma, saltando directamente para mockups de alta fidelidade (fundo escuro, hierarquia visual, botões estilizados), o que é explicitado no ACD como decisão consciente.

### 2.2 Fase Pre-production

Na fase Pre-production foram aprofundados os detalhes técnicos e de design:

- Diagrama E-A completo com todos os atributos
- Definição dos perfis de utilizador (3 personas)
- Produção do ADD (Application Design Document)
- Protótipo mínimo de navegação entre todos os ecrãs (Navigation Compose)
- Registo formal do teste Firebase

O ADD definiu a arquitectura MVVM com Jetpack Compose, a estrutura de packages Kotlin, o fluxo detalhado de cada ecrã, o modelo de dados em Kotlin, o motor de jogo e a navegação por rotas (Navigation Compose).

### 2.3 Fase Production

A fase Production correspondeu à implementação completa da aplicação, descrita em detalhe nas secções seguintes.

---

## 3. Arquitectura da Aplicação

### 3.1 Padrão Arquitectónico

A aplicação segue o padrão **MVVM (Model-View-ViewModel)** com **Jetpack Compose**:

- **Model** — classes de dados e repositórios de acesso ao Firebase
- **View** — Composables que mostram dados e capturam interacções do utilizador
- **ViewModel** — gere o estado de cada ecrã e contém a lógica de apresentação; sobrevive a mudanças de configuração

### 3.2 Estrutura de Packages

```
com.a13897.a21_duel/
├── data/
│   ├── model/
│   │   ├── Utilizador.kt
│   │   ├── Partida.kt
│   │   ├── Ronda.kt
│   │   ├── CartaEspecial.kt
│   │   ├── CartaUsada.kt
│   │   ├── Cosmetico.kt
│   │   └── SubscricaoPRO.kt
│   └── repository/
│       ├── UtilizadorRepository.kt
│       ├── PartidaRepository.kt
│       └── CosmeticoRepository.kt
├── ui/
│   ├── login/
│   ├── menu/
│   ├── lobby/
│   ├── jogo/
│   ├── perfil/
│   ├── resultados/
│   ├── tutorial/
│   ├── sobre/
│   ├── definicoes/
│   └── navigation/NavGraph.kt
└── game/
    ├── MotorJogo.kt
    ├── IAJogador.kt
    ├── EstadoRonda.kt
    ├── CartaEspecialJogo.kt
    ├── CartasEspeciais_grupo1.kt
    ├── CartasEspeciais_grupo2.kt
    ├── CartasEspeciais_grupo3.kt
    └── RegistoCartasEspeciais.kt
```

### 3.3 Tecnologias Utilizadas

| Tecnologia | Utilização |
|---|---|
| Kotlin | Linguagem de desenvolvimento |
| Android Studio | IDE |
| Jetpack Compose | Interface gráfica declarativa |
| Navigation Compose | Navegação entre ecrãs |
| Firebase Authentication | Autenticação de utilizadores |
| Firebase Firestore | Base de dados em tempo real |
| Gson | Serialização do estado de jogo (JSON) |
| Coroutines + Flow | Operações assíncronas e streams de dados |
| Figma | Wireframes e mockups |
| Claude (Anthropic) | Assistência de desenvolvimento (Vibe Coding) |

---

## 4. Ecrãs da Aplicação

A aplicação conta com 9 ecrãs, todos implementados em Jetpack Compose com o padrão `Screen` + `ScreenContent` para suporte a `@Preview`.

| Ecrã | Descrição |
|---|---|
| Login | Autenticação com email/password; suporte a criação de conta |
| Menu Principal | Hub central com acesso a todos os modos e secções |
| Lobby | Matchmaking automático e salas privadas com código |
| Jogo | Campo de cartas com mãos, inventário, cartas em campo e controlos |
| Resultados | Resultado da partida (vitória/derrota) e opções pós-jogo |
| Perfil / Loja | Cosméticos, estatísticas e subscrição PRO |
| Tutorial | Guia passo a passo em 6 passos com navegação |
| Definições | Conta, idioma e acesso ao ecrã Sobre |
| Sobre | Informação do projecto, autor e contexto académico |

### 4.1 Navegação

A navegação é gerida pelo `NavGraph.kt` com Navigation Compose, usando rotas com parâmetros para passar dados entre ecrãs:

```
login → menu → lobby → jogo/{idPartida}/{idJogador1}/{idJogador2}/{contraIA} → resultados/{idPartida}
menu → tutorial
menu → perfil
menu → definicoes → sobre
```

O `idUtilizador` não é passado por rota — está disponível globalmente via `FirebaseAuth.getInstance().currentUser?.uid`.

---

## 5. Modelo de Dados

### 5.1 Diagrama E-A Simplificado

Diagrama disponível em `/docs/concept/entity_diagram.png`.

### 5.2 Diagrama E-A Completo

Diagrama disponível em `/docs/concept/entity_diagram_full.png`.

### 5.3 Entidades e Atributos

| Entidade | Atributos |
|---|---|
| Utilizador | id, email, username, idAvatar (FK), vitorias, derrotas |
| Partida | id, idJogador1 (FK), idJogador2 (FK), estado, vencedor, vidasJogador1, vidasJogador2, inventarioJogador1, inventarioJogador2, estadoRondaJson |
| Ronda | id, idPartida (FK), numero, vencedor, aposta, objectivo, cartasDisponiveis, timestampInicio |
| CartaEspecial | id, nome, descricao, tipo, idJogador (FK), usada |
| CartaUsada | id, idRonda (FK), idCartaEspecial (FK), idJogador, turno |
| Cosmético | id, nome, tipo (avatar/baralho/tema), exclusivoPRO, desbloqueio (default/nivel/PRO) |
| SubscriçãoPRO | id, idUtilizador (FK), dataInicio, dataRenovacao, activa |

> Nota: os campos `inventarioJogador1`, `inventarioJogador2` e `estadoRondaJson` foram adicionados à entidade `Partida` durante a fase de Production, para suportar a sincronização em tempo real do estado de jogo entre clientes.

### 5.4 Estrutura no Firestore

```
firestore/
├── utilizadores/{idUtilizador}
│   └── cosmeticosDesbloqueados/{idCosmetico}
├── partidas/{idPartida}
│   └── rondas/{idRonda}
├── cosmeticos/{idCosmetico}
├── matchmaking/{idUtilizador}
└── salasPrivadas/{codigo}
```

---

## 6. Mecânica de Jogo

### 6.1 Estrutura Base

- Baralho fixo de **11 cartas numeradas de 1 a 11**, igual para todos os jogadores
- Cada jogador começa com **2 cartas**: uma visível e uma oculta
- Os turnos alternam entre jogadores — em cada turno o jogador pode pedir carta, ficar (stay) ou jogar carta especial
- Quando ambos ficam (ou o baralho está vazio), revelam-se as mãos
- Quem estiver mais perto do objectivo ganha a ronda; empate significa que ninguém perde vidas

### 6.2 Sistema de Vidas e Apostas

- Cada jogador começa com **8 vidas**
- A aposta começa em 1 e aumenta **+1 por ronda**
- Quem perde a ronda perde vidas igual ao valor da aposta
- O jogo termina quando um jogador chega a 0 vidas

### 6.3 Timer e Timeout

- Cada ronda tem um **timer de 60 segundos** por turno
- Se o timer esgotar, é dado stay automático pelo jogador cuja vez era
- Após 3 stays automáticos consecutivos, o jogador perde a partida por abandono

### 6.4 Cartas Especiais

- A partir da 2ª ronda, os jogadores recebem cartas especiais (alternando entre 1 e 2 por ronda)
- O campo de especiais faz reset a cada ronda; o inventário mantém-se entre rondas
- Long press sobre uma carta no inventário mostra o tooltip com a sua descrição

#### Lista completa de cartas especiais

| Nome | Efeito | Tipo |
|---|---|---|
| Shhh | Recebe uma carta virada para baixo | Imediato |
| Carta 2 a 7 | Recebe a carta com o número correspondente (se já em campo, não faz nada) | Imediato |
| Sorte do dealer | Recebe a melhor carta disponível para o objectivo actual | Imediato |
| Massacre | Aumenta aposta +1, recebe carta especial extra, retira 15 segundos ao timer | Passivo |
| Amizade | Ambos os jogadores recebem +2 cartas especiais | Imediato |
| Escudo | Diminui aposta -1 | Passivo |
| Escudo+ | Diminui aposta -2 | Passivo |
| Espada | Aumenta aposta +1 | Passivo |
| Espada+ | Aumenta aposta +2 | Passivo |
| Até 17 | Objectivo passa a 17 (ambos); substitui outra carta "até X" activa | Passivo |
| Até 24 | Objectivo passa a 24 (ambos); substitui outra carta "até X" activa | Passivo |
| Até 27 | Objectivo passa a 27 (ambos); substitui outra carta "até X" activa | Passivo |
| Mais um | Objectivo aumenta +1 (ambos); acumula com outras cartas de objectivo | Passivo |
| Menos um | Objectivo diminui -1 (ambos); acumula com outras cartas de objectivo | Passivo |
| Renasce | Apaga a carta especial mais recente da mesa e recebe uma carta especial | Imediato |
| Troca | Troca a carta mais recente dos dois jogadores | Imediato |
| Vai à pesca | Obriga o oponente a ir buscar 1 carta (não funciona sem cartas disponíveis) | Imediato |
| Destroi | Destrói a última carta especial usada pelo oponente (efeitos passivos cessam) | Imediato |
| Resistente | Se chegar a 0 vidas no fim da ronda, recebe 1 vida | Passivo |
| Retorno | Devolve a tua última carta ao baralho em posição aleatória | Imediato |
| Remove | Devolve a última carta do oponente ao baralho em posição aleatória | Imediato |
| Renovar | Devolve todas as tuas cartas ao baralho e recebe 2 novas | Imediato |
| Mais quatro | Adiciona 4 cartas aleatórias (1–11) ao baralho em posições aleatórias | Imediato |
| Copia | Recebe uma carta igual à última que o oponente recebeu | Imediato |
| Super 8 | Se tiveres o 8 e ganhares a ronda, dás +2 de dano extra | Passivo |

### 6.5 Alternância de Turnos

As rondas ímpares começam sempre com o Jogador 1 (Host); as rondas pares começam com o Jogador 2 (Cliente). Esta alternância garante equidade ao longo da partida.

### 6.6 Comportamento da IA

- Dificuldade única (simples), jogada localmente no dispositivo
- Se pontuação actual ≤ objectivo − 3 → pede carta; caso contrário → stay
- Joga 1 carta especial aleatória por ronda, com delay visual de 2.5 segundos para simular "pensar"
- Partidas vs IA são também registadas no Firestore (usando `"IA"` como `idJogador2`)

---

## 7. Firebase e Sincronização em Tempo Real

### 7.1 Autenticação

Implementada via Firebase Authentication com email/password. O `UtilizadorRepository` gere o registo, login e logout; a password nunca é armazenada no Firestore.

### 7.2 Firestore e Fonte de Verdade Única

A sincronização do estado de jogo online foi um dos principais desafios técnicos do projecto. A solução adoptada baseia-se no princípio de **Fonte de Verdade Única**:

- Apenas o **Host (Jogador 1)** escreve o `estadoRondaJson` no Firestore
- O **Cliente (Jogador 2)** observa o documento via `callbackFlow` + `awaitClose` e aplica o estado recebido localmente
- O estado da ronda completo é serializado em JSON via **Gson** e guardado num único campo do documento da Partida
- Inventários de ambos os jogadores são também guardados no mesmo documento, garantindo sincronização atómica

### 7.3 Matchmaking

O matchmaking automático funciona através de uma colecção `matchmaking` no Firestore:

1. O primeiro jogador cria uma entrada de espera
2. O segundo jogador encontra a entrada, cria a Partida e remove a entrada
3. Ambos são notificados via `SnapshotListener` (protocolo de handshake) e transitam para o ecrã de Jogo com o mesmo `idPartida`

As salas privadas seguem o mesmo protocolo, usando um código de 6 dígitos como identificador.

### 7.4 Mitigação de Race Conditions

Um problema identificado durante o desenvolvimento foi o de "rondas duplas" — o Firebase podia disparar múltiplos eventos de snapshot para o mesmo update, causando que o fim de ronda fosse processado duas vezes.

A solução implementada foi uma **tranca de segurança** (`aProcessarFimDeRonda: Boolean`) no `JogoViewModel`:

```kotlin
private fun verificarFimDaRonda() {
    if (aProcessarFimDeRonda) return // ignora chamadas duplicadas
    if (!estado.rondaTerminou()) return
    aProcessarFimDeRonda = true // tranca
    // ... processa o fim da ronda
}
```

A tranca é reposta a `false` no início de cada nova ronda.

Adicionalmente, ao comparar o estado remoto com o local, o timer é ignorado (`copy(tempoRestanteSegundos = 0)`) para evitar falsos positivos de diferença de estado causados pela latência de rede.

### 7.5 Detecção de Acções do Oponente

O Cliente infere o que o oponente fez comparando o estado anterior com o estado remoto recebido:

- Se o inventário do oponente diminuiu → **"Usou carta: X"**
- Se a mão do oponente tem mais cartas → **"Pediu carta."**
- Se o stay do oponente passou de false para true → **"Parou (Stay)."**

Esta abordagem evita um campo extra no Firestore para comunicar a acção, reduzindo writes desnecessários.

---

## 8. Motor de Jogo

O motor de jogo (`game/`) é implementado como **lógica pura em Kotlin**, sem dependências de Android ou Firebase, o que facilita testes isolados.

### 8.1 EstadoRonda

O `EstadoRonda` é um `data class` imutável — cada alteração devolve uma nova cópia via `.copy()`, tornando o estado previsível e fácil de serializar para o Firestore:

```kotlin
data class EstadoRonda(
    val numero: Int,
    val aposta: Int,
    val objectivoBase: Int = 21,
    val ajusteObjectivo: Int = 0,
    val cartaAteXActiva: String? = null,
    val maoJogador1: List<CartaMao> = emptyList(),
    val maoJogador2: List<CartaMao> = emptyList(),
    val cartasEspeciaisEmCampo: List<String> = emptyList(),
    val cartasDisponiveis: List<Int> = (1..11).toList(),
    val stayJogador1: Boolean = false,
    val stayJogador2: Boolean = false,
    val turnoAtual: String = "jogador1",
    val tempoRestanteSegundos: Int = 60
)
```

### 8.2 Cartas Especiais

As 25 cartas especiais são implementadas como uma hierarquia `sealed class CartaEspecialJogo`, onde cada carta sabe aplicar o seu próprio efeito sobre o `EstadoRonda`:

```kotlin
sealed class CartaEspecialJogo(val nome: String, val tipo: TipoEfeitoCarta) {
    abstract fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda
}
```

Efeitos que afectam o inventário (fora do `EstadoRonda`) são tratados directamente no `JogoViewModel`.

### 8.3 Recálculo de Aposta e Objectivo

Quando uma carta passiva é destruída (carta **Destroi**), a aposta e o objectivo são recalculados a partir de **zero** com base no conjunto de cartas activas remanescentes em campo, em vez de tentar reverter o efeito individualmente. Isto evita bugs de estado inconsistente.

---

## 9. Modelo de Negócio

### 9.1 Versão Gratuita

- Jogar online e vs IA sem restrições
- Acesso a cosméticos base (avatares, baralhos e temas default)
- Anúncios

### 9.2 Subscrição PRO (1€/mês)

- Baralhos exclusivos
- Temas de mesa personalizados
- Avatares animados exclusivos
- Sem anúncios

A feature PRO é de natureza puramente cosmética, não afectando a jogabilidade — decisão consciente para não criar um modelo "pay-to-win" que afastaria utilizadores gratuitos.

> Nota: a integração real com Google Play Billing não foi implementada nesta versão. A app inclui uma função `simularSubscricaoPRO()` que desbloqueia os cosméticos PRO directamente para fins de demonstração.

---

## 10. Multilíngue

A aplicação suporta três línguas, implementadas com o sistema standard de recursos Android:

| Língua | Pasta |
|---|---|
| Português (base) | `res/values/strings.xml` |
| Inglês | `res/values-en/strings.xml` |
| Espanhol | `res/values-es/strings.xml` |

Os ficheiros de strings cobrem todos os textos da interface, incluindo os 6 passos do tutorial e os tooltips das 25 cartas especiais. O idioma é seleccionado automaticamente com base nas preferências do sistema Android.

---

## 11. Discussão de Questões Importantes

### 11.1 Arquitectura Multiplayer Online

A maior decisão arquitectónica do projecto foi a transição de um modelo de estado distribuído (ambos os clientes escrevem) para uma **Fonte de Verdade Única** (só o Host escreve). Esta mudança foi motivada por bugs de "rondas duplas" e inconsistências de estado causadas por latência de rede. A solução adoptada é robusta e escalável, mas introduz uma assimetria entre Host e Cliente que é gerida pelo campo `meuJogadorInterno` no `JogoViewModel`.

### 11.2 Serialização do Estado de Jogo

A decisão de serializar o `EstadoRonda` completo em JSON (Gson) e guardá-lo num único campo Firestore, em vez de modelar cada campo separadamente, foi uma troca deliberada entre normalização de dados e simplicidade de implementação. Para o volume de dados em causa (estado de uma ronda de cartas), esta abordagem é perfeitamente adequada.

### 11.3 IA Local vs Online

A IA é executada localmente no dispositivo (sem Firebase), mas as partidas contra IA são registadas no Firestore exactamente como partidas online, usando `"IA"` como identificador do segundo jogador. Isto simplifica a arquitectura (o `JogoViewModel` tem um único fluxo de dados) e garante que as estatísticas do jogador reflectem também as partidas contra IA.

### 11.4 Metodologia Vibe Coding

O desenvolvimento foi conduzido em Vibe Coding — uma metodologia de desenvolvimento assistido por IA onde o programador define a visão, valida decisões e dirige o processo, enquanto o agente de IA (Claude, Anthropic) gera e refina o código. Esta abordagem permitiu iterar rapidamente sobre decisões arquitectónicas complexas (como a resolução das race conditions) mantendo o controlo humano sobre as decisões de design.

---

## 12. Conclusões

O **21 Duel** é uma aplicação Android funcional que implementa um jogo de cartas 1v1 em tempo real com as seguintes características concluídas:

- Autenticação de utilizadores via Firebase
- Jogo online em tempo real com sincronização via Firestore
- Modo vs IA com comportamento definido
- 25 cartas especiais implementadas com lógica completa
- Sistema de vidas, apostas crescentes e timer por turno
- Matchmaking automático e salas privadas com código
- 9 ecrãs completos com Jetpack Compose
- Tutorial em 6 passos
- Suporte multilíngue (português, inglês, espanhol)
- Ecrã Sobre e Definições (incluindo logout)

### Trabalho Futuro

- Sistema de ranking e XP (identificado desde o início como implementação futura)
- Integração real com Google Play Billing para subscrição PRO
- Teste de upload/download de imagens via Firebase Storage
- Testes de usabilidade formais com utilizadores externos
- Animações e polimento visual

---

## Apêndices

- **Apêndice A** — Application Concept Document (ACD v1.3) — `/docs/concept/ACD_21Duel.md`
- **Apêndice B** — Application Design Document (ADD v1.0) — `/docs/pre-production/ADD_21Duel.md`
- **Apêndice C** — Relatório de Pre-production — `/docs/pre-production/Relatorio_PreProduction_21Duel.md`
- **Apêndice D** — Registo do teste Firebase — `/docs/concept/firebase_test.md`
- **Apêndice E** — Wireframes e mockups — `/docs/concept/wireframes/`
- **Apêndice F** — Diagrama de navegação — `/docs/concept/navigation_map.png`
- **Apêndice G** — Diagrama E-A simplificado — `/docs/concept/entity_diagram.png`
- **Apêndice H** — Diagrama E-A completo — `/docs/concept/entity_diagram_full.png`

---

*Relatório produzido em Junho de 2026 no âmbito da unidade curricular de Computação Móvel da Escola Superior Náutica Infante D. Henrique.*
