import java.util.ArrayList;
import java.util.HashMap;

public class Perceptron {
    public double stalaUczenia;
    public double[] wagi;
    public HashMap<Perceptron, Polaczenie> wagiPoloczen;
    public double prog;
    public Perceptron[] from;
    public Perceptron[] to;
    public ArrayList<Sygnal> sygnaly;
    public double[] daneTmp;
    public double yTmp;

    public Perceptron(double stalaUczenia, int wymiar, Perceptron[] from, Perceptron[] to) {
        this.stalaUczenia = stalaUczenia;
        this.wagi = new double[wymiar];
        this.sygnaly = new ArrayList<>();
        this.wagiPoloczen = new HashMap<>();
        this.from = from;
        this.to = to;

        this.prog = 0.5;

        for (int i = 0; i < wagi.length; i++) {
            wagi[i] = Math.random() * 2 - 1;
        }

        int tmp = 0;

        if (from != null) {
            for (int i = 0; i < from.length; i++) {
                tmp++;
                if (tmp == wagi.length) tmp = 0;
                Polaczenie pol = new Polaczenie();
                pol.waga = wagi[tmp];
                wagiPoloczen.put(from[i], pol);
            }
        }

    }

    public void learn() {
        if (sygnaly.get(0).blad == null) {
            double[] dane = new double[sygnaly.size()];
            double[] wagi = new double[sygnaly.size()];
            for (int i = 0; i < sygnaly.size(); i++) {
                dane[i] = sygnaly.get(i).y;
                wagi[i] = wagiPoloczen.get(sygnaly.get(i).from).waga;
            }

            for (int i = 0; i < sygnaly.size(); i++) {
                wagiPoloczen.get(sygnaly.get(i).from).ostatnioOtrzymaneDane = dane[i];
            }
            double net = 0;

            for (int i = 0; i < wagi.length; i++) {
                net += wagi[i] * dane[i];
            }

            net = net - this.prog;

            double y = 1 / (1 + Math.exp(-net));
            this.yTmp = y;

            for (int i = 0; i < this.to.length; i++) {
                Sygnal sygnal = new Sygnal(sygnaly.get(0).litera, y, this, to[i]);
                to[i].receiveSignal(sygnal);
            }
        } else {
            double sumaBledow = 0;
            for (int i = 0; i < sygnaly.size(); i++) {
                sumaBledow += sygnaly.get(i).blad;
            }
            double bladTu = -1 * this.yTmp * (this.yTmp - 1) * sumaBledow;
            this.prog = this.prog + this.stalaUczenia * bladTu;
            if (from != null) {
                for (Perceptron perceptron : wagiPoloczen.keySet()) {
                    double wagaPrzed = wagiPoloczen.get(perceptron).waga;
                    double daneOtrzymane = wagiPoloczen.get(perceptron).ostatnioOtrzymaneDane;
                    double wagaPo = wagaPrzed + stalaUczenia * daneOtrzymane * bladTu;
                    wagiPoloczen.get(perceptron).waga = wagaPo;
                    double bladDoWyslania = wagaPrzed * bladTu;
                    Sygnal sygnalBledu = new Sygnal(sygnaly.get(0).litera, 0, this, perceptron);
                    sygnalBledu.blad = bladDoWyslania;
                    perceptron.receiveSignal(sygnalBledu);
                }
            } else {
                for (int i = 0; i < wagi.length; i++) {
                    wagi[i] = wagi[i] + stalaUczenia * daneTmp[i] * bladTu;
                }
            }
        }
        this.sygnaly.clear();
    }

    public void receiveSignal(Sygnal sygnal) {
        sygnaly.add(sygnal);
    }

    public void activate(double[] dane, String litera) {
        this.daneTmp = dane;
        double net = 0;

        for (int i = 0; i < this.wagi.length; i++) {
            net += this.wagi[i] * dane[i];
        }

        net = net - this.prog;

        double y = 1 / (1 + Math.exp(-net));

        for (int i = 0; i < this.to.length; i++) {
            Sygnal sygnal = new Sygnal(litera, y, this, to[i]);
            to[i].receiveSignal(sygnal);
        }


    }

//    public static double[] znormalizujWektor(double[] wektor) {
//        double[] wektorPoNormalizacji = new double[wektor.length];
//
//        double przezCoPodzielic = 0;
//        for (int i = 0; i < wektorPoNormalizacji.length; i++) {
//            wektorPoNormalizacji[i] = wektor[i];
//            przezCoPodzielic += wektor[i] * wektor[i];
//        }
//        przezCoPodzielic = Math.sqrt(przezCoPodzielic);
//        for (int i = 0; i < wektorPoNormalizacji.length; i++) {
//            wektorPoNormalizacji[i] = wektorPoNormalizacji[i] / przezCoPodzielic;
//        }
//        return wektorPoNormalizacji;
//    }
}
