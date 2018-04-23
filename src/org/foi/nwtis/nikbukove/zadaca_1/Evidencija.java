package org.foi.nwtis.nikbukove.zadaca_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa obavlja evidenciju rada, omogućuje spremanje i dohvaćanje broja svih
 * vrsta zahtjeva te obavlja serijalizaciju podataka u datoteku.
 *
 * @author Nikolina Bukovec
 */
public class Evidencija implements Serializable {

    private long ukupanBrojZahtjeva = 0;
    private long brojNeispravnihZahjteva = 0;
    private long brojNedozvoljenihZahtjeva = 0;
    private long brojUspjesnihZahtjeva = 0;
    private long brojPrekinutihZatjeva = 0;
    private long ukupnoVrijemeRadaRadnihDrtevi = 0;
    public long brojObavljenihSeralizacija = 0;

    public long getUkupanBrojZahtjeva() {
        return ukupanBrojZahtjeva;
    }

    public void setUkupanBrojZahtjeva(boolean set) {
        if (set == true) {
            ukupanBrojZahtjeva = ukupanBrojZahtjeva + 1;
        }
    }

    public long getBrojNeispravnihZahjteva() {
        return brojNeispravnihZahjteva;
    }

    public void setBrojNeispravnihZahjteva(boolean set) {
        if (set == true) {
            brojNeispravnihZahjteva = brojNeispravnihZahjteva + 1;
        }
    }

    public long getBrojNedozvoljenihZahtjeva() {
        return brojNedozvoljenihZahtjeva;
    }

    public void setBrojNedozvoljenihZahtjeva(boolean set) {
        if (set == true) {
            brojNedozvoljenihZahtjeva = brojNedozvoljenihZahtjeva + 1;
        }
    }

    public long getBrojUspjesnihZahtjeva() {
        return brojUspjesnihZahtjeva;
    }

    public void setBrojUspjesnihZahtjeva(boolean set) {
        if (set == true) {
            brojUspjesnihZahtjeva = brojUspjesnihZahtjeva + 1;
        }
    }

    public long getBrojPrekinutihZhatjeva() {
        return brojPrekinutihZatjeva;
    }

    public void setBrojPrekinutihZahtjeva(boolean set) {
        if (set == true) {
            brojPrekinutihZatjeva = brojPrekinutihZatjeva + 1;
        }

    }

    public long getUkupnoVrijemeRadaRadnihDrtevi() {
        return ukupnoVrijemeRadaRadnihDrtevi;
    }

    public void setUkupnoVrijemeRadaRadnihDrtevi(long ukupnoVrijemeRadaRadnihDrtevi) {
        this.ukupnoVrijemeRadaRadnihDrtevi = ukupnoVrijemeRadaRadnihDrtevi;

    }

    public long getBrojObavljenihSeralizacija() {
        return brojObavljenihSeralizacija;
    }

    public void setBrojObavljenihSeralizacija(boolean set) {
        if (set == true) {
            brojObavljenihSeralizacija = brojObavljenihSeralizacija + 1;
        }
    }

    /**
     * Metoda obavlja serijalizaciju podataka evidencije rada.
     *
     * @param nazivDatEvidencije String
     *
     * @author Nikolina Bukovec
     */
    public void obaviSerijalizaciju(String nazivDatEvidencije) {
        File datEvidencije = new File(nazivDatEvidencije);
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(new FileOutputStream(datEvidencije));
            os.writeObject(this);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SerijalizatorEvidencije.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SerijalizatorEvidencije.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(SerijalizatorEvidencije.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
