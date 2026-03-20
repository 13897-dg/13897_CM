# Assignment 1 - Report

Tutorial 1 - Hello Kotlin. Hello Mobile Android World!
Course : Computação Móvel (CM)
Student (s) : David (13897)
Date : Março 2026
Repository URL : https://github.com/13897-dg/13897_CM
---
## 1. Introduction
Este repositório contém a primeira entrega da cadeira de Computação Móvel (Tutorial 1). O principal objetivo é introduzir o desenvolvimento nativo em Android utilizando a linguagem Kotlin e rever os conceitos base de programação em Java.
A área de trabalho está dividida em quatro projetos distintos (módulos) dentro da pasta `TP1`:
- **CM_TP1**: Um projeto base estruturado com Maven contendo configurações elementares para aprender Kotlin.
- **Helloworld**: Uma aplicação Android fundamental criada para aprender Android Studio
- **System_info**: Uma aplicação Android que interage programaticamente com as propriedades do dispositivo através da API `android.os.Build`.
- **Vibecode (Joke App)**: Uma aplicação Android mais complexa desenvolvida com o Google Antigravity, ou seja usando unm agente de AI, baseada em Kotlin que consome endpoints externos via API (apipheny.io) para apresentar conteúdo dinâmico ao utilizador.

## 2. System Overview
O sistema desenvolvido é um laboratório de aprendizagem progressivo, dividido em exercícios independentes. Em vez de um único produto de software gigantesco, os vários *use cases* estão encapsulados em módulos para demonstrar diferentes valências do SDK de Android:
- Interação elementar com a IDE, Consola de *Logs* e configurações de ciclos de vida.
- Interoperabilidade local com o hardware do sistema telefónico através de APIs internas (`android.os.Build`).
- Interações complexas, chamadas de rede a servidores terceiros e injeção de resultados numa Interface de Utilizador (a app *Vibecode*).

Mecanismos globais como o ecossistema Gradle/Maven partilhado dentro da mesma *workspace* garantem a standardização de todas as configurações técnicas desenvolvidas em contexto de laboratório.

## 3. Architecture and Design
- **Linguagem Principal**: **Kotlin** foi utilizado de forma ubíqua para todo o ecossistema Android dos projetos (`Helloworld`, `System_info` e `Vibecode`).
- **Padrão de UI**: Os primeiros exercícios iteraram sobre sistemas nativos usando Views Clássicas (`XML`, `findViewById`). Para as iterações e componentes dinâmicos de front-end (como no `Vibecode`), explora-se a adoção de conceitos e designs de **Jetpack Compose**.
- **Arquitetura de Diretórios**: Foram mantidos submódulos separados dentro de `TP1` no Android Studio, o que previne o acoplamento de código entre tutoriais diferentes, uma decisão chave para melhor legibilidade.

## 4. Implementation
- **Helloworld**: O ficheiro `MainActivity.kt` faz o "override" da função `onCreate` e imprime mensagens para o processo Logcat da IDE usando `println(this.localClassName + " onCreate")`.
- **System_info**: Um `StringBuilder` coleta exaustivamente todas as informações críticas usando a classe nativa: `Build.MANUFACTURER`, `Build.MODEL`, `Build.TYPE`, `Build.VERSION.SDK_INT`. Os dados concatenados são injetados na propriedade `.text` do elemento `systemInfoTextView`.
- **Vibecode**: Preparado para solicitações assíncronas de serviços REST (Jokes API), refletindo as práticas abordadas ao longo da criação de interfaces móveis e paradigmas assíncronos.

## 5. Testing and Validation
- **Estratégia**: Análise da compilação com o Gradle e teste de UI nos emuladores Android do sistema (AVD/Logcat).
- **Casos de Teste (Scenarios)**:
  - Validou-se a exibição dos "logs" quando a `Activity` é instanciada (`Helloworld`).
  - Garantiu-se que o tamanho e as _systemBars_ não ocultam conteúdo nas margens do ecrã (EdgeToEdge configs).
  - Confirmou-se em tempo de execução que o parse do `Build` extrai corretamente a identificação do modelo do telemóvel sem originar "crashes" (`System_info`).

## 6. Usage Instructions
1. Realizar o clone do repositório para uma pasta local.
2. Usar o explorador do sistema para abrir os diretórios (`CM_TP1`, `Helloworld`, `System_info`, ou `Vibecode`) com a IDE **Android Studio / IntelliJ IDEA**.
3. Aguardar que o `Gradle Build` faça download das dependências (Kotlin, Core Ktx, AppCompat).
4. No Android Studio, iniciar um emulador e clicar no botão `Run 'app'` (Shift + F10) para fazer deploy na máquina virtual (ou dispositivo físico por cabo).

