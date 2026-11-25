package br.edu.univille.poo.pianotiles.dto;

/**
 * Dados que o jogo envia ao terminar uma partida.
 */
public record PartidaDTO(String nomeJogador,
                         Long musicaId,
                         int pontos,
                         int totalTiles,
                         int estrelas,
                         int comboMaximo,
                         boolean concluida) {
}
