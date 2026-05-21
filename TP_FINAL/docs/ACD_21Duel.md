# Application Concept Document (ACD)
## 21 Duel
**Versão:** 1.2  
**Data:** Maio 2026  
**Unidade Curricular:** Computação Móvel  
**Instituição:** Escola Náutica Infante D. Henrique  

---

## 1. Conceito Geral

**21 Duel** é um jogo de cartas 1v1 para Android inspirado no mini-jogo "21" presente em Resident Evil, adaptado para um contexto casual e independente de qualquer elemento de terror ou ligação à franchise original.

O jogador compete contra outro jogador online ou contra uma IA, tentando aproximar-se o mais possível de 21 pontos sem ultrapassar esse valor, enquanto utiliza cartas especiais para influenciar o estado do jogo.

---

## 2. Público-Alvo

- Jogadores casuais de telemóvel (15–35 anos)
- Fãs de jogos de cartas digitais (ex: Hearthstone, Slay the Spire, Blackjack)
- Utilizadores que procuram partidas rápidas e competitivas

### Personas

**Persona 1 — O Casual**
- 16–22 anos, joga no telemóvel para passar o tempo
- Quer partidas rápidas, não quer ler regras complexas
- Nunca vai pagar PRO mas vê anúncios
- Usa o tutorial, depende dos tooltips das cartas especiais
- Joga maioritariamente vs IA

**Persona 2 — O Competitivo**
- 18–30 anos, fã de jogos de cartas digitais
- Quer ranking, quer ganhar, estuda as cartas especiais
- Candidato PRO — paga para ter cosméticos exclusivos e sem anúncios
- Joga quase exclusivamente online

**Persona 3 — O Social**
- 15–25 anos, joga principalmente com amigos
- Usa salas privadas com código, não liga ao matchmaking
- Pode pagar PRO se os amigos pagarem
- Não liga muito ao ranking mas gosta de personalizar o avatar

> Nota: um utilizador real pode combinar características de várias personas. A sobreposição mais comum e mais valiosa é Competitivo + Social — joga online, usa salas privadas com amigos, e é o perfil mais provável de converter para PRO.

---

## 3. Mecânica de Jogo

### 3.1 Estrutura Base
- Baralho fixo de **11 cartas numeradas de 1 a 11**, igual para todos os jogadores
- Cada jogador começa com **2 cartas**: uma visível e uma oculta
- Os turnos alternam entre jogadores — em cada turno o jogador pode:
  - **Pedir carta** — recebe uma carta adicional do baralho
  - **Ficar** — passa a vez sem pedir carta
  - **Jogar carta especial** — activa um efeito do seu inventário
- Quando ambos ficam (ou não há mais cartas disponíveis), revelam-se as mãos
- Quem estiver mais perto do objectivo (por defeito 21) ganha a ronda
- Em caso de empate na ronda, ninguém perde vidas e começa nova ronda

### 3.2 Baralho Vazio
- Quando o baralho fica vazio a meio de uma ronda, os jogadores **não podem pedir carta** mas ainda podem **jogar cartas especiais**
- A ronda só termina quando ambos derem stay

### 3.3 Sistema de Vidas e Apostas
- Cada jogador começa com **8 vidas**
- Cada ronda tem uma **aposta** (começa em 1, aumenta ao longo da partida — frequência por definir)
- Quem perde a ronda perde vidas igual ao valor da aposta
- O jogo termina quando um jogador chega a **0 vidas**
- Empate na partida é impossível — só um jogador pode chegar a 0 vidas

### 3.4 Timeout e Jogo Online
- Cada ronda tem um **timer** (duração por definir)
- Se um jogador não jogar dentro do tempo, o jogo dá **stay automático** por ele
- Após **3 stays automáticos consecutivos**, o jogador perde a partida por abandono

### 3.5 Cartas Especiais
- Cada jogador começa sem cartas especiais
- A partir da 2ª ronda, os jogadores recebem cartas especiais alternando entre 1 e 2 por ronda
- O **campo de cartas especiais faz reset a cada ronda** — nenhuma carta especial dura mais do que a ronda em que foi jogada
- O **inventário de cartas especiais mantém-se** entre rondas — cartas não usadas ficam disponíveis para rondas seguintes
- Existem dois tipos de efeito:
  - **Imediato** — o efeito acontece ao jogar a carta
  - **Passivo/Campo** — o efeito mantém-se enquanto a carta estiver em campo durante a ronda
- Pressão longa (long press) sobre uma carta especial no inventário mostra uma janela com a descrição do efeito

### 3.6 Regras das Cartas de Objectivo
- Cartas "até X" (Até 17, Até 24, Até 27) — **só pode haver uma activa de cada vez**; a mais recente substitui a anterior; afectam ambos os jogadores
- Cartas "Mais um" / "Menos um" — acumulam entre si e aplicam-se sobre o objectivo actual (incluindo o de uma carta "até X" activa)
- A carta **"Sorte do dealer"** usa o objectivo actual no momento de utilização

#### Lista de Cartas Especiais

