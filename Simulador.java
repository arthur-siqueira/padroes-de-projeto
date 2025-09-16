public class Simulador {
    public static void main(String[] args) {
        Hidrometro h = new Hidrometro("12345-ABC");
        Display d = new Display();

        // Simulação de consumo
        h.consumirAgua(50);
        h.consumirAgua(12);
        h.consumirAgua(120);

        // Gera a imagem com display
        d.gerarImagem(h);
    }
    
}
