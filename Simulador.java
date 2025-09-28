import java.util.Random;

public class Simulador {
    public static void main(String[] args) {
        Hidrometro h = new Hidrometro("12345-ABC");
        Display d = new Display();
        Random rand = new Random();

        // Loop infinito simulando consumo de água
        while (true) {
            // Simula um consumo entre 1 e 20 litros
            int consumo = rand.nextInt(20) + 1;
            h.consumirAgua(consumo);

            // Atualiza o display
            d.gerarImagem(h);

            try {
                Thread.sleep(2000); // espera 2 segundos antes do próximo ciclo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    
}
