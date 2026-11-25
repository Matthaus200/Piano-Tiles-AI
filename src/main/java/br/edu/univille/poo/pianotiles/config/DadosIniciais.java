package br.edu.univille.poo.pianotiles.config;

import br.edu.univille.poo.pianotiles.service.GeradorDePista;
import br.edu.univille.poo.pianotiles.service.MusicaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Cadastra algumas músicas de domínio público na primeira vez que o sistema sobe.
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final MusicaService musicaService;
    private final GeradorDePista geradorDePista;

    public DadosIniciais(MusicaService musicaService, GeradorDePista geradorDePista) {
        this.musicaService = musicaService;
        this.geradorDePista = geradorDePista;
    }

    @Override
    public void run(String... args) {
        if (musicaService.contar() > 0) {
            return;
        }
        cadastrar("Brilha, Brilha Estrelinha", "Infantil", 100, 1,
                "Uma canção de ninar para aquecer os dedos.",
                "C4 C4 G4 G4 A4 A4 G4 F4 F4 E4 E4 D4 D4 C4 G4 G4 F4 F4 E4 E4 D4 "
                        + "G4 G4 F4 F4 E4 E4 D4 C4 C4 G4 G4 A4 A4 G4 F4 F4 E4 E4 D4 D4 C4");
        cadastrar("Jingle Bells", "Natal", 115, 2,
                "Clássico natalino com ritmo animado.",
                "E4 E4 E4 E4 E4 E4 E4 G4 C4 D4 E4 F4 F4 F4 F4 F4 E4 E4 E4 E4 D4 D4 E4 D4 G4 "
                        + "E4 E4 E4 E4 E4 E4 E4 G4 C4 D4 E4 F4 F4 F4 F4 F4 E4 E4 E4 G4 G4 F4 D4 C4");
        cadastrar("Ode à Alegria", "Clássica", 125, 3,
                "Beethoven, 9ª Sinfonia. Atenção aos tiles duplos!",
                "E4 E4 F4 G4 G4 F4 E4 D4 C4 C4 D4 E4 E4 D4 D4 E4 E4 F4 G4 G4 F4 E4 D4 C4 C4 D4 E4 D4 C4 C4 "
                        + "D4 D4 E4 C4 D4 E4 F4 E4 C4 D4 E4 F4 E4 D4 C4 D4 G3");
        cadastrar("Für Elise", "Clássica", 145, 4,
                "Beethoven em velocidade de desafio.",
                "E5 D#5 E5 D#5 E5 B4 D5 C5 A4 C4 E4 A4 B4 E4 G#4 B4 C5 E4 E5 D#5 E5 D#5 E5 B4 D5 C5 A4 "
                        + "C4 E4 A4 B4 E4 C5 B4 A4 B4 C5 D5 E5 G4 F5 E5 D5 F4 E5 D5 C5 E4 D5 C5 B4");
    }

    private void cadastrar(String titulo, String genero, int bpm, int dificuldade, String descricao, String melodia) {
        musicaService.criarMusica(titulo, genero, bpm, dificuldade,
                geradorDePista.extrairNotas(melodia), false, descricao, null);
    }
}
