# 🎹 Piano Tiles AI

Sistema web feito com **Spring Boot + Spring AI** para a Atividade GenAI de POO (Univille).
É um clone do jogo **Piano Tiles**: toque só nos tiles pretos, na ordem, sem deixar nenhum escapar.
A GenAI **compõe músicas novas** (que viram fases do jogo) e funciona como **treinador** do jogador.

## Requisitos da atividade

| Requisito | Onde está |
|---|---|
| Integração com banco de dados | H2 + Spring Data JPA: entidades `Musica`, `Tile` e `Partida` (ranking e histórico ficam salvos) |
| Interface Web | Thymeleaf (`index`, `jogar`, `historico`) + jogo em canvas/JavaScript (`static/js/jogo.js`) |
| Integração com GenAI via Spring AI | `GenAIService` usando `ChatClient` e `BeanOutputConverter` (OpenRouter) |
| Funcionalidades com GenAI | **Compositor IA** e **Treinador IA** (detalhes abaixo) |
| Chave fora do GitHub | A chave fica em variável de ambiente ou em `chave.properties`, que está no `.gitignore` |

## Funcionalidades com GenAI

1. **Compositor IA**: o jogador descreve uma música ("trilha de filme de aventura", "música de chefão de videogame"...)
   e escolhe a dificuldade. A IA devolve um JSON estruturado (título, gênero, BPM, descrição e melodia),
   convertido em objeto Java pelo `BeanOutputConverter` do Spring AI. O `GeradorDePista` transforma a melodia em
   tiles (simples, longos e duplos) e a fase é salva no banco.
2. **Treinador IA**: lê as últimas partidas do jogador no banco e devolve uma análise com ponto forte,
   onde ele erra, 3 dicas e qual música jogar em seguida. Fica disponível no fim de cada partida e na tela de histórico.

## Como rodar

Requisitos: **Java 17+** (o Maven vem junto, pelo `mvnw`).

1. Crie uma chave gratuita em https://openrouter.ai/keys
2. Configure a chave de **uma** destas formas:
   - copie `chave.properties.exemplo` para `chave.properties` e cole a chave; **ou**
   - crie a variável de ambiente `OPENROUTER_API_KEY`.
3. Rode:
   ```bash
   # Windows
   mvnw.cmd spring-boot:run
   # Linux/Mac
   ./mvnw spring-boot:run
   ```
   (ou rode a classe `PianoTilesApplication` pelo IntelliJ)
4. Abra http://localhost:8080

Console do banco: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/pianotiles`, usuário `sa`, sem senha).

> O jogo e as 4 músicas iniciais funcionam mesmo sem chave. Só o Compositor e o Treinador precisam dela.

## Como jogar

- Clique/toque nos tiles pretos de baixo para cima, ou use as teclas **D F J K**.
- Tile com bolinha amarela é **longo**: segure até encher.
- Nas dificuldades 3+ aparecem **tiles duplos**: toque nos dois.
- Errar a faixa ou deixar um tile passar = fim de jogo. A velocidade aumenta ao longo da música.
- Estrelas: 1 ⭐ em 33%, 2 ⭐ em 66% e 3 ⭐ ao completar a música.

## Estrutura

```
src/main/java/br/edu/univille/poo/pianotiles
├── PianoTilesApplication.java
├── config/DadosIniciais.java        # músicas iniciais (domínio público)
├── controller/HomeController.java   # telas Thymeleaf + formulário do Compositor IA
├── controller/ApiController.java    # REST usado pelo jogo (pista, salvar partida, treinador)
├── dto/                             # TileDTO, PartidaDTO, MusicaGeradaDTO, TreinadorDTO
├── model/                           # Musica, Tile, Partida (JPA)
├── repository/                      # Spring Data JPA
└── service/
    ├── GenAIService.java            # toda a integração com o Spring AI
    ├── GeradorDePista.java          # melodia -> tiles
    ├── MusicaService.java
    └── PartidaService.java
```

### Endpoints REST

| Método | URL | Descrição |
|---|---|---|
| GET | `/api/musicas/{id}/pista` | tiles da música |
| POST | `/api/partidas` | salva o resultado de uma partida |
| POST | `/api/treinador` | análise do Treinador IA (`{"jogador": "Nome"}`) |
