package br.edu.univille.poo.pianotiles.service;

import br.edu.univille.poo.pianotiles.model.Tile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transforma uma melodia (lista de notas) na pista do jogo, distribuindo as notas nas 4 faixas.
 * Regras (parecidas com o Piano Tiles 2):
 *  - nunca repete a faixa da linha anterior;
 *  - de tempos em tempos coloca um tile longo (segurar);
 *  - a partir da dificuldade 3 aparecem tiles duplos (dois ao mesmo tempo).
 */
@Component
public class GeradorDePista {

    /** Nota válida: letra A-G, sustenido opcional e oitava de 2 a 6. Ex: C4, F#5. */
    private static final Pattern NOTA = Pattern.compile("(?<![A-Za-z#])([A-G]#?)([2-6])(?![0-9])");

    public static final int MINIMO_DE_NOTAS = 12;
    public static final int MAXIMO_DE_NOTAS = 120;

    /** Extrai as notas válidas de um texto qualquer (ex: "C4 E4 G4" ou "C4, E4, G4"). */
    public List<String> extrairNotas(String texto) {
        List<String> notas = new ArrayList<>();
        if (texto == null) {
            return notas;
        }
        Matcher m = NOTA.matcher(texto.toUpperCase());
        while (m.find() && notas.size() < MAXIMO_DE_NOTAS) {
            notas.add(m.group(1) + m.group(2));
        }
        return notas;
    }

    public List<Tile> gerar(List<String> notas, int dificuldade, long semente) {
        Random random = new Random(semente);
        List<Tile> pista = new ArrayList<>();
        int faixaAnterior = -1;
        int intervaloLongo = dificuldade <= 2 ? 7 : 9;

        for (int i = 0; i < notas.size(); i++) {
            String nota = notas.get(i);

            int faixa;
            do {
                faixa = random.nextInt(4);
            } while (faixa == faixaAnterior);

            boolean longo = i > 0 && (i + 1) % intervaloLongo == 0;
            boolean duplo = !longo && dificuldade >= 3 && i > 0 && i % (8 - dificuldade) == 0;

            if (duplo) {
                int faixa2;
                do {
                    faixa2 = random.nextInt(4);
                } while (faixa2 == faixa);
                pista.add(new Tile(faixa, nota, faixa2, abaixarOitava(nota), false));
            } else {
                pista.add(new Tile(faixa, nota, null, null, longo));
            }
            faixaAnterior = faixa;
        }
        return pista;
    }

    /** Nota do acompanhamento do tile duplo: a mesma nota uma oitava abaixo (quando possível). */
    private String abaixarOitava(String nota) {
        int oitava = nota.charAt(nota.length() - 1) - '0';
        if (oitava <= 2) {
            return nota;
        }
        return nota.substring(0, nota.length() - 1) + (oitava - 1);
    }
}
