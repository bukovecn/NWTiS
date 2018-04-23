package org.foi.nwtis.nikbukove.zadaca_1;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa koja služi za rad s podacima o IOT uređajima. Ažurira i dodaje podatke
 * o uređajima.
 *
 * @author Nikolina Bukovec
 */
public class IOT {

    private List<IOT_device> listaUredaja = new ArrayList<>();

    public String azurirajUredaj(IOT_device iot) {
        try {
            for (IOT_device iotDevice : listaUredaja) {
                if (iotDevice.getID() == iot.getID()) {
                    iotDevice = iot;
                    return "OK; 21";
                }
            }
            listaUredaja.add(iot);
            return "OK; 20";
        } catch (Exception e) {
            return "ERROR 21; Došlo je do problema tijekom ažuriranja podataka!";
        }
    }
}
