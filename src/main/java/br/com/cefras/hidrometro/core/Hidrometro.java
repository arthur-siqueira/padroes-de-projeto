package br.com.cefras.hidrometro.core;

import java.util.Random;

public class Hidrometro {
    //Atributos originais
    private final String numeroSerie;
    private double litrosConsumidos; //Alterado para double para maior precisao

    //Novos atributos da simulacao
    private double vazaoM3h; //Vazao em m³/h
    private double pressaoBar; //Pressão em bar
    private double pressaoNormal;
    private String statusRede;

    //Parametros de eventos
    private final double probabilidadeFaltaAgua;
    private final double probabilidadeArTubulacao;
    private final double fatorMultiplicadorAr;

    private final Random random = new Random();

    private boolean emEvento = false;
    private int duracaoEventoEmTicks = 0;

    public Hidrometro(String numeroSerie, double vazaoInicial, double pressaoInicial, double probFaltaAgua, double probAr, double fatorAr) {
        this.numeroSerie = numeroSerie;
        this.litrosConsumidos = 0;
        this.vazaoM3h = vazaoInicial;
        this.pressaoNormal = pressaoNormal;
        this.pressaoBar = pressaoInicial;
        this.probabilidadeFaltaAgua = probFaltaAgua;
        this.probabilidadeArTubulacao = probAr;
        this.fatorMultiplicadorAr = fatorAr;
        this.statusRede = "NORMAL";
    }

    //Metodo principal que atualiza o estado da simulacao a cada ciclo
    public void atualizarSimulacao(double intervaloSegundos) {
        if (emEvento) {
            // Se já estamos em um evento, apenas decrementamos sua duração.
            duracaoEventoEmTicks--;
            if (duracaoEventoEmTicks <= 0) {
                emEvento = false;
                this.statusRede = "NORMAL";
            }
        } else {
            // Se não estamos em um evento, sorteamos se um novo começa.
            if (random.nextDouble() < this.probabilidadeFaltaAgua) { // O sistema deve ser capaz de simular eventos aleatórios de falta de água.
                this.statusRede = "SEM FLUXO";
                emEvento = true;
                duracaoEventoEmTicks = random.nextInt(50) + 50; // Evento dura de 5 a 10 segundos (50-100 ticks de 100ms)
            } else if (random.nextDouble() < this.probabilidadeArTubulacao) { // O sistema deve ser capaz de simular a passagem de ar.
                this.statusRede = "AR NA TUBULAÇÃO";
                emEvento = true;
                duracaoEventoEmTicks = random.nextInt(40) + 30; // Evento dura de 3 a 7 segundos
            }
        }

        double vazaoAtual = this.vazaoM3h;

        switch (this.statusRede) {
            case "SEM FLUXO":
                vazaoAtual = 0;
                this.pressaoBar = 0; //Pressao cai para zero
                break;
            case "AR NA TUBULAÇÃO":
                //Com ar, a pressao pode ficar instavel
                this.pressaoBar = this.pressaoNormal * (0.5 + random.nextDouble()); //Flutua entre 50% e 150% do normal
                break;
            case "NORMAL":
            default:
                this.pressaoBar = this.pressaoNormal; //Pressao volta ao normal
                break;
        }

        double flutuacao = (random.nextDouble() - 0.5) * (vazaoAtual * 0.1); //Flutuacao de ate 5% da vazao atual
        double vazaoComFlutuacao = Math.max(0, vazaoAtual + flutuacao);

        //O calculo agora usa a vazão com a flutuação.
        double litrosNoIntervalo = (vazaoComFlutuacao * 1000.0 / 3600.0) * intervaloSegundos;

        if (statusRede.equals("AR NA TUBULAÇÃO")) {
            litrosNoIntervalo *= this.fatorMultiplicadorAr;
        }

        this.litrosConsumidos += litrosNoIntervalo;
        //this.pressaoBar = pressaoAtual; //Atualiza a pressao para o display
    }

    //Metodos para controle interativo da vazao
    public void aumentarVazao() { this.vazaoM3h += 0.1; }
    public void diminuirVazao() { this.vazaoM3h = Math.max(0, this.vazaoM3h - 0.1); }

    //Getters para a GUI
    public String getNumeroSerie() { return numeroSerie; }
    public long getMetrosCubicos() { return (long) (litrosConsumidos / 1000.0); }
    public double getLitrosConsumidos() { return litrosConsumidos; }
    public double getVazaoM3h() { return vazaoM3h; }
    public double getPressaoBar() { return pressaoBar; }
    public String getStatusRede() { return statusRede; }
}