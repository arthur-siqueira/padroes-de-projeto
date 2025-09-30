package br.com.cefras.hidrometro;

import java.io.FileReader;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;

public class Simulador {

    public static void main(String[] args) {
        //Carrega as configuracoes do arquivo .properties
        Properties config = new Properties();
        try (FileReader reader = new FileReader("src/main/resources/parametros.properties")) {
            config.load(reader);
        } catch (IOException e) {
            System.err.println("Erro: Não foi possível carregar o arquivo 'parametros.properties'.");
            e.printStackTrace();
            return;
        }

        //INICIO DA MODIFICACAO
        Scanner scanner = new Scanner(System.in);
        int numeroDeSimuladores = 0;

        while (true) {
            try {
                System.out.print("Digite a quantidade de hidrômetros a simular (1 a 5): ");
                numeroDeSimuladores = scanner.nextInt();

                if (numeroDeSimuladores >= 1 && numeroDeSimuladores <= 5) {
                    break; //Sai do loop se ah entrada for valida
                } else {
                    System.out.println("Erro: Por favor, insira um número entre 1 e 5.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erro: Entrada inválida. Por favor, digite um número inteiro.");
                scanner.next(); //Limpa o buffer do scanner
            }
        }
        scanner.close();

        System.out.println("\nIniciando " + numeroDeSimuladores + " simulador(es)...");

        //Instancia e inicia o número de simuladores escolhido pelo usuário
        for (int i = 1; i <= numeroDeSimuladores; i++) {
            String numeroSerie = "HIDROMETRO-SN-00" + i;
            SimuladorInstancia instancia = new SimuladorInstancia(numeroSerie, config);
            Thread threadSimulador = new Thread(instancia);
            threadSimulador.setName("SimuladorThread-" + i);
            threadSimulador.start();
        }
    }
}