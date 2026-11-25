package br.edu.univille.poo.pianotiles.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Uma música (fase) do jogo. Pode vir dos dados iniciais ou ser composta pela GenAI.
 */
@Entity
public class Musica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String genero;
    private int bpm;

    /** 1 (fácil) até 5 (insano). */
    private int dificuldade;

    private boolean geradaPorIA;

    @Column(length = 1000)
    private String descricao;

    /** Pedido que o jogador digitou para a IA (só para músicas geradas). */
    @Column(length = 500)
    private String pedido;

    private LocalDateTime dataCriacao;

    @OneToMany(mappedBy = "musica", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<Tile> tiles = new ArrayList<>();

    public void adicionarTile(Tile tile) {
        tile.setMusica(this);
        tile.setOrdem(tiles.size());
        tiles.add(tile);
    }

    public String getEstrelasDificuldade() {
        return "★".repeat(dificuldade) + "☆".repeat(5 - dificuldade);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public int getBpm() { return bpm; }
    public void setBpm(int bpm) { this.bpm = bpm; }

    public int getDificuldade() { return dificuldade; }
    public void setDificuldade(int dificuldade) { this.dificuldade = dificuldade; }

    public boolean isGeradaPorIA() { return geradaPorIA; }
    public void setGeradaPorIA(boolean geradaPorIA) { this.geradaPorIA = geradaPorIA; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getPedido() { return pedido; }
    public void setPedido(String pedido) { this.pedido = pedido; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public List<Tile> getTiles() { return tiles; }
    public void setTiles(List<Tile> tiles) { this.tiles = tiles; }
}
