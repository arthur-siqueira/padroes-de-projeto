package br.com.cefras.hidrometro;

import br.com.cefras.hidrometro.core.Hidrometro;
import br.com.cefras.hidrometro.gui.Display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

//O simulador deve rodar em uma Thread separada da Thread da interface grafica
public class SimuladorInstancia implements Runnable {

    private final Hidrometro hidrometro;
    private final Display display;
    private final String matricula;
    private long ultimoM3Salvo = -1;
    private int contadorImagens = 1;

    public SimuladorInstancia(String numeroSerie, Properties config) {
        //O simulador deve ler seus parametros de funcionamento de um arquivo
        this.matricula = config.getProperty("matricula.suap");
        double vazaoInicial = Double.parseDouble(config.getProperty("vazao.inicial.m3h"));
        double pressaoNormal = Double.parseDouble(config.getProperty("pressao.normal.bar"));
        double probFaltaAgua = Double.parseDouble(config.getProperty("probabilidade.falta.agua"));
        double probAr = Double.parseDouble(config.getProperty("probabilidade.ar.tubulacao"));
        double fatorAr = Double.parseDouble(config.getProperty("fator.multiplicador.ar"));

        this.hidrometro = new Hidrometro(numeroSerie, vazaoInicial, pressaoNormal, probFaltaAgua, probAr, fatorAr);
        this.display = new Display(hidrometro);
    }

    @Override
    public void run() {
        display.setVisible(true);

        while (true) {
            try {
                //Atualiza a lagica da simulacao
                hidrometro.atualizarSimulacao(0.1); //Simula a passagem de 1 segundo

                //Verifica a condicao para salvamento de medicoes
                verificarSalvamentoCondicional();

                Thread.sleep(100); //Pausa de 100 milissegundos
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void verificarSalvamentoCondicional() {
        long m3Atuais = hidrometro.getMetrosCubicos();
        //Gatilho: A cada metro cubico (m³) inteiro completado.
        if (m3Atuais > ultimoM3Salvo) {
            ultimoM3Salvo = m3Atuais;
            salvarMedicao();
        }
    }

    private void salvarMedicao() {
        //Diretorio: As imagens devem ser salvas em uma pasta nomeada Medicoes_[MatriculaSUAP]
        File diretorio = new File("Medições_" + this.matricula);
        if (!diretorio.exists()) {
            diretorio.mkdir();
        }

        //Nome do Arquivo: Os arquivos devem ser nomeados ciclicamente de 01.jpeg a 99.jpeg
        String nomeArquivo = String.format("%02d.jpeg", contadorImagens);
        File arquivoSaida = new File(diretorio, nomeArquivo);

        try {
            BufferedImage screenshot = display.getScreenshot();
            //Formato: A imagem deve ser salva em formato JPEG.
            ImageIO.write(screenshot, "jpeg", arquivoSaida);
            System.out.println("Medição salva: " + arquivoSaida.getAbsolutePath());

            contadorImagens++;
            if (contadorImagens > 99) {
                contadorImagens = 1; //Reinicia o ciclo
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}