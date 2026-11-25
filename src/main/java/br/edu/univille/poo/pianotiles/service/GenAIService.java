package br.edu.univille.poo.pianotiles.service;

import br.edu.univille.poo.pianotiles.dto.MusicaGeradaDTO;
import br.edu.univille.poo.pianotiles.model.Musica;
import br.edu.univille.poo.pianotiles.model.Partida;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Toda a integração com a GenAI (Spring AI) fica nesta classe.
 *
 * Funcionalidades:
 *  1. Compositor IA: cria uma música/fase nova a partir de um pedido em texto (resposta estruturada em JSON).
 *  2. Treinador IA: lê o histórico do jogador no banco e devolve uma análise com dicas.
 */
@Service
public class GenAIService {

    private final ChatClient.Builder chatBuilder;
    private final MusicaService musicaService;
    private final GeradorDePista geradorDePista;

    public GenAIService(ChatClient.Builder chatBuilder,
                        MusicaService musicaService,
                        GeradorDePista geradorDePista) {
        this.chatBuilder = chatBuilder;
        this.musicaService = musicaService;
        this.geradorDePista = geradorDePista;
    }

    /**
     * Funcionalidade 1 - Compositor IA.
     * Pede para a IA compor uma melodia, converte a resposta em objeto Java e salva a fase no banco.
     */
    public Musica comporMusica(String pedido, int dificuldade) {
        if (pedido == null || pedido.isBlank()) {
            throw new IllegalArgumentException("Descreva a música que você quer que a IA crie.");
        }
        int nivel = Math.max(1, Math.min(5, dificuldade));
        int quantidadeNotas = 30 + nivel * 6;

        // O BeanOutputConverter gera as instruções de formato (JSON Schema) a partir do record.
        BeanOutputConverter<MusicaGeradaDTO> conversor = new BeanOutputConverter<>(MusicaGeradaDTO.class);

        String resposta = chatBuilder.build().prompt()
                .system("""
                        Você é um compositor de músicas para o jogo Piano Tiles.
                        Você cria melodias simples, agradáveis e fáceis de reconhecer.
                        Responda sempre em português do Brasil.
                        """)
                .user("""
                        Crie uma música para uma fase do Piano Tiles inspirada neste pedido: "%s".
                        Dificuldade %d (1 = fácil, 5 = muito difícil).

                        Regras:
                        - "titulo": um nome criativo e curto para a música.
                        - "genero": estilo musical em uma ou duas palavras.
                        - "bpm": número inteiro entre 80 e 160 (mais rápido para dificuldades maiores).
                        - "descricao": uma frase curta e divertida sobre a música.
                        - "melodia": exatamente %d notas separadas por espaço, no formato NOTA+OITAVA,
                          usando só notas de C3 a C6 (exemplos: C4 D4 E4 F#4 G4 A4 B4 C5).
                          Faça frases musicais que se repetem e variam, como uma música de verdade.

                        %s
                        """.formatted(pedido.replace("\"", "'"), nivel, quantidadeNotas, conversor.getFormat()))
                .call()
                .content();

        MusicaGeradaDTO gerada = converter(conversor, resposta);

        // Valida as notas: se o JSON vier quebrado, tenta aproveitar as notas do texto inteiro.
        List<String> notas = geradorDePista.extrairNotas(gerada.melodia());
        if (notas.size() < GeradorDePista.MINIMO_DE_NOTAS) {
            notas = geradorDePista.extrairNotas(resposta);
        }
        if (notas.size() < GeradorDePista.MINIMO_DE_NOTAS) {
            throw new IllegalStateException("A IA não devolveu uma melodia válida. Tente de novo.");
        }

        String titulo = vazio(gerada.titulo()) ? "Música da IA" : gerada.titulo();
        String genero = vazio(gerada.genero()) ? "IA" : gerada.genero();
        int bpm = gerada.bpm() == null ? 90 + nivel * 12 : gerada.bpm();

        return musicaService.criarMusica(titulo, genero, bpm, nivel, notas, true, gerada.descricao(), pedido);
    }

    /**
     * Funcionalidade 2 - Treinador IA.
     * Envia o histórico do jogador (do banco de dados) e pede uma análise personalizada.
     */
    public String treinar(String jogador, List<Partida> historico) {
        if (historico.isEmpty()) {
            return "Ainda não encontrei partidas de " + jogador
                    + ". Jogue algumas músicas e depois peça a análise de novo!";
        }

        StringBuilder resumo = new StringBuilder();
        for (Partida p : historico) {
            resumo.append("- %s (dificuldade %d, %d BPM): %d de %d tiles (%d%%), %d estrelas, combo máximo %d, %s%n"
                    .formatted(p.getMusica().getTitulo(), p.getMusica().getDificuldade(), p.getMusica().getBpm(),
                            p.getPontos(), p.getTotalTiles(), p.getPercentual(), p.getEstrelas(),
                            p.getComboMaximo(), p.isConcluida() ? "completou a música" : "errou antes do fim"));
        }

        return chatBuilder.build().prompt()
                .system("""
                        Você é um treinador simpático e motivador de jogadores de Piano Tiles.
                        Responda em português do Brasil, em texto simples (sem markdown, sem asteriscos).
                        """)
                .user("""
                        Analise as últimas partidas do jogador %s (da mais recente para a mais antiga):
                        %s
                        Responda em no máximo 120 palavras com:
                        1) o ponto forte dele;
                        2) onde ele mais erra (músicas, velocidade ou dificuldade);
                        3) três dicas práticas e curtas para melhorar;
                        4) qual música ele deveria jogar agora como próximo desafio.
                        """.formatted(jogador, resumo))
                .call()
                .content();
    }

    private MusicaGeradaDTO converter(BeanOutputConverter<MusicaGeradaDTO> conversor, String resposta) {
        if (resposta == null) {
            throw new IllegalStateException("A IA não respondeu. Tente de novo.");
        }
        try {
            // Alguns modelos colocam texto antes/depois do JSON: pegamos só o objeto.
            int inicio = resposta.indexOf('{');
            int fim = resposta.lastIndexOf('}');
            String json = inicio >= 0 && fim > inicio ? resposta.substring(inicio, fim + 1) : resposta;
            MusicaGeradaDTO dto = conversor.convert(json);
            if (dto != null) {
                return dto;
            }
        } catch (RuntimeException e) {
            // segue para o plano B abaixo
        }
        return new MusicaGeradaDTO(null, null, null, null, resposta);
    }

    private boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }
}