| Nome | Efeito | Tipo |
|---|---|---|
| Shhh | Recebe uma carta virada para baixo | Imediato |
| Carta 2 a 7 | Recebe a carta com o número correspondente (se já em campo, não faz nada) | Imediato |
| Sorte do dealer | Recebe a melhor carta disponível para o objectivo actual | Imediato |
| Massacre | Aumenta aposta +1, recebe carta especial extra, retira X segundos ao timer | Passivo |
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

---

## 4. Modos de Jogo

- **Online 1v1** — matchmaking automático ou sala privada com código; tempo real
- **vs IA** — partida contra inteligência artificial local

### 4.1 Comportamento da IA
- Dificuldade única (simples)
- Se pontuação actual ≤ objectivo - 3 → pede carta; caso contrário → stay
- Joga 1 carta especial por ronda, escolhida aleatoriamente

---

## 5. Tutorial (conteúdo)

O tutorial é apresentado em ecrã próprio, passo a passo com imagem ilustrativa e texto:

| Passo | Texto |
|---|---|
| 1 | Chega o mais perto possível de 21 sem ultrapassar. Quem estiver mais perto no fim da ronda ganha! |
| 2 | O baralho tem 11 cartas numeradas de 1 a 11. Começas com 2 cartas — uma visível e uma escondida. Só tu sabes a tua mão completa. |
| 3 | Em cada turno podes pedir carta, ficar (stay) ou jogar uma carta especial. Quando ambos ficarem, as mãos são reveladas. |
| 4 | Cada jogador começa com 8 vidas. Quem perde a ronda perde vidas igual à aposta dessa ronda. A aposta vai aumentando — cuidado! |
| 5 | A partir da 2ª ronda recebes cartas especiais. Usa-as para virar o jogo a teu favor — mudam apostas, objectivos, cartas em campo e muito mais. |
| 6 | O jogo acaba quando um jogador chegar a 0 vidas. Boa sorte! |

---

## 6. Ecrãs Principais

A aplicação conta com 7 ecrãs principais, desenvolvidos em simultâneo como wireframes e mockups com a ferramenta **Figma**, disponíveis em `/docs/concept/wireframes/`:

| Ecrã | Descrição |
|---|---|
| 1. Login | Autenticação com email/password ou Google |
| 2. Menu Principal | Hub central com acesso a todos os modos e secções |
| 3. Lobby | Matchmaking online e criação/entrada em sala privada |
| 4. Jogo | Campo de cartas com mãos, zona de especiais e controlos |
| 5. Perfil / Loja | Cosméticos, subscrição PRO e estatísticas do jogador |
| 6. Tutorial | Guia passo a passo da mecânica do jogo |
| 7. Resultados | Resultado da partida e resumo estatístico |

---

## 7. Modelo de Negócio

### Feature gratuita (todos os utilizadores)
- Jogar online e vs IA sem restrições
- Acesso a cosméticos base
- Anúncios

### Feature paga — PRO (1€/mês)
- Baralhos exclusivos
- Temas de mesa personalizados
- Avatares animados exclusivos
- Sem anúncios

### Projecção
Nada

---

## 8. Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **IDE:** Android Studio
- **Base de dados / Backend:** Firebase (Firestore + Authentication)
- **Prototipagem:** Figma
- **IA integrada:** Antigravity (funcionalidade extra)

---

## 9. Diagrama Entidade-Associação (simplificado)

Diagrama disponível em `/docs/concept/entity_diagram.png`.

Entidades identificadas:
- **Utilizador** — conta e perfil do jogador
- **Partida** — sessão de jogo entre dois utilizadores
- **Ronda** — cada ronda dentro de uma partida
- **CartaEspecial** — inventário de cartas especiais durante a partida
- **CartaUsada** — registo das cartas especiais jogadas em cada ronda
- **Cosmético** — todos os itens visuais desbloqueáveis (avatares, baralhos, temas de mesa)
- **SubscriçãoPRO** — registo da subscrição activa do utilizador

### 9.1 Diagrama E-A Completo (com atributos)

Diagrama disponível em `/docs/concept/entity_diagram_full.png`.

| Entidade | Atributos |
|---|---|
| Utilizador | id, email, password, username, idAvatar (FK), vitorias, derrotas |
| Partida | id, idJogador1 (FK), idJogador2 (FK), estado, vencedor, vidasJogador1, vidasJogador2 |
| Ronda | id, idPartida (FK), numero, vencedor, aposta, objectivo, cartasDisponiveis, timestampInicio |
| CartaEspecial | id, nome, descricao, tipo, idJogador (FK), usada |
| CartaUsada | id, idRonda (FK), idCartaEspecial (FK), idJogador, turno |
| Cosmético | id, nome, tipo (avatar/baralho/tema), exclusivoPRO, desbloqueio (default/nivel/PRO) |
| SubscriçãoPRO | id, idUtilizador (FK), dataInicio, dataRenovacao, activa |

---

## 10. Detalhes por Definir

- Limite máximo de cartas especiais no inventário por jogador
- Frequência de aumento da aposta (a cada X rondas?)
- Timer da ronda — duração total
- Quantidade de segundos que o "Massacre" retira ao timer
- Multilíngue — português, inglês e uma terceira língua
- Sistema de ranking/classificação online — tabela global ou só registo V/D?
- Níveis e XP — esqueleto a definir (implementação futura)
