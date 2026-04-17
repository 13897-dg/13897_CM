import os

# Content for the README / Report file
readme_content = """# Relatório de Desenvolvimento: Cool Weather App

Este documento descreve o processo de desenvolvimento, as escolhas tecnológicas e as funcionalidades implementadas na aplicação **Cool Weather App**, desenvolvida em ambiente Android Studio com Kotlin.

## 1. Visão Geral do Projeto
A **Cool Weather App** é uma ferramenta de meteorologia que permite ao utilizador consultar dados climáticos em tempo real (temperatura, vento, pressão, etc.) para qualquer localização geográfica através de coordenadas de Latitude e Longitude.

## 2. Interface e Design (UI/UX)
A interface foi desenhada para ser dinâmica e adaptativa, focando-se na experiência do utilizador:

* **Temas Dinâmicos:** Implementação de um sistema de temas que alterna entre **Dia (Light)** e **Noite (Dark)**, tanto em modo **Retrato (Portrait)** como **Paisagem (Landscape)**.
* **Background Reativo:** O fundo da aplicação muda automaticamente com base na hora local da coordenada pesquisada, utilizando imagens personalizadas (`sunny_bg` e `night_bg`).
* **Leitabilidade:** Foi introduzida uma camada de fundo semi-transparente (`View` com Alpha) entre a imagem de fundo e os textos para garantir contraste e leitura clara dos dados.
* **Ícone da Aplicação:** Criação de um ícone de lançamento personalizado através do *Image Asset Studio*.

## 3. Implementação Técnica (Backend)

### 3.1. Integração com API
A aplicação consome dados da API pública **Open-Meteo**. A comunicação é feita de forma assíncrona para garantir a fluidez da interface:
* **Threads:** As chamadas de rede são executadas numa `Thread` secundária para não bloquear a *UI Thread*.
* **runOnUiThread:** Após a receção dos dados, a interface é atualizada de forma segura através deste método.
* **GSON:** Utilização da biblioteca da Google para converter automaticamente a resposta JSON da API em objetos Kotlin (`Data Classes`).

### 3.2. Estrutura de Dados
Foram criadas classes específicas para mapear a resposta da API:
* `WeatherData`: Classe principal que contém latitude, longitude e objetos de tempo.
* `CurrentWeather`: Dados atuais (Temperatura, Velocidade do Vento, Código de Clima).
* `Hourly`: Dados por hora, utilizados para extrair a pressão atmosférica.
* `WMO_WeatherCode`: Um `Enum` que mapeia os códigos técnicos da API para nomes de ícones amigáveis (ex: código 0 -> `clear_day`).

## 4. Internacionalização (i18n)
A aplicação suporta múltiplos idiomas através do sistema de `strings.xml`:
* **Português (Portugal)**
* **Inglês (Default)**
  A tradução abrange todos os rótulos, botões e mensagens da aplicação, adaptando-se automaticamente às definições do sistema operativo do utilizador.

## 5. Funcionalidades de Inteligência
* **Timezone Auto:** A aplicação solicita o fuso horário automático à API (`timezone=auto`), garantindo que a hora exibida é a hora real da localização pesquisada.
* **Cálculo de Ciclo Solar:** A lógica implementada verifica a hora local devolvida pela API para decidir se deve exibir o tema de Dia ou Noite, independentemente da hora no telemóvel do utilizador.

## 6. Configurações de Sistema
Para o correto funcionamento, foram configurados os seguintes elementos:
* **Permissões:** Adicionada a permissão `android.permission.INTERNET` no `AndroidManifest.xml`.
* **Dependências:** Inclusão do `com.google.code.gson:gson:2.8.9` no ficheiro `build.gradle`.

## 7. Conclusão
O projeto demonstra o domínio de conceitos fundamentais de desenvolvimento Android, incluindo ciclo de vida de atividades, gestão de layouts complexos, manipulação de recursos gráficos, comunicação com serviços web externos e boas práticas de programação assíncrona e internacionalização.

---
**Desenvolvido como parte do currículo de Programação Android.**
"""

# Save to a markdown file
file_path = '/mnt/data/report_cool_weather_app.md'
with open(file_path, 'w', encoding='utf-8') as f:
f.write(readme_content)

print(f"File saved at: {file_path}")