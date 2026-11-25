package br.edu.univille.poo.pianotiles.model;

import jakarta.persistence.*;

/**
 * Uma linha da pista do jogo. Pode ter 1 tile (faixa1) ou 2 tiles ao mesmo tempo (faixa1 + faixa2).
 * As faixas vão de 0 a 3 (da esquerda para a direita).
 */
@Entity
public class Tile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int ordem;

    private int faixa1;
    private String nota1;

    /** Segunda faixa (tile duplo). Nulo quando o tile é simples. */
    private Integer faixa2;
    private String nota2;

    /** Tile longo: o jogador precisa segurar. */
    private boolean longo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musica_id")
    private Musica musica;

    public Tile() {
    }

    public Tile(int faixa1, String nota1, Integer faixa2, String nota2, boolean longo) {
        this.faixa1 = faixa1;
        this.nota1 = nota1;
        this.faixa2 = faixa2;
        this.nota2 = nota2;
        this.longo = longo;
    }

    public boolean isDuplo() {
        return faixa2 != null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }

    public int getFaixa1() { return faixa1; }
    public void setFaixa1(int faixa1) { this.faixa1 = faixa1; }

    public String getNota1() { return nota1; }
    public void setNota1(String nota1) { this.nota1 = nota1; }

    public Integer getFaixa2() { return faixa2; }
    public void setFaixa2(Integer faixa2) { this.faixa2 = faixa2; }

    public String getNota2() { return nota2; }
    public void setNota2(String nota2) { this.nota2 = nota2; }

    public boolean isLongo() { return longo; }
    public void setLongo(boolean longo) { this.longo = longo; }

    public Musica getMusica() { return musica; }
    public void setMusica(Musica musica) { this.musica = musica; }
}
