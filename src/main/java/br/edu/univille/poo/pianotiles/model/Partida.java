package br.edu.univille.poo.pianotiles.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Resultado de uma partida jogada. É usado no ranking e pelo Treinador IA.
 */
@Entity
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeJogador;

    @ManyToOne
    @JoinColumn(name = "musica_id")
    private Musica musica;

    /** Quantidade de linhas acertadas. */
    private int pontos;
    private int totalTiles;
    private int estrelas;
    private int comboMaximo;
    private boolean concluida;
    private LocalDateTime dataHora;

    public int getPercentual() {
        return totalTiles == 0 ? 0 : Math.round(pontos * 100f / totalTiles);
    }

    public String getEstrelasTexto() {
        return "★".repeat(estrelas) + "☆".repeat(3 - estrelas);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeJogador() { return nomeJogador; }
    public void setNomeJogador(String nomeJogador) { this.nomeJogador = nomeJogador; }

    public Musica getMusica() { return musica; }
    public void setMusica(Musica musica) { this.musica = musica; }

    public int getPontos() { return pontos; }
    public void setPontos(int pontos) { this.pontos = pontos; }

    public int getTotalTiles() { return totalTiles; }
    public void setTotalTiles(int totalTiles) { this.totalTiles = totalTiles; }

    public int getEstrelas() { return estrelas; }
    public void setEstrelas(int estrelas) { this.estrelas = estrelas; }

    public int getComboMaximo() { return comboMaximo; }
    public void setComboMaximo(int comboMaximo) { this.comboMaximo = comboMaximo; }

    public boolean isConcluida() { return concluida; }
    public void setConcluida(boolean concluida) { this.concluida = concluida; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
