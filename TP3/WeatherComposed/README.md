# WeatherComposed - Relatório de Projeto

## 📱 Visão Geral
**WeatherComposed** (anteriormente "CoolWeatherApp") é uma aplicação Android moderna de previsão do tempo. O principal objetivo deste projeto foi migrar uma interface tradicional baseada em XML para o **Jetpack Compose**, adotando a arquitetura **MVVM** e integrando bibliotecas modernas de comunicação em rede e mapas.

## 🛠️ Tecnologias Utilizadas
A aplicação foi construída utilizando o ecossistema moderno de desenvolvimento Android:
- **Linguagem:** Kotlin
- **Interface de Utilizador (UI):** Jetpack Compose (Material Design 3)
- **Arquitetura:** MVVM (Model-View-ViewModel) com `StateFlow` e `Coroutines`
- **Networking:** Ktor Client (`ktor-client-android`, `ktor-client-content-negotiation`)
- **Serialização de Dados:** Kotlinx Serialization (JSON)
- **Mapas:** Google Maps Compose (`maps-compose`, `play-services-maps`)

## 🏗️ Arquitetura e Estrutura do Projeto
O projeto foi estruturado para manter a separação de responsabilidades (Separation of Concerns), facilitando a manutenção e a escalabilidade da aplicação.

A estrutura de pacotes principal encontra-se em `com.a13897.weathercomposed`:

*   **`data/` (Camada de Dados):**
    *   `WeatherApiClient.kt`: Responsável por fazer as chamadas de rede à API [Open-Meteo](https://open-meteo.com/) usando o Ktor Client.
    *   `WeatherData.kt`: Contém os modelos de dados (Data Classes) gerados para mapear a resposta JSON da API e um enumerador `WMO_WeatherCode` para gerir os ícones de estado do tempo.
*   **`ui/` (Camada de Apresentação):**
    *   `WeatherScreen.kt`: Contém a UI principal do ecrã, gerindo dinamicamente as orientações **Portrait** (Vertical) e **Landscape** (Horizontal).
    *   `CoordinatesCard.kt`: Componente de UI para visualização e edição (manual ou via mapa) das coordenadas (Latitude e Longitude).
    *   `WeatherCard.kt` e `WeatherRow.kt`: Componentes para exibição elegante das informações climáticas (Temperatura, Vento, Pressão, etc.).
    *   `LocationPickerActivity.kt`: Atividade dedicada que exibe o Google Maps para permitir ao utilizador escolher visualmente uma localização e obter as respetivas coordenadas.
*   **`viewmodel/` (Camada de Lógica de Negócio):**
    *   `WeatherViewModel.kt`: Gere o estado da interface gráfica (`uiState`), expõe os dados usando `StateFlow` e faz a ponte com o `WeatherApiClient` para recolher novos dados do tempo de forma assíncrona com Coroutines.

## ✨ Principais Funcionalidades

1.  **Migração Completa para Compose:** Toda a interface gráfica é agora desenhada puramente em Kotlin com Jetpack Compose, garantindo melhor performance e facilidade em criar designs reativos.
2.  **Layouts Responsivos:** A aplicação tem dois layouts distintos (`PortraitWeatherUI` e `LandscapeWeatherUI`) que se adaptam perfeitamente à rotação do ecrã do dispositivo.
3.  **Seleção de Coordenadas por Mapa:** Integração de um ecrã de mapas (Google Maps) que permite clicar num ponto qualquer do globo para atualizar as coordenadas de pesquisa de meteorologia de forma visual.
4.  **Integração Ktor:** Troca da antiga implementação de chamadas HTTP para o moderno e eficiente Ktor Client, melhorando a gestão de respostas assíncronas.
5.  **Atualização Reativa:** Ao alterar as coordenadas ou pressionar o botão de atualização, a arquitetura MVVM garante que a UI é notificada imediatamente após a resposta da API, atualizando os campos na interface sem congelar o ecrã.

## 🚀 Como Executar o Projeto

1.  Clone o repositório e abra o projeto no **Android Studio** (Recomenda-se a versão mais recente, como o Android Studio Ladybug ou mais recente).
2.  Aguarde pela sincronização do Gradle.
3.  *Nota sobre a Chave da API do Google Maps:* A chave encontra-se no ficheiro `AndroidManifest.xml`. Caso a funcionalidade do mapa não esteja a carregar os tiles (mapa em branco), poderá ser necessário gerar uma nova *API Key* na Google Cloud Console e substituí-la na tag `<meta-data android:name="com.google.android.geo.API_KEY" ... />`.
4.  Selecione um Emulador ou Dispositivo Físico (mínimo API 24).
5.  Clique em **Run** (`Shift + F10`).
