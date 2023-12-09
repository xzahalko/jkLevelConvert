package cz.zahalka;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
@Data
public class MapDetials implements Serializable {
    ArrayList<Screen> screens = new ArrayList<>();
    ArrayList<Teleport> mapTeleports = new ArrayList<>();

    int screens2Copy = 0;
    int maxScreens2Copy = 0;

    public int searchTeleport ( Teleport tel ) {
//        int i = 0;
        int foundPos = -1;
//        for(Teleport teleport : mapTeleports) {
//            i++;
            for (int i = 0; i < mapTeleports.size(); i++) {
                Teleport teleport = mapTeleports.get(i);
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
