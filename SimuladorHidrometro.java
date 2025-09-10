import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class SimuladorHidrometro {
    private int litrosConsumidos = 0;

    public void consumirAgua(int litros) {
        litrosConsumidos += litros;
    }

    public void gerarImagem() {
        try {
            // Cria imagem em branco
            int largura = 400, altura = 200;
            BufferedImage img = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = img.createGraphics();

            // Fundo branco
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, largura, altura);

            // Texto preto (m³)
            int m3 = litrosConsumidos / 1000;
            int litrosRestantes = litrosConsumidos % 1000;
            int decimos = litrosRestantes % 10;

            g.setColor(Color.BLACK);
            g.setFont(new Font("Monospaced", Font.BOLD, 40));
            g.drawString(String.format("%06d", litrosConsumidos), 50, 80);

            g.setFont(new Font("SansSerif", Font.PLAIN, 28));
            g.drawString(String.format("%.2f m³", litrosConsumidos / 1000.0), 50, 120);

            g.setFont(new Font("SansSerif", Font.PLAIN, 22));
            g.drawString("Litros restantes: " + litrosRestantes, 50, 150);
            g.drawString("Décimos de litro: " + decimos, 50, 180);

            g.dispose();

            // Salva a imagem
            ImageIO.write(img, "png", new File("hidrometro_saida.png"));
            System.out.println("Imagem gerada: hidrometro_saida.png");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Simulação
    public static void main(String[] args) {
        SimuladorHidrometro h = new SimuladorHidrometro();

        // Consumos simulados
        h.consumirAgua(50);
        h.consumirAgua(12);
        h.consumirAgua(120);

        h.gerarImagem();
    }
}
