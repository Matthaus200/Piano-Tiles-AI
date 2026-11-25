package br.edu.univille.poo.pianotiles.repository;

import br.edu.univille.poo.pianotiles.model.Partida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    /** Ranking geral: mais pontos primeiro; em empate, quem fez antes. */
    List<Partida> findTop10ByOrderByPontosDescDataHoraAsc();

    /** Ranking de uma música. */
    List<Partida> findTop10ByMusicaIdOrderByPontosDescDataHoraAsc(Long musicaId);

    /** Histórico de um jogador (mais recentes primeiro). */
    List<Partida> findTop20ByNomeJogadorIgnoreCaseOrderByDataHoraDesc(String nomeJogador);

    void deleteAllByMusicaId(Long musicaId);
}
