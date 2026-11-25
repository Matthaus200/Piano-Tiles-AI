package br.edu.univille.poo.pianotiles.controller;

import br.edu.univille.poo.pianotiles.dto.PartidaDTO;
import br.edu.univille.poo.pianotiles.dto.TileDTO;
import br.edu.univille.poo.pianotiles.dto.TreinadorDTO;
import br.edu.univille.poo.pianotiles.model.Partida;
import br.edu.univille.poo.pianotiles.service.GenAIService;
import br.edu.univille.poo.pianotiles.service.MusicaService;
import br.edu.univille.poo.pianotiles.service.PartidaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints chamados pelo JavaScript do jogo.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final MusicaService musicaService;
    private final PartidaService partidaService;
    private final GenAIService genAIService;

    public ApiController(MusicaService musicaService, PartidaService partidaService, GenAIService genAIService) {
        this.musicaService = musicaService;
        this.partidaService = partidaService;
        this.genAIService = genAIService;
    }

    /** Pista (tiles) de uma música. */
    @GetMapping("/musicas/{id}/pista")
    public List<TileDTO> pista(@PathVariable Long id) {
        return musicaService.obterPista(id);
    }

    /** Salva o resultado de uma partida. */
    @PostMapping("/partidas")
    public Map<String, Object> salvarPartida(@RequestBody PartidaDTO dto) {
        Partida partida = partidaService.registrar(dto);
        return Map.of("id", partida.getId(), "pontos", partida.getPontos());
    }

    /** Treinador IA: analisa o histórico do jogador. */
    @PostMapping("/treinador")
    public TreinadorDTO treinador(@RequestBody TreinadorDTO pedido) {
        String jogador = PartidaService.normalizarNome(pedido.jogador());
        String analise = genAIService.treinar(jogador, partidaService.historico(jogador));
        return new TreinadorDTO(jogador, analise);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> erro(Exception e) {
        String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("mensagem", msg));
    }
}
