package br.com.cefras.hidrometro.core;

import java.util.Random;

public class Hidrometro {
    private final String numeroSerie;
    private double litrosConsumidos;
    private double vazaoM3h;
    private double pressaoBar;
    private final double pressaoNormal;
    private String statusRede;
    private final double probabilidadeFaltaAgua;
    private final double probabilidadeArTubulacao;
    private final double fatorMultiplicadorAr;
    private final Random random = new Random();

    private boolean emEvento = false;
    private int duracaoEventoEmTicks = 0;

    public Hidrometro(String numeroSerie, double vazaoInicial, double pressaoNormal, double probFaltaAgua, double probAr, double fatorAr) {
        this.numeroSerie = numeroSerie;
        this.litrosConsumidos = 0;
        this.vazaoM3h = vazaoInicial;
        this.pressaoNormal = pressaoNormal;
        this.pressaoBar = pressaoNormal;
        this.probabilidadeFaltaAgua = probFaltaAgua;
        this.probabilidadeArTubulacao = probAr;
        this.fatorMultiplicadorAr = fatorAr;
        this.statusRede = "NORMAL";
    }

    public void atualizarSimulacao(double intervaloSegundos) {
        // --- INÍCIO DA LÓGICA DE ESTADO CORRIGIDA ---

        if (emEvento) {
            duracaoEventoEmTicks--;
            if (duracaoEventoEmTicks <= 0) {
                emEvento = false;
                // O status será definido como NORMAL no bloco else abaixo.
            }
        }

        // Se não estamos em um evento, ou um acabou de terminar.
        if (!emEvento) {
            // Verificamos a chance de um novo evento começar.
            if (random.nextDouble() < this.probabilidadeFaltaAgua) {
                this.statusRede = "SEM FLUXO";
                emEvento = true;
                duracaoEventoEmTicks = random.nextInt(50) + 50; // 5 a 10 segundos
            } else if (random.nextDouble() < this.probabilidadeArTubulacao) {
                this.statusRede = "AR NA TUBULAÇÃO";
                emEvento = true;
                duracaoEventoEmTicks = random.nextInt(40) + 30; // 3 a 7 segundos
            } else {
                // CORREÇÃO CRÍTICA: Se nenhum evento começar, garantimos que o status é NORMAL.
                this.statusRede = "NORMAL";
            }
        }

        // --- FIM DA LÓGICA DE ESTADO CORRIGIDA ---

        double vazaoAtual = this.vazaoM3h;

        switch (this.statusRede) {
            case "SEM FLUXO":
                vazaoAtual = 0;
                this.pressaoBar = 0;
                break;
            case "AR NA TUBULAÇÃO":
                this.pressaoBar = this.pressaoNormal * (0.5 + random.nextDouble());
                break;
            case "NORMAL":
            default:
                this.pressaoBar = this.pressaoNormal;
                break;
        }

        double flutuacao = (random.nextDouble() - 0.5) * (vazaoAtual * 0.1);
        double vazaoComFlutuacao = Math.max(0, vazaoAtual + flutuacao);
        double litrosNoIntervalo = (vazaoComFlutuacao * 1000.0 / 3600.0) * intervaloSegundos;

        if (this.statusRede.equals("AR NA TUBULAÇÃO")) {
            litrosNoIntervalo *= this.fatorMultiplicadorAr;
        }

        this.litrosConsumidos += litrosNoIntervalo;
    }

    // Métodos de aumentar/diminuir vazão e getters permanecem os mesmos
    public void aumentarVazao() { this.vazaoM3h += 0.1; }
    public void diminuirVazao() { this.vazaoM3h = Math.max(0, this.vazaoM3h - 0.1); }
    public String getNumeroSerie() { return numeroSerie; }
    public long getMetrosCubicos() { return (long) (litrosConsumidos / 1000.0); }
    public double getLitrosConsumidos() { return litrosConsumidos; }
    public double getVazaoM3h() { return vazaoM3h; }
    public double getPressaoBar() { return pressaoBar; }
    public String getStatusRede() { return statusRede; }
}