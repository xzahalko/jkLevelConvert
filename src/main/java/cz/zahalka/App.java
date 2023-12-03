package cz.zahalka;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.*;
import java.nio.file.Paths;

/**
 * Hello world!
 *
 */
public class App extends JFrame
{
    private static BufferedImage originalImage;
    private static BufferedImage targetImage;
    private static BufferedImage targetImageOrig;
    private static int lWidthX = 59;
    private static int lHeightY = 44;
    private static int screensPerY = 12;
    private static int screensPerX = 12;
    private static MapDetials mapDetails;

    private static BufferedImage imageToBufferedImage(Image image) {

        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return bufferedImage;

    }
    public static Image makeColorTransparent(BufferedImage im, final Color color) {
        ImageFilter filter = new RGBImageFilter() {

            // the color we are looking for... Alpha bits are set to opaque
            public int markerRGB = color.getRGB() | 0xFF000000;

            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    private static MapDetials getScreensDetails(int startX, int startY ) {
        int continuesColorX = 0;
        int continuesColorY = 0;
        int screens2Copy = 0;
        int screenNum = 0;
        int firstScreen = startX * ( screensPerX + 1 ) * startY * ( screensPerX + 1 );
        boolean lastSet = true;
        boolean fisrtSet = false;
        boolean prevScrFull = false;
        int firstSX = 0;
        int firstSY = 0;
        int firstNum = 0;

        MapDetials details = new MapDetials();

        Color colorTrans1 = new Color(91,91,91 );
        Color colorTrans2 = new Color(24,66,17 );

        for (int sx=startX; sx<=screensPerX; sx++) {
            int pocObrY;
            continuesColorX = 0;
            continuesColorY = 0;

            if ( sx == startX ) pocObrY = startY; else pocObrY = 0;

            for (int sy = pocObrY; sy <= screensPerY; sy++) {
                continuesColorX = 0;
                continuesColorY = 0;
                Screen screenI = new Screen(screenNum);
                screenI.setXPos(sx);screenI.setYPos(sy);
                details.getScreens().add(screenI);
                System.out.println("pocObr " + screenNum);

                for (int yy = 0; yy <= lHeightY; yy++) {
                    continuesColorX = 0;
                    for (int xx = 0; xx <= lWidthX; xx++) {
                        Color colorIM = new Color ( originalImage.getRGB(xx + sx * (lWidthX + 1), yy + sy * (lHeightY + 1)));

                        if ( ( colorIM.getRGB() == colorTrans2.getRGB() ) || ( colorIM.getRGB() == colorTrans1.getRGB() ) ) {
                            continuesColorX = continuesColorX + 1;
                        } else {
                            continuesColorX = 0;

                            if ( prevScrFull && lastSet && !fisrtSet) {
                                if (screenNum == 1) {
                                    details.getScreens().get(screenNum - 1).setFirst(true);
                                    if (sy == screensPerY) {
                                        firstSX = sx -1;
                                        firstSY = screensPerY;
                                    } else {
                                        firstSX = sx;
                                        firstSY = sy - 1;
                                    }
                                } else {
                                    details.getScreens().get(screenNum).setFirst(true);
                                    firstSX = sx;
                                    firstSY = sy;
                                }
                                firstNum = firstSX * (screensPerX + 1) + firstSY;
                                details.getScreens().get(firstNum).setFirstScreenNum(firstNum);
                                fisrtSet = true;
                                lastSet = false;
                            }
                        }

                        if ( (xx == 0 ) || ( xx == lWidthX ) ) {
                            if ( ( colorIM.getGreen() == 0 ) && ( colorIM.getBlue() == 255 ) ) {
                                Teleport teleport;
                                if ( xx == 0 ) {
                                    teleport = new Teleport(sy + ( sx * (screensPerY + 1 ) ), colorIM, "L" );
                                } else teleport = new Teleport(sy + ( sx * ( screensPerY + 1 ) ), colorIM, "R" );

                                if ( details.searchTeleport(teleport) == -1 ) details.getMapTeleports().add(teleport);
                                if ( details.getScreens().get(screenNum).searchScreenTeleport(teleport)  == -1 )
                                    details.getScreens().get(screenNum).getMapScreenTeleports().add(teleport);
                            }
                        }
                    }
                    if ( continuesColorX >= lWidthX ) {
                        continuesColorY = continuesColorY + 1;
                        if ( continuesColorY >= lHeightY) {
                            if (!lastSet && fisrtSet && prevScrFull ) {
                                details.getScreens().get(screenNum -1).setLast(true);
                                lastSet = true;
                                fisrtSet = false;
                            }
//                            break;
                        }
                    } else continuesColorY = 0;
                }
                if ( continuesColorY < lHeightY) {
                    screens2Copy++;
                    prevScrFull = true;
                } else {
                    System.out.println("screens2Copy " + screens2Copy);
                    details.getScreens().get(firstNum).setScreens2copy(screens2Copy);
                    firstNum = sx * (screensPerX + 1) + sy;
                    firstSX = sx;
                    firstSY = sy;
                    screens2Copy = 0;
                }
                details.getScreens().get(screenNum).setFirstScreenX(firstSX);
                details.getScreens().get(screenNum).setFirstScreenY(firstSY);
                details.getScreens().get(screenNum).setFirstScreenNum(firstNum);
                screenNum++;
            }
        }

        details.setScreens2Copy(screens2Copy);

        return details;
    }

    private static BufferedImage writeGrid(BufferedImage tImage ) {
        Color colorTrans1 = new Color(91, 91, 91);
        Color colorTrans2 = new Color(24, 66, 17);
        Integer screenNum = 0;

        for (int sx=0; sx<=screensPerX; sx++) {
            for (int sy = 0; sy <= screensPerY; sy++) {
                for (int yy = 0; yy <= lHeightY; yy++) {
                    for (int xx = 0; xx <= lWidthX; xx++) {
                            if ( ( yy == 0 ) || ( yy == lHeightY ) || ( xx == 0 ) || ( xx == lWidthX ) )
                                tImage.setRGB(xx + sx * (lWidthX + 1), yy + sy * (lHeightY + 1), colorTrans2.getRGB());
                            else tImage.setRGB(xx + sx * (lWidthX + 1), yy + sy * (lHeightY + 1), colorTrans1.getRGB());
                    }
                }
                Graphics g = tImage.getGraphics();
                g.setFont(new Font("Nova Square", Font.PLAIN, 7));
                g.setColor(colorTrans2);
                screenNum++;
                if ( screenNum < 100 )
                    g.drawString(screenNum.toString(), lWidthX - 9 + sx * (lWidthX + 1), lHeightY - 1 + sy * (lHeightY + 1));
                else
                    g.drawString(screenNum.toString(), lWidthX - 13 + sx * (lWidthX + 1), lHeightY - 1 + sy * (lHeightY + 1));
            }
        }
        return tImage;
    }

    public static void main( String[] args ) {
        final App frame = new App();

        frame.setLocation(500,500);
        frame.setSize(500,500);

        JButton button = new JButton("Choose file");
        JPanel panel = new JPanel();
        panel.add(button);
        frame.getContentPane().add(panel);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // delegate to event handler method
                this.buttonActionPerformed(evt);
            }

            private void buttonActionPerformed(ActionEvent evt) {
                JFileChooser fileChooser = new JFileChooser();

                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();

                    System.out.println(selectedFile.getAbsolutePath());
                    System.out.println(Paths.get(selectedFile.getAbsolutePath()).getParent().toString());
                    try {
                        originalImage = ImageIO.read(new File(selectedFile.getAbsolutePath()));
                    } catch (IOException e) {
                        System.out.printf("chyba cteni");
                    }

                    mapDetails = getScreensDetails(0,0 );

                    int maxScr2copy = 0;
                    int width = 0;
                    int height = 0;
                    int scr2copy = 0;

                    for (int i = 0; i < mapDetails.getScreens().size(); i++) {
                        Screen scr = mapDetails.getScreens().get(i);
                        if (scr.first) {
                            if (scr.getScreens2copy() > maxScr2copy) maxScr2copy = scr.getScreens2copy();
                            width = width + ( lWidthX + 1 )  * 2;
                            height = maxScr2copy * (lHeightY + 1);
                        }
                        if (i == 0) scr2copy = maxScr2copy;
                    }

                    targetImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    Graphics2D    graphics = targetImage.createGraphics();
                    graphics.setPaint ( new Color(91, 91, 91) );
                    graphics.fillRect ( 0, 0, width, height );

                    int exportedScr = 0;
                    for (int i = 0; i < mapDetails.getScreens().size(); i++) {
                        Screen scr = mapDetails.getScreens().get(i);
                        if (scr.first) {
                            scr2copy = scr.getScreens2copy();
                            int screensCopied = 0;

                            for (int sx = scr.getFirstScreenX(); sx <= screensPerX; sx++) {
                                for (int sy = ( sx == scr.getFirstScreenX() ) ? scr.getFirstScreenY() : 0 ; sy <= screensPerY; sy++) {
                                    int obrY = ((maxScr2copy - 1 - ( sy - scr.getFirstScreenY() )  - ( ( sx - scr.getFirstScreenX() ) * (screensPerY + 1))) * (lHeightY + 1));
                                    int obrX = exportedScr * (lWidthX + 1) * 2;
//                            System.out.println("screenY start " + obrY);

                                    if (screensCopied >= scr2copy) break;

                                    for (int yy = 0; yy <= lHeightY; yy++) {
                                        for (int xx = 0; xx <= lWidthX; xx++) {
                                            Color originalColor = new Color(originalImage.getRGB(xx + sx * (lWidthX + 1), yy + sy * (lHeightY + 1)));
                                            int r1 = originalColor.getRed();
                                            int g1 = originalColor.getGreen();
                                            int b1 = originalColor.getBlue();
                                            int a1 = originalColor.getAlpha();

                                            Color bnw = new Color(r1, g1, b1, a1);
                                            targetImage.setRGB(
                                                    obrX + xx,
                                                    obrY + yy,
                                                    bnw.getRGB()
                                            );
                                        }
                                    }
                                    screensCopied++;
                                }
                            }
                            exportedScr++;
                            System.out.println("screen copied " + scr2copy);
                        }
                    }
/*
                    Image im  = makeColorTransparent( targetImage, new Color(91, 91, 91) );
                    targetImage = imageToBufferedImage( im );
*/
                    try {
                        File outputfile = new File(Paths.get(selectedFile.getAbsolutePath()).getParent().toString() + "\\saved.png");
                        ImageIO.write(targetImage, "png", outputfile);
                    } catch (IOException e) {
                        System.out.printf("chyba zapisu");
                    }

                    try {
                        FileOutputStream fout = new FileOutputStream(Paths.get(selectedFile.getAbsolutePath()).getParent().toString() + "\\saved.dat");
                        ObjectOutputStream oos = new ObjectOutputStream(fout);
                        oos.writeObject(mapDetails);
                        oos.close();
                    } catch (IOException e) {
                        System.out.printf("chyba zapisu");
                    }

/*                    try {
                        FileInputStream fin = new FileInputStream(Paths.get(selectedFile.getAbsolutePath()).getParent().toString() + "\\saved.dat");
                        ObjectInputStream ois = new ObjectInputStream(fin);
                        mapDetails = (MapDetials) ois.readObject();
                        ois.close();
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.printf("chyba zapisu");
                    }
*/
                    int widthDef = 780;
                    int heightDef = 585;

//vyplneni sedivou barvou cely obrazek
                    targetImageOrig = new BufferedImage(widthDef,heightDef,  BufferedImage.TYPE_INT_RGB);
                    targetImageOrig = writeGrid(targetImageOrig);

                    exportedScr = 0;
                    for (int i = 0; i < mapDetails.getScreens().size(); i++) {
                        Screen scr = mapDetails.getScreens().get(i);
                        if (scr.first) {
                            scr2copy = scr.getScreens2copy();
                            int screensCopied = 0;

                            for (int sx = scr.getFirstScreenX(); sx <= screensPerX; sx++) {
                                for (int sy = ( sx == scr.getFirstScreenX() ) ? scr.getFirstScreenY() : 0 ; sy <= screensPerY; sy++) {
                                    int obrY = ((maxScr2copy - 1 - ( sy - scr.getFirstScreenY() )  - ( ( sx - scr.getFirstScreenX() ) * (screensPerY + 1))) * (lHeightY + 1));
                                    int obrX = exportedScr * (lWidthX + 1) * 2;

                                    if (screensCopied >= scr2copy) break;

                                    if (obrY < 0) {
                                        System.out.println("screenY start " + obrY);
                                    }
                                    for (int yy = 0; yy <= lHeightY; yy++) {
                                        for (int xx = 0; xx <= lWidthX; xx++) {
                                            Color originalColor = new Color(targetImage.getRGB(obrX + xx,obrY + yy));
                                            int r1 = originalColor.getRed();
                                            int g1 = originalColor.getGreen();
                                            int b1 = originalColor.getBlue();
                                            int a1 = originalColor.getAlpha();
                                            Color bnw = new Color(r1, g1, b1, a1);

                                            targetImageOrig.setRGB(
                                                    xx + sx * (lWidthX + 1),
                                                    yy + sy * (lHeightY + 1),
                                                    bnw.getRGB()
                                            );
                                        }
                                    }
                                    screensCopied++;
                                }
                            }
                            exportedScr++;
                            System.out.println("screen copied " + screensCopied);
                        }
                    }

                    //nastav sedivou barvu transparentni
//                    Image im  = makeColorTransparent( targetImageOrig, new Color(91, 91, 91) );
//                    targetImageOrig = imageToBufferedImage( im );

                    try {
                        // retrieve image
                        File outputfile = new File(Paths.get(selectedFile.getAbsolutePath()).getParent().toString() + "\\savedOrig.png");
                        ImageIO.write(targetImageOrig, "png", outputfile);
                    } catch (IOException e) {
                        System.out.printf("chyba zapisu");
                    }
                }
            }
        });

        frame.setVisible(true);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
