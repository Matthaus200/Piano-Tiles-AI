package br.edu.univille.poo.pianotiles;

import br.edu.univille.poo.pianotiles.model.Tile;
import br.edu.univille.poo.pianotiles.service.GeradorDePista;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Testes da regra que transforma a melodia em pista (não precisa de IA nem de banco). */
class GeradorDePistaTests {

    private final GeradorDePista gerador = new GeradorDePista();

    @Test
    void extraiSomenteNotasValidas() {
        List<String> notas = gerador.extrairNotas("C4, e4 G4 X9 F#5 H2 C7 \"A3\"");
        assertEquals(List.of("C4", "E4", "G4", "F#5", "A3"), notas);
    }

    @Test
    void naoRepeteFaixaSeguida() {
        List<String> notas = gerador.extrairNotas("C4 D4 E4 F4 G4 A4 B4 C5 C4 D4 E4 F4 G4 A4 B4 C5 C4 D4 E4 F4");
        List<Tile> pista = gerador.gerar(notas, 4, 42);

        assertEquals(notas.size(), pista.size());
        for (int i = 1; i < pista.size(); i++) {
            assertNotEquals(pista.get(i - 1).getFaixa1(), pista.get(i).getFaixa1());
        }
        for (Tile t : pista) {
            assertTrue(t.getFaixa1() >= 0 && t.getFaixa1() <= 3);
            if (t.isDuplo()) {
                assertNotEquals(t.getFaixa1(), t.getFaixa2());
                assertFalse(t.isLongo());
            }
        }
    }

    @Test
    void dificuldadeFacilNaoTemTileDuplo() {
        List<String> notas = gerador.extrairNotas("C4 D4 E4 F4 G4 A4 B4 C5 C4 D4 E4 F4 G4 A4 B4 C5");
        assertTrue(gerador.gerar(notas, 1, 7).stream().noneMatch(Tile::isDuplo));
    }
}
