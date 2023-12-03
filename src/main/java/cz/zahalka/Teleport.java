package cz.zahalka;

import lombok.Data;

import java.awt.*;
import java.io.Serializable;

@Data
public class Teleport implements Serializable {
    int screen = 0;
    Color barva;
    String side = "";
    int teleportScreen = 0;

    public Teleport (int obr, Color b, String s ) {
        screen = obr;
        barva = b;
        side = s;
        teleportScreen = b.getRed();
    }
}
