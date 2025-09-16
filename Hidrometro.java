public class Hidrometro {
    private String numeroSerie;
    private int litrosConsumidos;

    public Hidrometro(String numeroSerie) {
        this.numeroSerie = numeroSerie;
        this.litrosConsumidos = 0;
    }

    public void consumirAgua(int litros) {
        this.litrosConsumidos += litros;
    }

    public int getLitrosConsumidos() {
        return litrosConsumidos;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public int getMetrosCubicos() {
        return litrosConsumidos / 1000;
    }

    public int getLitrosRestantes() {
        return litrosConsumidos % 1000;
    }

    public int getDecimosLitro() {
        return litrosConsumidos % 10;
    }
    
}