---
# Autonomous Software Engineering Sections - only for [AC OK , AI OK]
## 7. Prompting Strategy
- Foi pedido a ferramenta uma explicação da diferença entre implementar XML Clássico versus a anotação moderna via `@Composable`, com o objetivo de alinhar o entendimento dos paradigmas para o desenvolvimento futuro no `Vibecode`.
- A estratégia de "prompting" continuou com instruções diretas e exploratórias, para além de um comando expresso (ex: `consegues criar um read.me com este perfil de report? sobre os 4 projetos não so o vibecode`) para juntar todo o contexto recolhido e organizar num único Markdown formal.

## 8. Autonomous Agent Workflow
- Como agente de Inteligência Artificial, o assistente usou ferramentas de execução (ex: `list_dir`, `view_file` e `find_by_name`) para aceder ao sistema de ficheiros local e avaliar o conteúdo exato de cada um dos quatro projetos (`TP1/Helloworld`, `TP1/System_info`), sem necessidade do utilizador copiar e colar manualmente o código fonte no chat, promovendo um "workflow" de descoberta automatizada e contextualização rápida.

## 9. Verification of AI - Generated Artifacts
- A responsabilidade da verificação recaiu sobre a análise cruzada entre os dados observados pelo próprio aluno na IDE, sublinha-se a revisão rigorosa de artefactos gerados como esta documentação para atestar que espelham diretamente o que os 4 projetos desenvolvem (ex: confirmar o código autêntico de leitura de Hardware na app System_info).

## 10. Human vs AI Contribution
- **Humano**: Estruturação inicial dos diretórios, instanciação dos projetos de raiz com o Gradle, desenvolvimento do código lógico inicial (`onCreate`, XML, injeção em Views), criação da configuração da API.
- **Assistente AI**: Análise de contexto estático ("code review"), explicação didática em língua portuguesa de conceitos arquiteturais Jetpack Compose, e geração mecânica deste relatório `.md` global agrupador.

## 11. Ethical and Responsible Use
- A ferramenta foi utilizada de forma complementar e didática (para rever conceitos e estruturar relatórios com exatidão) com plena consciência dos requisitos curriculares. Todos os artefactos produzidos foram compreendidos sem desvio às instruções da academia, validando-se a inexistência de "hallucinations" (criação de factos ou código inexistentes) na formulação do relatório.

---
# Development Process

## 12. Version Control and Commit History
- Foi feito uso de Git (VCS) com histórico progressivo correspondente à evolução de laboratório:
  - Submissão da base laboratorial introdutória (`CM_TP1`).
  - Setup do esqueleto de Android (`Helloworld`).
  - Atualização do projeto para a extração nativa de dados (`System_info`).
  - Iterações modernas e focadas na implementação do Joke App (`Vibecode`).

## 13. Difficulties and Lessons Learned
- **Dificuldades**: Gerir a transição da mudança de estado e o "lifecycle" imperativo para lógicas mais reativas. Compreender os _Insets_, o tratamento de UI margens das versões novas de Android e as separações de projetos.
- **Lições Aprendidas**: Consolidaram-se os processos e rituais do Android (`setContentView`, encontrar Views no XML via R.id, etc.) e o conhecimento vital sobre extração de dados internos do sistema com a `android.os.Build` listando na perfeição num layout.

## 14. Future Improvements
- Refatorar progressivamente o código XML legado para Compose Moderno.
- Adicionar uma cache local (ex: _Room Database_) no projeto `Vibecode` de modo a ver piadas _offline_.
- Adicionar Testes Unitários e UI Automatizados instrumentados em Espresso.

---
## 15. AI Usage Disclosure ( Mandatory )
- **Ferramentas Utilizadas**: Assistente de Inteligência Artificial (Google Antigravity / Gemini).
- **Como foram utilizadas**: 
  - Auxílio de suporte/clarificação ao longo dos módulos **CM_TP1** (`cm_library`) e **System_info**.
  - Utilização assistida e guiada a **100% no desenvolvimento integral** da aplicação **Vibecode** (Joke App).
  - Geração estruturada e auxílio na organização textual deste guião `README.md`.
- **Confirmação**: Confirmo que os conteúdos estruturados e o código gerado pelo assistente foram alvo da minha leitura e validação total e assumo o mérito, responsabilidade total e autoria funcional de toda a informação partilhada.
