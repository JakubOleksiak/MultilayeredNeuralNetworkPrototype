import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class s18728 {

    public static void main(String[] args) {
        double stalaUczenia = Double.parseDouble(args[0]);
        File trainSetDir = new File(args[1]);
        File testSetDir = new File(args[2]);
        List<Litera> litery = new ArrayList<>();

        //Wczytywanie obrazow
        FileVisitor<Path> fileVisitor = new FileVisitor<Path>() {
            boolean wypisz = true;
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                wypisz = true;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                BufferedImage img = ImageIO.read(file.toFile());
                Image imgTmp = img.getScaledInstance(100, 100, BufferedImage.SCALE_AREA_AVERAGING);
                BufferedImage scaledImage = new BufferedImage(imgTmp.getWidth(null), imgTmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2tmp = scaledImage.createGraphics();

                g2tmp.drawImage(imgTmp, 0, 0, null);
                g2tmp.dispose();

                BufferedImage croppedImage = DrawingArea.crop(scaledImage, new Dimension(100, 100));

                int height = croppedImage.getHeight();
                int width = croppedImage.getWidth();
                int[][] pixels = new int[height][width];
                double[] data = new double[25];

                for (int row = 0; row < height; row++) {
                    for (int col = 0; col < width; col++) {
                        int tmp = croppedImage.getRGB(col, row);
                        if (tmp == -1) tmp = 0;
                        else tmp = 1;
                        pixels[row][col] = tmp;
                    }
                }

                int indeks = 0;

                for (int i = 0; i < pixels.length; i+=20) {
                    for (int j = 0; j < pixels[i].length; j+=20) {
                        double stosunek = 0;
                        for (int k = 0+i; k < 20+i; k++) {
                            for (int l = 0+j; l < 20+j; l++) {
                                stosunek+=pixels[k][l];
                            }
                        }
                        data[indeks++] = stosunek/400;
                    }
                }
                Litera litera = new Litera();
                litera.litera = file.getParent().getFileName().toString();
                litera.dane = data;
                litery.add(litera);
                if (wypisz){
                    System.out.println(litera.litera);
                    wypisz = false;
                }


                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        };


        try {
            Files.walkFileTree(trainSetDir.toPath(), fileVisitor);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ////////////PERCEPTRONY

        Perceptron[] perceptronyWarstwaPierwsza = new Perceptron[100];
        Perceptron[] perceptronyWarstwaDruga = new Perceptron[60];
        PerceptronOstatniejWarstwy[] perceptronyWarstwaOstatnia = new PerceptronOstatniejWarstwy[26];

        for (int i = 0; i < perceptronyWarstwaPierwsza.length; i++) {
            perceptronyWarstwaPierwsza[i] = new Perceptron(stalaUczenia, litery.get(0).dane.length, null, perceptronyWarstwaDruga);
        }

        for (int i = 0; i < perceptronyWarstwaDruga.length; i++) {
            perceptronyWarstwaDruga[i] = new Perceptron(stalaUczenia, perceptronyWarstwaPierwsza.length, perceptronyWarstwaPierwsza, perceptronyWarstwaOstatnia);
        }

        for (int i = 0; i < perceptronyWarstwaOstatnia.length; i++) {
            perceptronyWarstwaOstatnia[i] = new PerceptronOstatniejWarstwy(stalaUczenia, ((char)(65+i))+"", perceptronyWarstwaDruga.length, perceptronyWarstwaDruga);
        }

        //Uczenie perceptronow

        Collections.shuffle(litery);
        int ileDobrze = 0;
        int ileCalkowicie = 0;
        double dokladnosc = 0;
        int w = 0;
        while (dokladnosc<0.94){
            ileCalkowicie = 0;
            ileDobrze = 0;
            System.out.print("Iteracja " + ((w++)+1));
            for (Litera litera:litery) {
                //System.out.println("Aktywacja perceptronow warstwy pierwszej");
                for (int j = 0; j < perceptronyWarstwaPierwsza.length; j++) {
                    perceptronyWarstwaPierwsza[j].activate(litera.dane, litera.litera);
                }

                //System.out.println("Aktywacja perceptronow warstwy drugiej");
                for (int j = 0; j < perceptronyWarstwaDruga.length; j++) {
                    perceptronyWarstwaDruga[j].learn();
                }
                double max = -2;
                String literaPerceptron = "";
                //System.out.println("Uczenie perceptronow warstwy ostatniej");
                for (int j = 0; j < perceptronyWarstwaOstatnia.length; j++) {
                    perceptronyWarstwaOstatnia[j].learn();
                    if (perceptronyWarstwaOstatnia[j].yTmp>max){
                        max = perceptronyWarstwaOstatnia[j].yTmp;
                        literaPerceptron = perceptronyWarstwaOstatnia[j].litera;
                    }
                }
                if (literaPerceptron.equals(litera.litera))ileDobrze++;
                ileCalkowicie++;


                //System.out.println("Uczenie perceptronow warstwy drugiej");
                for (int j = 0; j <perceptronyWarstwaDruga.length ; j++) {
                    perceptronyWarstwaDruga[j].learn();
                }
                //System.out.println("Uczenie perceptronow warstwy pierwszej");
                for (int j = 0; j <perceptronyWarstwaPierwsza.length ; j++) {
                    perceptronyWarstwaPierwsza[j].learn();
                }
            }
            dokladnosc = (double)ileDobrze/ileCalkowicie;
            System.out.println(" Dokladnosc: " + dokladnosc);
        }

        /////////////Test

        litery.clear();
        FileVisitor<Path> fileVisitorTest = new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                BufferedImage img = ImageIO.read(file.toFile());
                Image imgTmp = img.getScaledInstance(100, 100, BufferedImage.SCALE_AREA_AVERAGING);
                BufferedImage scaledImage = new BufferedImage(imgTmp.getWidth(null), imgTmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2tmp = scaledImage.createGraphics();

                g2tmp.drawImage(imgTmp, 0, 0, null);
                g2tmp.dispose();

                BufferedImage croppedImage = DrawingArea.crop(scaledImage, new Dimension(100, 100));

                int height = croppedImage.getHeight();
                int width = croppedImage.getWidth();
                int[][] pixels = new int[height][width];
                double[] data = new double[25];

                for (int row = 0; row < height; row++) {
                    for (int col = 0; col < width; col++) {
                        int tmp = croppedImage.getRGB(col, row);
                        if (tmp == -1) tmp = 0;
                        else tmp = 1;
                        pixels[row][col] = tmp;
                    }
                }

                int indeks = 0;

                for (int i = 0; i < pixels.length; i+=20) {
                    for (int j = 0; j < pixels[i].length; j+=20) {
                        double stosunek = 0;
                        for (int k = 0+i; k < 20+i; k++) {
                            for (int l = 0+j; l < 20+j; l++) {
                                stosunek+=pixels[k][l];
                            }
                        }
                        data[indeks++] = stosunek/400;
                    }
                }
                Litera litera = new Litera();
                litera.litera = file.getFileName().toString().substring(0, 1);
                litera.dane = data;
                litery.add(litera);


                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(testSetDir.toPath(), fileVisitorTest);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ileDobrze = 0;
        ileCalkowicie = 0;
        for (Litera litera:litery) {
            for (int i = 0; i < perceptronyWarstwaPierwsza.length; i++) {
                perceptronyWarstwaPierwsza[i].activate(litera.dane, "");
            }

            for (int i = 0; i < perceptronyWarstwaDruga.length; i++) {
                perceptronyWarstwaDruga[i].learn();
            }

            double max = -2;
            String literaTmp = "";
            for (int i = 0; i < perceptronyWarstwaOstatnia.length; i++) {
                double tmpY =perceptronyWarstwaOstatnia[i].getY();
                if (tmpY>max){
                    max = tmpY;
                    literaTmp = perceptronyWarstwaOstatnia[i].litera;
                }
            }
            if (litera.litera.equals(literaTmp))ileDobrze++;
            ileCalkowicie++;
        }

        System.out.println("Dokladnosc przy testach: " + (double)ileDobrze/ileCalkowicie);


        ///////////////GUI

        JFrame jFrame = new JFrame("Test");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(800, 800);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        jFrame.getContentPane().add(mainPanel);

        final DrawingArea drawingArea = new DrawingArea();

        mainPanel.add(drawingArea, BorderLayout.CENTER);

        JButton sendPixels = new JButton("OK");
        sendPixels.setPreferredSize(new Dimension(800, 50));
        sendPixels.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int[][] pixels = drawingArea.getPixels();

                    int indeks = 0;
                    double[] data = new double[25];
                    for (int i = 0; i < pixels.length; i+=20) {
                        for (int j = 0; j < pixels[i].length; j+=20) {
                            double stosunek = 0;
                            for (int k = 0+i; k < 20+i; k++) {
                                for (int l = 0+j; l < 20+j; l++) {
                                    stosunek+=pixels[k][l];
                                }
                            }
                            data[indeks++] = stosunek/400;
                        }
                    }
                    for (int i = 0; i < perceptronyWarstwaPierwsza.length; i++) {
                        perceptronyWarstwaPierwsza[i].activate(data, "");
                    }

                    for (int i = 0; i < perceptronyWarstwaDruga.length; i++) {
                        perceptronyWarstwaDruga[i].learn();
                    }

                    double max = -2;
                    String litera = "";
                    for (int i = 0; i < perceptronyWarstwaOstatnia.length; i++) {
                        double tmpY =perceptronyWarstwaOstatnia[i].getY();
                        System.out.print(tmpY + " ");
                        System.out.println(perceptronyWarstwaOstatnia[i].litera);
                        if (tmpY>max){
                            max = tmpY;
                            litera = perceptronyWarstwaOstatnia[i].litera;
                        }
                    }
                    System.out.println(litera);
                }catch (Exception ex){
                    System.out.println(ex);
                }
            }
        });

        JButton clearButton = new JButton("Clear");
        clearButton.setPreferredSize(new Dimension(800, 50));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawingArea.clear();
            }
        });

        mainPanel.add(sendPixels, BorderLayout.SOUTH);
        mainPanel.add(clearButton, BorderLayout.NORTH);

        jFrame.setVisible(true);
    }


}
