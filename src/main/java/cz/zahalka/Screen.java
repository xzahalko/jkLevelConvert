package cz.zahalka;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

@Data
public class Screen implements Serializable {
    boolean first = false;
    int firstScreenX = 0;
    int firstScreenY = 0;
    int firstScreenNum = 0;
    boolean last= false;
    int screens2copy = 0;
    int xPos = 0;
    int yPos = 0;
    int screenNum;
    ArrayList<Teleport> mapScreenTeleports = new ArrayList<>();

    public Screen (int scrNum ) {
        screenNum = scrNum;
    }

    public int searchScreenTeleport ( Teleport tel ) {
//        int i = 0;
        int foundPos = -1;
//        for(Teleport teleport : mapTeleports) {
//            i++;
        for (int i = 0; i < mapScreenTeleports.size(); i++) {
            Teleport teleport = mapScreenTeleports.get(i);
            if ( ( teleport.getTeleportScreen() == tel.getTeleportScreen() ) &&
                    ( teleport.getSide().equals(tel.getSide() ) ) &&
                    ( teleport.getScreen() == tel.getScreen() ) &&
                    ( teleport.getBarva().getRGB() == tel.getBarva().getRGB() )
            ) {
                foundPos = i;
                break;
            }
        }
        return foundPos;
    }
}
