/*
 * Piano Tiles - lógica do jogo no navegador (canvas + Web Audio).
 * A pista (tiles) vem do banco pelo endpoint /api/musicas/{id}/pista
 * e o resultado é salvo em /api/partidas.
 */
(() => {
    const palco = document.getElementById('palco');
    const tela = document.getElementById('tela');
    const ctx = tela.getContext('2d');

    const musica = {
        id: Number(palco.dataset.musicaId),
        titulo: palco.dataset.titulo,
        bpm: Number(palco.dataset.bpm),
        dificuldade: Number(palco.dataset.dificuldade)
    };

    const campoNome = document.getElementById('nome-jogador');
    campoNome.value = localStorage.getItem('pt-jogador') || '';
    const nomeJogador = () => campoNome.value.trim() || 'Anônimo';

    // ---------------- Áudio: piano sintetizado ----------------
    let audio = null;
    const SEMITOM = {C: 0, D: 2, E: 4, F: 5, G: 7, A: 9, B: 11};

    function frequencia(nota) {
        const m = /^([A-G])(#?)(\d)$/.exec(nota || '');
        if (!m) return 440;
        const midi = SEMITOM[m[1]] + (m[2] ? 1 : 0) + (Number(m[3]) + 1) * 12;
        return 440 * Math.pow(2, (midi - 69) / 12);
    }

    function tocarNota(nota, duracao) {
        if (!audio) return;
        const f = frequencia(nota);
        const t = audio.currentTime;
        [1, 2, 3, 4].forEach((harmonico, i) => {
            const osc = audio.createOscillator();
            const ganho = audio.createGain();
            osc.type = i === 0 ? 'triangle' : 'sine';
            osc.frequency.value = f * harmonico;
            ganho.gain.setValueAtTime(0.0001, t);
            ganho.gain.exponentialRampToValueAtTime(0.3 / (harmonico * harmonico), t + 0.008);
            ganho.gain.exponentialRampToValueAtTime(0.0001, t + duracao / (1 + i * 0.4));
            osc.connect(ganho).connect(audio.destination);
            osc.start(t);
            osc.stop(t + duracao);
        });
    }

    function somDeErro() {
        if (!audio) return;
        const osc = audio.createOscillator();
        const ganho = audio.createGain();
        osc.type = 'sawtooth';
        osc.frequency.value = 90;
        ganho.gain.value = 0.25;
        ganho.gain.exponentialRampToValueAtTime(0.001, audio.currentTime + 0.5);
        osc.connect(ganho).connect(audio.destination);
        osc.start();
        osc.stop(audio.currentTime + 0.5);
    }

    // ---------------- Estado do jogo ----------------
    let pista = [];          // tiles vindos do servidor
    let linhas = [];         // linhas do jogo (com estado)
    let L, A, ALT_LINHA;     // largura, altura e altura de uma linha (1/4 da tela)
    let deslocamento, estado, acertos, combo, comboMaximo, efeitos, ultimoQuadro, falha, estrelas;

    function redimensionar() {
        A = window.innerHeight;
        L = Math.min(window.innerWidth, Math.round(A * 0.62));
        tela.width = L;
        tela.height = A;
        tela.style.width = L + 'px';
        tela.style.height = A + 'px';
        ALT_LINHA = A / 4;
    }
    window.addEventListener('resize', () => { redimensionar(); desenhar(); });

    async function carregarPista() {
        const resp = await fetch('/api/musicas/' + musica.id + '/pista');
        if (!resp.ok) throw new Error('Não foi possível carregar a música.');
        pista = await resp.json();
    }

    function iniciar() {
        audio = audio || new (window.AudioContext || window.webkitAudioContext)();
        audio.resume();
        localStorage.setItem('pt-jogador', campoNome.value.trim());

        let posicao = 0;
        linhas = pista.map((t, i) => {
            const altura = t.longo ? 2 : 1;
            const linha = {
                faixas: t.faixas, notas: t.notas, longo: t.longo,
                altura, posicao, tocadas: new Set(),
                concluida: false, segurando: false, ponteiro: null, inicio: i === 0
            };
            posicao += altura;
            return linha;
        });

        redimensionar();
        deslocamento = 0;
        estado = 'pronto';
        acertos = 0; combo = 0; comboMaximo = 0; estrelas = 0;
        efeitos = []; falha = null;
        ultimoQuadro = performance.now();

        document.getElementById('pre-jogo').style.display = 'none';
        document.getElementById('fim-jogo').style.display = 'none';
        document.getElementById('texto-dicas').textContent = '';
        requestAnimationFrame(loop);
    }

    // posição Y (topo) de uma linha na tela
    const topo = (linha) => A - (linha.posicao + linha.altura) * ALT_LINHA + deslocamento;

    // velocidade em pixels/segundo: depende do BPM, da dificuldade e acelera durante a música
    function velocidade() {
        const progresso = acertos / linhas.length;
        const linhasPorSegundo = (musica.bpm / 60) * (0.85 + musica.dificuldade * 0.1) * (1 + progresso * 0.35);
        return linhasPorSegundo * ALT_LINHA;
    }

    function loop(agora) {
        if (estado === 'fim') return;
        const dt = Math.min(0.05, (agora - ultimoQuadro) / 1000);
        ultimoQuadro = agora;

        if (estado === 'jogando') {
            deslocamento += velocidade() * dt;
            for (const linha of linhas) {
                if (linha.concluida) continue;
                const y = topo(linha);
                if (linha.segurando && y >= A - ALT_LINHA * 0.3) {
                    concluirLinha(linha);              // segurou o tile longo até o fim
                } else if (!linha.segurando && y >= A) {
                    return errar(linha, null);         // tile escapou pela parte de baixo
                }
            }
            if (linhas.every(l => l.concluida)) {
                return terminar(true);
            }
        }
        desenhar();
        requestAnimationFrame(loop);
    }

    function concluirLinha(linha) {
        linha.concluida = true;
        linha.segurando = false;
        acertos++;
        combo++;
        comboMaximo = Math.max(comboMaximo, combo);
        const p = acertos / linhas.length;
        estrelas = p >= 1 ? 3 : p >= 0.66 ? 2 : p >= 0.33 ? 1 : 0;
    }

    // ---------------- Entrada (mouse, toque e teclado) ----------------
    function apertar(faixa, yToque, ponteiro) {
        if (estado !== 'pronto' && estado !== 'jogando') return;
        // a linha "da vez" é a primeira que ainda não foi concluída e não está sendo segurada
        const linha = linhas.find(l => !l.concluida && !l.segurando);
        if (!linha) return;

        const indice = linha.faixas.indexOf(faixa);
        if (indice < 0 || linha.tocadas.has(faixa)) {
            return errar(linha, {faixa, y: yToque});
        }
        if (estado === 'pronto') estado = 'jogando';

        linha.tocadas.add(faixa);
        tocarNota(linha.notas[indice], linha.longo ? 2.2 : 1.4);
        efeitos.push({x: faixa * L / 4, y: Math.max(0, Math.min(A - ALT_LINHA, topo(linha) + (linha.altura - 1) * ALT_LINHA)), t: 0});

        if (linha.tocadas.size === linha.faixas.length) {
            if (linha.longo) {
                linha.segurando = true;
                linha.ponteiro = ponteiro;
            } else {
                concluirLinha(linha);
            }
        }
    }

    function soltar(ponteiro) {
        const linha = linhas.find(l => l.segurando && l.ponteiro === ponteiro);
        if (linha && estado === 'jogando') concluirLinha(linha);
    }

    tela.addEventListener('pointerdown', (e) => {
        e.preventDefault();
        const r = tela.getBoundingClientRect();
        const faixa = Math.max(0, Math.min(3, Math.floor((e.clientX - r.left) / (L / 4))));
        apertar(faixa, e.clientY - r.top, e.pointerId);
    });
    window.addEventListener('pointerup', (e) => soltar(e.pointerId));
    window.addEventListener('pointercancel', (e) => soltar(e.pointerId));

    const TECLAS = {d: 0, f: 1, j: 2, k: 3};
    window.addEventListener('keydown', (e) => {
        const faixa = TECLAS[e.key.toLowerCase()];
        if (faixa === undefined || e.repeat) return;
        if (document.activeElement === campoNome) return;
        apertar(faixa, A - ALT_LINHA / 2, 'tecla' + faixa);
    });
    window.addEventListener('keyup', (e) => {
        const faixa = TECLAS[e.key.toLowerCase()];
        if (faixa !== undefined) soltar('tecla' + faixa);
    });

    // ---------------- Fim de jogo ----------------
    function errar(linha, toque) {
        somDeErro();
        estado = 'errou';
        falha = {linha, toque, instante: performance.now()};
        const piscar = () => {
            if (estado !== 'errou') return;
            desenhar();
            requestAnimationFrame(piscar);
        };
        requestAnimationFrame(piscar);
        setTimeout(() => terminar(false), 1000);
    }

    async function terminar(venceu) {
        estado = 'fim';
        desenhar();
        document.getElementById('fim-titulo').textContent = venceu ? '🎉 Música completa!' : '😵 Você errou!';
        document.getElementById('fim-estrelas').textContent = '★'.repeat(estrelas) + '☆'.repeat(3 - estrelas);
        document.getElementById('fim-placar').textContent =
            `${acertos}/${linhas.length} tiles · combo máximo ${comboMaximo}`;
        document.getElementById('fim-jogo').style.display = 'flex';

        try {
            await fetch('/api/partidas', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    nomeJogador: nomeJogador(), musicaId: musica.id, pontos: acertos,
                    totalTiles: linhas.length, estrelas, comboMaximo, concluida: venceu
                })
            });
        } catch (e) {
            console.warn('Não foi possível salvar a partida', e);
        }
    }

    async function pedirDicas() {
        const saida = document.getElementById('texto-dicas');
        const carregando = document.getElementById('carregando-dicas');
        const botao = document.getElementById('botao-dicas');
        botao.disabled = true;
        carregando.style.display = 'block';
        saida.textContent = '';
        try {
            const resp = await fetch('/api/treinador', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({jogador: nomeJogador()})
            });
            const dados = await resp.json();
            saida.textContent = resp.ok ? dados.analise : ('Erro: ' + dados.mensagem);
        } catch (e) {
            saida.textContent = 'Erro ao falar com a IA: ' + e.message;
        }
        carregando.style.display = 'none';
        botao.disabled = false;
    }

    // ---------------- Desenho ----------------
    function desenhar() {
        if (!linhas.length) return;
        const LF = L / 4;
        ctx.fillStyle = '#fff';
        ctx.fillRect(0, 0, L, A);

        ctx.strokeStyle = '#ddd';
        ctx.lineWidth = 2;
        for (let i = 1; i < 4; i++) {
            ctx.beginPath(); ctx.moveTo(i * LF, 0); ctx.lineTo(i * LF, A); ctx.stroke();
        }

        const piscando = falha && Math.floor((performance.now() - falha.instante) / 150) % 2 === 0;

        for (const linha of linhas) {
            const y = topo(linha);
            const h = linha.altura * ALT_LINHA;
            if (y > A || y + h < 0) continue;

            for (const faixa of linha.faixas) {
                const x = faixa * LF;
                const tocada = linha.tocadas.has(faixa);
                const escapou = falha && falha.linha === linha && !falha.toque && piscando;

                ctx.fillStyle = escapou ? '#e53935' : tocada ? 'rgba(120,170,255,.45)' : '#111';
                ctx.fillRect(x + 1, y + 1, LF - 2, h - 2);

                if (linha.longo && !tocada) {
                    ctx.fillStyle = '#fff';
                    ctx.fillRect(x + LF / 2 - 3, y + 10, 6, h - 20);
                    ctx.fillStyle = '#ffd23f';
                    ctx.beginPath(); ctx.arc(x + LF / 2, y + h - ALT_LINHA * 0.25, LF * 0.14, 0, Math.PI * 2); ctx.fill();
                }
                if (linha.longo && linha.segurando) {
                    ctx.fillStyle = 'rgba(60,120,255,.85)';
                    const preenchido = Math.min(h, Math.max(0, A - ALT_LINHA * 0.3 - y));
                    ctx.fillRect(x + 1, y + h - preenchido, LF - 2, preenchido);
                }
                if (linha.inicio && !tocada) {
                    ctx.fillStyle = '#fff';
                    ctx.font = `bold ${Math.round(LF * 0.2)}px system-ui`;
                    ctx.textAlign = 'center';
                    ctx.fillText('INICIAR', x + LF / 2, y + h / 2 + 6);
                }
            }
        }

        if (falha && falha.toque && piscando) {
            const x = falha.toque.faixa * LF;
            const y = Math.min(A - ALT_LINHA, Math.max(0, falha.toque.y - ALT_LINHA / 2));
            ctx.fillStyle = 'rgba(229,57,53,.9)';
            ctx.fillRect(x + 1, y, LF - 2, ALT_LINHA);
        }

        efeitos = efeitos.filter(f => (f.t += 0.05) < 1);
        for (const f of efeitos) {
            ctx.strokeStyle = `rgba(60,120,255,${1 - f.t})`;
            ctx.lineWidth = 4;
            ctx.strokeRect(f.x + 4 - f.t * 8, f.y - f.t * 8, LF - 8 + f.t * 16, ALT_LINHA + f.t * 16);
        }

        // placar no topo
        ctx.fillStyle = 'rgba(0,0,0,.75)';
        ctx.fillRect(0, 0, L, 64);
        ctx.fillStyle = '#fff';
        ctx.textAlign = 'center';
        ctx.font = 'bold 34px system-ui';
        ctx.fillText(String(acertos), L / 2, 40);
        ctx.font = '12px system-ui';
        ctx.fillText(musica.titulo + (combo > 2 ? '  ·  combo x' + combo : ''), L / 2, 58);
        ctx.textAlign = 'right';
        ctx.font = '22px system-ui';
        for (let i = 0; i < 3; i++) {
            ctx.fillStyle = i < estrelas ? '#ffd23f' : '#555';
            ctx.fillText('★', L - 10 - (2 - i) * 24, 30);
        }
        ctx.fillStyle = '#333';
        ctx.fillRect(0, 64, L, 5);
        ctx.fillStyle = '#4f8cff';
        ctx.fillRect(0, 64, L * acertos / linhas.length, 5);
    }

    // ---------------- Botões ----------------
    const botaoComecar = document.getElementById('botao-comecar');
    botaoComecar.disabled = true;
    botaoComecar.textContent = 'Carregando...';
    carregarPista()
        .then(() => { botaoComecar.disabled = false; botaoComecar.textContent = 'Começar'; })
        .catch((e) => { botaoComecar.textContent = e.message; });

    botaoComecar.addEventListener('click', iniciar);
    document.getElementById('botao-repetir').addEventListener('click', iniciar);
    document.getElementById('botao-dicas').addEventListener('click', pedirDicas);
})();
