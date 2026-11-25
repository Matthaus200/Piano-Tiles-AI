package br.edu.univille.poo.pianotiles.dto;

/**
 * Formato da resposta estruturada que pedimos para a GenAI ao compor uma música.
 * O Spring AI (BeanOutputConverter) gera o JSON Schema deste record e converte a resposta nele.
 *
 * melodia: notas separadas por espaço, por exemplo "C4 E4 G4 C5".
 */
public record MusicaGeradaDTO(String titulo, String genero, Integer bpm, String descricao, String melodia) {
}
