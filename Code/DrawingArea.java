import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

public class DrawingArea extends JComponent {

    //Obraz, na ktorym malujemy
    private Image image;

    //To, czym rysujemy
    private Graphics2D graphics2D;

    //Wspolrzedne myszki
    private int curX, curY, oldX, oldY;

    //Ustawianie listenerow
    public DrawingArea() {
        this.setDoubleBuffered(false);
        this.setPreferredSize(new Dimension(800, 700));

        this.addMouseListener(new MouseAdapter() {
            @Override
            //Kiedy nacisniety przycisk, ustawiane sa pierwsze wspolrzedne
            public void mousePressed(MouseEvent e) {
                oldX = e.getX();
                oldY = e.getY();
            }
        });


        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            //Przy przeciaganiu myszka (z wcisnietym klawiszem) rysowana jest linia
            public void mouseDragged(MouseEvent e) {
                curX = e.getX();
                curY = e.getY();

                if (graphics2D != null) {
                    graphics2D.drawLine(oldX, oldY, curX, curY);

                    repaint();

                    oldX = curX;
                    oldY = curY;
                }
            }


        });
    }

    @Override
    //Odswiezanie obrazu (wywolywane metoda repaint())
    protected void paintComponent(Graphics g) {
        if (image == null) {
            image = createImage(this.getSize().width, this.getSize().height);
            graphics2D = (Graphics2D) image.getGraphics();
            graphics2D.setStroke(new BasicStroke(15));

            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            clear();
        }

        g.drawImage(image, 0, 0, null);
    }

    //Czysci obraz
    public void clear() {
        graphics2D.setPaint(Color.WHITE);
        graphics2D.fillRect(0, 0, this.getSize().width, this.getSize().height);
        graphics2D.setPaint(Color.BLACK);
        repaint();
    }

    //Przycinanie obrazu i skalowanie go
    public static BufferedImage crop(BufferedImage imageToCrop, Dimension dim) {

        ////////////////////POCZATEK PRZYCINANIA

        BufferedImage img = imageToCrop;

        int height = img.getHeight();
        int width = img.getWidth();
        int rowGora = 0, rowDol = 0, colLewo = 0, colPrawo = 0;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int tmp = img.getRGB(col, row);
                if (tmp != -1) {
                    rowDol = row;
                    break;
                }
            }
        }

        for (int row = height - 1; row >= 0; row--) {
            for (int col = 0; col < width; col++) {
                int tmp = img.getRGB(col, row);
                if (tmp != -1) {
                    rowGora = row;
                    break;
                }
            }
        }

        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int tmp = img.getRGB(col, row);
                if (tmp != -1) {
                    colPrawo = col;
                    break;
                }
            }
        }

        for (int col = width - 1; col >= 0; col--) {
            for (int row = 0; row < height; row++) {
                int tmp = img.getRGB(col, row);
                if (tmp != -1) {
                    colLewo = col;
                    break;
                }
            }
        }


        int[][] res = new int[rowDol - rowGora][colPrawo - colLewo];
        BufferedImage bufferedImage = new BufferedImage(res[0].length, res.length, BufferedImage.TYPE_INT_ARGB);

        for (int row = 0; row < res.length; row++) {
            for (int col = 0; col < res[row].length; col++) {
                int tmp = img.getRGB(col + colLewo, row + rowGora);
                bufferedImage.setRGB(col, row, tmp);
            }
        }

        //////////////////////////KONIEC PRZYCINANIA


        ////////////////////SKALOWANIE

        Image imgTmp = bufferedImage.getScaledInstance(dim.getSize().width, dim.getSize().height, Image.SCALE_AREA_AVERAGING);
        BufferedImage resBuf = new BufferedImage(imgTmp.getWidth(null), imgTmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2tmp = resBuf.createGraphics();
        //g2tmp.scale((double)this.getSize().width/res[0].length, (double)this.getSize().height/res.length);

        g2tmp.drawImage(imgTmp, 0, 0, null);
        g2tmp.dispose();


        //resBuf = resBuf.getSubimage(0, 0, 300, 300);


        return resBuf;
    }

    //Wyciaganie pikseli z przycietego i przeskalowanego obrazu
    public int[][] getPixels() {
        BufferedImage bufImage = this.crop(this.getBufferedImage(), new Dimension(100, 100));
        int height = bufImage.getHeight();
        int width = bufImage.getWidth();
        int[][] res = new int[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int tmp = bufImage.getRGB(col, row);
                if (tmp == -1) tmp = 0;
                else tmp = 1;
                res[row][col] = tmp;
            }
        }

        return res;

    }


    //Wyciaganie BufferedImage do obrobki w crop i getPixels
    private BufferedImage getBufferedImage() {

        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2tmp = bufferedImage.createGraphics();

        g2tmp.drawImage(image, 0, 0, null);
        g2tmp.dispose();

        return bufferedImage;
    }
}
