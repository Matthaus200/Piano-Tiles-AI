package br.edu.univille.poo.pianotiles.service;

import br.edu.univille.poo.pianotiles.dto.TileDTO;
import br.edu.univille.poo.pianotiles.model.Musica;
import br.edu.univille.poo.pianotiles.model.Tile;
import br.edu.univille.poo.pianotiles.repository.MusicaRepository;
import br.edu.univille.poo.pianotiles.repository.PartidaRepository;
import br.edu.univille.poo.pianotiles.repository.TileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MusicaService {

    private final MusicaRepository musicaRepository;
    private final TileRepository tileRepository;
    private final PartidaRepository partidaRepository;
    private final GeradorDePista geradorDePista;

    public MusicaService(MusicaRepository musicaRepository,
                         TileRepository tileRepository,
                         PartidaRepository partidaRepository,
                         GeradorDePista geradorDePista) {
        this.musicaRepository = musicaRepository;
        this.tileRepository = tileRepository;
        this.partidaRepository = partidaRepository;
        this.geradorDePista = geradorDePista;
    }

    public List<Musica> listarTodas() {
        return musicaRepository.findAllByOrderByGeradaPorIAAscDificuldadeAscIdAsc();
    }

    public Musica obterPeloId(Long id) {
        return musicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Música não encontrada: " + id));
    }

    public List<TileDTO> obterPista(Long musicaId) {
        return tileRepository.findAllByMusicaIdOrderByOrdemAsc(musicaId)
                .stream()
                .map(TileDTO::de)
                .toList();
    }

    public long contar() {
        return musicaRepository.count();
    }

    /**
     * Cria uma música a partir das notas, monta a pista e salva tudo no banco.
     */
    @Transactional
    public Musica criarMusica(String titulo, String genero, int bpm, int dificuldade,
                              List<String> notas, boolean geradaPorIA, String descricao, String pedido) {
        if (notas.size() < GeradorDePista.MINIMO_DE_NOTAS) {
            throw new IllegalArgumentException("A melodia precisa de pelo menos "
                    + GeradorDePista.MINIMO_DE_NOTAS + " notas.");
        }
        Musica musica = new Musica();
        musica.setTitulo(limitar(titulo, 80));
        musica.setGenero(limitar(genero, 40));
        musica.setBpm(Math.max(60, Math.min(200, bpm)));
        musica.setDificuldade(Math.max(1, Math.min(5, dificuldade)));
        musica.setGeradaPorIA(geradaPorIA);
        musica.setDescricao(limitar(descricao, 1000));
        musica.setPedido(limitar(pedido, 500));
        musica.setDataCriacao(LocalDateTime.now());

        long semente = musica.getTitulo().hashCode();
        for (Tile tile : geradorDePista.gerar(notas, musica.getDificuldade(), semente)) {
            musica.adicionarTile(tile);
        }
        return musicaRepository.save(musica);
    }

    /** Só permite excluir músicas criadas pela IA (as músicas iniciais ficam). */
    @Transactional
    public void excluir(Long id) {
        Musica musica = obterPeloId(id);
        if (!musica.isGeradaPorIA()) {
            throw new IllegalArgumentException("Só é possível excluir músicas criadas pela IA.");
        }
        partidaRepository.deleteAllByMusicaId(id);
        musicaRepository.delete(musica);
    }

    private String limitar(String texto, int tamanho) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        texto = texto.strip();
        return texto.length() > tamanho ? texto.substring(0, tamanho) : texto;
    }
}
