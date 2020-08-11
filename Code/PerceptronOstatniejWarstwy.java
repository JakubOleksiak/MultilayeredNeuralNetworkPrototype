public class PerceptronOstatniejWarstwy extends Perceptron {
    public String litera;
    public double yTmp;

    public PerceptronOstatniejWarstwy(double stalaUczenia, String litera, int wymiar, Perceptron[] from) {
        super(stalaUczenia, wymiar, from, null);
        this.litera = litera;
    }

    public double getY(){
        double[] dane = new double[sygnaly.size()];
        double[] wagi = new double[sygnaly.size()];
        for (int i = 0; i < sygnaly.size(); i++) {
            dane[i] = sygnaly.get(i).y;
            wagi[i] = wagiPoloczen.get(sygnaly.get(i).from).waga;
        }

        double net = 0;

        for (int i = 0; i < this.wagi.length; i++) {
            net += wagi[i] * dane[i];
        }

        net = net - this.prog;

        double y = 1 / (1 + Math.exp(-net));
        sygnaly.clear();
        return y;
    }

    @Override
    public void learn() {
        double[] dane = new double[sygnaly.size()];
        double[] wagi = new double[sygnaly.size()];
        for (int i = 0; i < sygnaly.size(); i++) {
            dane[i] = sygnaly.get(i).y;
            wagi[i] = wagiPoloczen.get(sygnaly.get(i).from).waga;
        }

        double net = 0;

        for (int i = 0; i < this.wagi.length; i++) {
            net += wagi[i] * dane[i];
        }

        net = net - this.prog;

        double y = 1 / (1 + Math.exp(-net));
        yTmp = y;
        double d;

        if (sygnaly.get(0).litera.equals(litera)) {
            d = 1.0;
        } else {
            d = 0.0;
        }

        double bladTu = -1 * y * (y - 1) * (d - y);
        this.prog = this.prog + this.stalaUczenia * bladTu;
        for (int i = 0; i < sygnaly.size(); i++) {
            double staraWaga = wagiPoloczen.get(sygnaly.get(i).from).waga;
            double nowaWaga = staraWaga + stalaUczenia * sygnaly.get(i).y * bladTu;
            wagiPoloczen.get(sygnaly.get(i).from).waga = nowaWaga;
            double bladDoWyslania = staraWaga * bladTu;
            Sygnal sygnalBledu = new Sygnal(sygnaly.get(i).litera, 0, this, sygnaly.get(i).from);
            sygnalBledu.blad = bladDoWyslania;
            sygnaly.get(i).from.receiveSignal(sygnalBledu);
        }
        this.sygnaly.clear();
    }
}
