package br.edu.univille.poo.pianotiles.dto;

import br.edu.univille.poo.pianotiles.model.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * Formato de um tile enviado para o JavaScript do jogo.
 * faixas: 1 ou 2 faixas (0 a 3); notas: nota de cada faixa (ex: "C4"); longo: tile de segurar.
 */
public record TileDTO(List<Integer> faixas, List<String> notas, boolean longo) {

    public static TileDTO de(Tile tile) {
        List<Integer> faixas = new ArrayList<>();
        List<String> notas = new ArrayList<>();
        faixas.add(tile.getFaixa1());
        notas.add(tile.getNota1());
        if (tile.isDuplo()) {
            faixas.add(tile.getFaixa2());
            notas.add(tile.getNota2());
        }
        return new TileDTO(faixas, notas, tile.isLongo());
    }
}
