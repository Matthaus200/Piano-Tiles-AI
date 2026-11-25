package br.edu.univille.poo.pianotiles.repository;

import br.edu.univille.poo.pianotiles.model.Tile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TileRepository extends JpaRepository<Tile, Long> {

    List<Tile> findAllByMusicaIdOrderByOrdemAsc(Long musicaId);
}
