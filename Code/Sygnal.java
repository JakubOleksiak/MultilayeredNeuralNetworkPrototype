public class Sygnal {
    public String litera;
    public double y;
    public Double blad;
    public Perceptron from;
    public Perceptron to;

    public Sygnal(String litera, double y, Perceptron from, Perceptron to) {
        this.litera = litera;
        this.y = y;
        this.from = from;
        this.to = to;
    }
}
