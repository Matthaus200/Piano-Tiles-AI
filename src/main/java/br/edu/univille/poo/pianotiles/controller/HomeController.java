package br.edu.univille.poo.pianotiles.controller;

import br.edu.univille.poo.pianotiles.model.Musica;
import br.edu.univille.poo.pianotiles.service.GenAIService;
import br.edu.univille.poo.pianotiles.service.MusicaService;
import br.edu.univille.poo.pianotiles.service.PartidaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Telas do sistema (Thymeleaf).
 */
@Controller
public class HomeController {

    private final MusicaService musicaService;
    private final PartidaService partidaService;
    private final GenAIService genAIService;

    public HomeController(MusicaService musicaService, PartidaService partidaService, GenAIService genAIService) {
        this.musicaService = musicaService;
        this.partidaService = partidaService;
        this.genAIService = genAIService;
    }

    @GetMapping("/")
    public ModelAndView index() {
        var mv = new ModelAndView("index");
        mv.addObject("musicas", musicaService.listarTodas());
        mv.addObject("ranking", partidaService.rankingGeral());
        return mv;
    }

    @GetMapping("/jogar/{id}")
    public ModelAndView jogar(@PathVariable Long id) {
        Musica musica = musicaService.obterPeloId(id);
        var mv = new ModelAndView("jogar");
        mv.addObject("musica", musica);
        mv.addObject("ranking", partidaService.rankingDaMusica(id));
        return mv;
    }

    @GetMapping("/historico")
    public ModelAndView historico(@RequestParam(required = false) String jogador) {
        String nome = PartidaService.normalizarNome(jogador);
        var mv = new ModelAndView("historico");
        mv.addObject("jogador", nome);
        mv.addObject("partidas", partidaService.historico(nome));
        return mv;
    }

    /** Compositor IA: recebe o pedido do formulário e cria uma fase nova. */
    @PostMapping("/musicas/gerar")
    public String gerarMusica(@RequestParam String pedido,
                              @RequestParam(defaultValue = "2") int dificuldade,
                              RedirectAttributes redirect) {
        try {
            Musica musica = genAIService.comporMusica(pedido, dificuldade);
            redirect.addFlashAttribute("sucesso",
                    "A IA compôs \"" + musica.getTitulo() + "\" (" + musica.getTiles().size() + " tiles). Bora jogar!");
            redirect.addFlashAttribute("novaMusicaId", musica.getId());
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", "Não foi possível gerar a música: " + mensagem(e));
        }
        return "redirect:/";
    }

    @PostMapping("/musicas/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            musicaService.excluir(id);
            redirect.addFlashAttribute("sucesso", "Música excluída.");
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", mensagem(e));
        }
        return "redirect:/";
    }

    private String mensagem(Exception e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
