package br.com.cefras.hidrometro.gui;

import br.com.cefras.hidrometro.core.Hidrometro;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

//A GUI deve ser implementada utilizando a biblioteca Java Swing.
public class Display extends JFrame {

    private final DisplayPanel displayPanel;

    public Display(Hidrometro hidrometro) {
        setTitle("Simulador de Hidrômetro - " + hidrometro.getNumeroSerie());
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        displayPanel = new DisplayPanel(hidrometro);
        add(displayPanel, BorderLayout.CENTER);

        //Painel com botoes para controle interativo da vazao.
        JPanel controlPanel = new JPanel();
        JButton btnAumentar = new JButton("Aumentar Vazão");
        btnAumentar.addActionListener(e -> hidrometro.aumentarVazao());
        JButton btnDiminuir = new JButton("Diminuir Vazão");
        btnDiminuir.addActionListener(e -> hidrometro.diminuirVazao());
        controlPanel.add(btnAumentar);
        controlPanel.add(btnDiminuir);
        add(controlPanel, BorderLayout.SOUTH);
    }

    //Metodo para capturar a imagem da GUI para salvamento.
    public BufferedImage getScreenshot() {
        BufferedImage image = new BufferedImage(displayPanel.getWidth(), displayPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        displayPanel.paint(g2d);
        g2d.dispose();
        return image;
    }

    private static class DisplayPanel extends JPanel {
        private final Hidrometro hidrometro;

        //PAINEL DE CONFIGURACAO DA INTERFACE
        //Configuracao da Leitura Numerica
        private static final int LEITURA_BASE_X = 147;
        private static final int LEITURA_BASE_Y = 242;
        private static final int LEITURA_ESPACAMENTO_DIGITOS = 25;
        private static final int LEITURA_ESPACAMENTO_VIRGULA = 2;

        //Configuracao do Ponteiro de VAZAO
        private static final int DIAL_CENTRO_X = 323;
        private static final int DIAL_CENTRO_Y = 299;
        private static final int DIAL_RAIO = 23;

        //Configuracao do Ponteiro de PRESSAO
        private static final int PRESSAO_CENTRO_X = 241;
        private static final int PRESSAO_CENTRO_Y = 300;
        private static final int PRESSAO_RAIO = 23;
        private static final double PRESSAO_VALOR_MAX = 10.0; //Escala de 0 a 10 bar
        private static final int PRESSAO_ANGULO_INICIAL = -135; //Posicao do ponteiro em 0 bar
        private static final int PRESSAO_ANGULO_FINAL = 0;

        //Configuracao dos Textos de Status
        private static final int STATUS_POS_X = 199;
        private static final int STATUS_POS_Y = 175;
        private static final int VAZAO_TEXTO_POS_X = 189;
        private static final int VAZAO_TEXTO_POS_Y = 342;
        private static final int PRESSAO_TEXTO_POS_X = 208;
        private static final int PRESSAO_TEXTO_POS_Y = 355;
        //FIM DO PAINEL DE CONFIGURACAO

        private Image backgroundImage;

        //VARIAVEIS PARA ANIMACAO
        private double consumoExibido; //Valor de consumo usado para desenhar
        private long ultimaAtualizacaoNs; //Guarda o tempo da ultima atualizacao para calcular o delta
        private double anguloFluxo;


        public DisplayPanel(Hidrometro hidrometro) {
            this.hidrometro = hidrometro;

            //Inicializa o valor de exibicao com o valor real do hidrometro.
            this.consumoExibido = hidrometro.getLitrosConsumidos();
            this.ultimaAtualizacaoNs = System.nanoTime();

            //Carrega a imagem de fundo.
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("hidrometro_bg.jpeg")) {
                if (is != null) {
                    this.backgroundImage = ImageIO.read(is);
                } else {
                    System.err.println("Imagem 'background.png' não encontrada nos recursos.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Timer para animacao fluida a ~60 FPS.
            Timer animationTimer = new Timer(17, e -> repaint());
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            //LOGICA DE ATUALIZACAO SUAVE
            long tempoAtualNs = System.nanoTime();
            //Calcula o tempo que passou desde a ultima frame, em segundos.
            double deltaTime = (tempoAtualNs - ultimaAtualizacaoNs) / 1000000000.0;
            this.ultimaAtualizacaoNs = tempoAtualNs;

            //Pega o valor "real" do modelo.
            double consumoReal = hidrometro.getLitrosConsumidos();

            //Calcula a taxa de fluxo em litros por segundo. 1 m³/h = 1000L / 3600s.
            double litrosPorSegundo = hidrometro.getVazaoM3h() / 3.6;

            //Calcula o incremento que deveria ter ocorrido nesse pequeno intervalo de tempo.
            double incremento = litrosPorSegundo * deltaTime;

            //Atualiza o valor de exibicao.
            //Se o valor de exibicao ficou para tras do valor real, ele o alcanca rapidamente.
            //Senao, ele avanca suavemente com base no fluxo.
            if (consumoExibido < consumoReal) {
                consumoExibido = Math.min(consumoReal, consumoExibido + incremento);
            } else {
                consumoExibido = consumoReal;
            }
            //FIM DA NOVA LOGICA DE ATUALIZAÇÃO SUAVE

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            //Os metodos de desenho agora usam o valor "consumoExibido" que eh suave.
            desenharLeituraNumerica(g2d, consumoExibido);
            desenharPonteiros(g2d, consumoExibido);
            desenharStatus(g2d);
        }

        //Os metodos de desenho agora recebem o valor a ser exibido como parametro.
        private void desenharLeituraNumerica(Graphics2D g, double consumo) {
            g.setFont(new Font("Monospaced", Font.BOLD, 29));
            long m3 = (long) (consumo / 1000.0);
            int litros = (int) (consumo % 1000);
            String m3Str = String.format("%05d", m3);
            String litrosStr = String.format("%03d", litros);

            int currentX = LEITURA_BASE_X;

            //Desenha a parte dos metros cubicos(preto)
            g.setColor(Color.BLACK);
            for (char c : m3Str.toCharArray()) {
                g.drawString(String.valueOf(c), currentX, LEITURA_BASE_Y);
                currentX += LEITURA_ESPACAMENTO_DIGITOS;
            }

            currentX += LEITURA_ESPACAMENTO_VIRGULA / 2;
            g.drawString("", currentX, LEITURA_BASE_Y - 5);
            currentX += LEITURA_ESPACAMENTO_VIRGULA - 3;

            //Desenha a parte dos litros (vermelho)
            g.setColor(Color.RED);
            for (char c : litrosStr.toCharArray()) {
                g.drawString(String.valueOf(c), currentX, LEITURA_BASE_Y);
                currentX += LEITURA_ESPACAMENTO_DIGITOS - 1;
            }
        }

        private void desenharStatus(Graphics2D g) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString("Status: " + hidrometro.getStatusRede(), STATUS_POS_X, STATUS_POS_Y);
            //O texto da vazao e pressao ainda usa o valor "oficial" do hidrometro.
            java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
            g.drawString("Vazão: " + df.format(hidrometro.getVazaoM3h()) + " m³/h", VAZAO_TEXTO_POS_X, VAZAO_TEXTO_POS_Y);
            g.drawString("Pressão: " + df.format(hidrometro.getPressaoBar()) + " bar", PRESSAO_TEXTO_POS_X, PRESSAO_TEXTO_POS_Y);
        }

        private void desenharPonteiros(Graphics2D g2d, double consumo) {
            //Ponteiro do Dial (1 volta por litro)
            double fracaoDoLitro = consumo % 1.0;
            double anguloDial = fracaoDoLitro * 360;
            desenharPonteiro(g2d, DIAL_CENTRO_X, DIAL_CENTRO_Y, DIAL_RAIO, Math.toRadians(anguloDial - 90), Color.RED, 2);

            //Ponteiro de Pressao (1 volta por 100ml)
            double anguloPressao = mapValueToAngle(
                    hidrometro.getPressaoBar(),
                    PRESSAO_VALOR_MAX,
                    PRESSAO_ANGULO_INICIAL,
                    PRESSAO_ANGULO_FINAL
            );
            desenharPonteiro(g2d, PRESSAO_CENTRO_X, PRESSAO_CENTRO_Y, PRESSAO_RAIO, anguloPressao, Color.GREEN, 2);
        }

        //Helper para mapear um valor (ex: pressao) para um angulo de ponteiro.
        private double mapValueToAngle(double value, double maxValue, int startAngle, int endAngle) {
            value = Math.max(0, Math.min(value, maxValue)); //Garante que o valor esteja entre 0 e o maximo
            double range = endAngle - startAngle;
            double percent = value / maxValue;
            return Math.toRadians(startAngle + (percent * range));
        }

        private void desenharPonteiro(Graphics2D g, int centerX, int centerY, int raio, double anguloRad, Color cor, int espessura) {
            int endX = (int) (centerX + raio * Math.cos(anguloRad));
            int endY = (int) (centerY + raio * Math.sin(anguloRad));
            g.setColor(cor);
            g.setStroke(new BasicStroke(espessura, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(centerX, centerY, endX, endY);
        }
    }
}