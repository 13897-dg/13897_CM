# Teste Firebase — 21 Duel

**Fase:** Concept  
**Data:** Junho 2026

---

## Objectivo

Validar a ligação entre a aplicação Android e o Firebase, conforme exigido na fase Concept do projecto: implementar uma app de teste com login via Firebase Authentication e capacidade de guardar/ler dados no Firestore.

## Configuração

- **Authentication:** Email/Password activado
- **Firestore Database:** modo de teste, localização `eur3 (europe-west)`
- **Package:** `com.a13897.a21_duel`

## Teste Realizado

Código de teste colocado temporariamente em `MainActivity.kt` (`onCreate`), executando em sequência:

1. Criação de conta de teste (`teste@21duel.com`)
2. Login com essa conta
3. Escrita de um documento na colecção `testes` do Firestore
4. Leitura do mesmo documento de volta

### Resultado (Logcat)

```
FirebaseTeste  D  Conta criada com sucesso
FirebaseTeste  D  Login feito com sucesso. UID: BbfcsIM0pyV1yNnv4zVvwx1aMf72
FirebaseTeste  D  Documento guardado com ID: BB5gQ4ylOrLxUh51jqkk
FirebaseTeste  D  Lido: {mensagem=Hello World do 21 Duel, timestamp=1782405995202}
```

Confirmado também visualmente no Firebase Console:
- Utilizador `teste@21duel.com` presente em Authentication
- Documento presente na colecção `testes` em Firestore Database

## Conclusão

Ligação entre a app e o Firebase (Authentication + Firestore) validada com sucesso. O código de teste é temporário e será substituído pela implementação definitiva (camada `data/repository/`, ver ADD secção 2) durante a fase de Produção.

## Por Fazer

- Teste de upload/download de imagem via Firebase Storage — não realizado nesta fase por decisão do autor; o enunciado pede explicitamente este teste ("save and get text and images"), pelo que pode ser necessário revisitar antes da entrega final caso seja avaliado.
