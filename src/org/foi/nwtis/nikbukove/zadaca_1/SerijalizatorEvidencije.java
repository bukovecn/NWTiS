package org.foi.nwtis.nikbukove.zadaca_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.nikbukove.konfiguracije.Konfiguracija;

/**
 * Klasa koja služi za provođenje serijalizacije rada ovisno o zadanom intervalu (iz konfoguracije).
 *
 * @author Nikolina Bukovec
 */
class SerijalizatorEvidencije extends Thread {

    private String nazivDretve;
    private Konfiguracija konf;
    private boolean radiDok = true;
    public Evidencija evidencija;

    SerijalizatorEvidencije(String nazivDretve, Konfiguracija konf, Evidencija evidencija) {
        super(nazivDretve);
        this.nazivDretve = nazivDretve;
        this.konf = konf;
        this.evidencija = evidencija;
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    @Override
    public void run() {
        String nazivDatEvidencije = konf.dajPostavku("datoteka.evidencije.rada");
        int intervalSerijalizacije = Integer.parseInt(konf.dajPostavku("interval.za.serijalizaciju"));
        while (radiDok) {
            long pocetak = System.currentTimeMillis();
            System.out.println("Dretva: " + nazivDretve + " Pocetak: " + pocetak);
            evidencija.obaviSerijalizaciju(nazivDatEvidencije);
            evidencija.setBrojObavljenihSeralizacija(true);
            long kraj = System.currentTimeMillis();
            long odradeno = kraj - pocetak;
            long cekaj = (intervalSerijalizacije * 1000) - odradeno;
            try {
                Thread.sleep(cekaj);
            } catch (InterruptedException ex) {
                Logger.getLogger(SerijalizatorEvidencije.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }
}
