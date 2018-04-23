package org.foi.nwtis.nikbukove.zadaca_1;

/**
 * Klasa koja služi za evidenciju IOT uređaja, omogućuje dohvaćanje(get) i
 * postavljanje(set) podataka o IOT uređaju.
 *
 * @author Nikolina Bukovec
 */
public class IOT_device {

    private int ID;
    private int zracenje;
    private int temperaturaZraka;
    private int temperaturaTla;

    public IOT_device() {
    }

    public IOT_device(int ID, int zracenje, int temperaturaZraka, int temperaturaTla) {
        this.ID = ID;
        this.zracenje = zracenje;
        this.temperaturaZraka = temperaturaZraka;
        this.temperaturaTla = temperaturaTla;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getZracenje() {
        return zracenje;
    }

    public void setZracenje(int zracenje) {
        this.zracenje = zracenje;
    }

    public int getTemperaturaZraka() {
        return temperaturaZraka;
    }

    public void setTemperaturaZraka(int temperaturaZraka) {
        this.temperaturaZraka = temperaturaZraka;
    }

    public int getTemperaturaTla() {
        return temperaturaTla;
    }

    public void setTemperaturaTla(int temperaturaTla) {
        this.temperaturaTla = temperaturaTla;
    }
}
