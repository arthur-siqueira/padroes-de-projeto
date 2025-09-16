import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Display {
    
    public void gerarImagem(Hidrometro h) {
        try {
            int largura = 400, altura = 200;
            BufferedImage img = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();

            // Fundo branco
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, largura, altura);

            // Número de série
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD,18));
            g.drawString("Hidrômetro Nº " + h.getNumeroSerie(), 50, 40);

            // Consumo total em litros
            g.setFont(new Font("Monospaced", Font.BOLD, 40));
            g.drawString(String.format("%06d", h.getLitrosConsumidos()), 50, 90);

            // Conversão em m³
            g.setFont(new Font("SansSerif", Font.PLAIN, 22));
            g.drawString("Litros restantes: " + h.getLitrosRestantes(), 50, 160);
            g.drawString("Décimo de litro: " + h.getDecimosLitro(), 50, 185);

            g.dispose();

            // Salvar imagem
            ImageIO.write(img, "png", new File("hidrometro_saida.png"));
            System.out.println("Imagem gerada: hidrometro_saida.png");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
