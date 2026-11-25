package br.edu.univille.poo.pianotiles.service;

import br.edu.univille.poo.pianotiles.dto.PartidaDTO;
import br.edu.univille.poo.pianotiles.model.Musica;
import br.edu.univille.poo.pianotiles.model.Partida;
import br.edu.univille.poo.pianotiles.repository.PartidaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final MusicaService musicaService;

    public PartidaService(PartidaRepository partidaRepository, MusicaService musicaService) {
        this.partidaRepository = partidaRepository;
        this.musicaService = musicaService;
    }

    public Partida registrar(PartidaDTO dto) {
        Musica musica = musicaService.obterPeloId(dto.musicaId());

        int total = Math.max(1, dto.totalTiles());
        int pontos = Math.max(0, Math.min(total, dto.pontos()));

        Partida partida = new Partida();
        partida.setNomeJogador(normalizarNome(dto.nomeJogador()));
        partida.setMusica(musica);
        partida.setPontos(pontos);
        partida.setTotalTiles(total);
        partida.setEstrelas(Math.max(0, Math.min(3, dto.estrelas())));
        partida.setComboMaximo(Math.max(0, Math.min(total, dto.comboMaximo())));
        partida.setConcluida(dto.concluida() && pontos == total);
        partida.setDataHora(LocalDateTime.now());
        return partidaRepository.save(partida);
    }

    public List<Partida> rankingGeral() {
        return partidaRepository.findTop10ByOrderByPontosDescDataHoraAsc();
    }

    public List<Partida> rankingDaMusica(Long musicaId) {
        return partidaRepository.findTop10ByMusicaIdOrderByPontosDescDataHoraAsc(musicaId);
    }

    public List<Partida> historico(String jogador) {
        return partidaRepository.findTop20ByNomeJogadorIgnoreCaseOrderByDataHoraDesc(normalizarNome(jogador));
    }

    public static String normalizarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return "Anônimo";
        }
        nome = nome.strip();
        return nome.length() > 20 ? nome.substring(0, 20) : nome;
    }
}
