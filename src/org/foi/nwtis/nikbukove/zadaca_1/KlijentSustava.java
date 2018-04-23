package org.foi.nwtis.nikbukove.zadaca_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.nikbukove.konfiguracije.KonfiguracijaJSON;
import org.foi.nwtis.nikbukove.konfiguracije.NemaKonfiguracije;

/**
 * Klasa koja nasljeđuje svojstva KorisnikaSustava i realizira funkcionalnost
 * klijenta sustava kao jedne vrste korisnika.
 *
 * @author Nikolina Bukovec
 */
public class KlijentSustava extends KorisnikSustava {

    KorisnikSustava korisnikSustava;
    String naredba = "";
    String radim = "";
    IOT iot;

    public KlijentSustava(KorisnikSustava korisnikSustava) {
        super();
        this.korisnikSustava = korisnikSustava;
    }

    /**
     * Preuzima kontrolu kao klijent, nad daljnim radom. Kreira novi socket na
     * unesenoj adresi i portu.
     *
     * @author Nikolina Bukovec
     */
    public void preuzmiKontorolu() {
        InputStream is = null;
        OutputStream os = null;
        Socket socket = null;
        System.out.println(korisnikSustava.provjera);
        if (korisnikSustava.provjera) {
            radim = kreirajNaredbe();
        } else {
            System.out.println("Parametri su neispravni!");
            return;
        }
        try {
            socket = new Socket(korisnikSustava.adresa, korisnikSustava.port);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            StringBuffer buffer = new StringBuffer();
            os.write(radim.getBytes());
            os.flush();
            socket.shutdownOutput();
            while (true) {
                int znak = is.read();
                if (znak == -1) {
                    break;
                }
                buffer.append((char) znak);
            }
            System.out.println("Odgovor: " + buffer.toString());
        } catch (IOException ex) {
            Logger.getLogger(AdministratorSustava.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                System.out.println("ERROR; Problem sa zatvaranjem socketa!");
            }
        }
    }

    /**
     * Kreira naredbe na temelju unesenih parametara i vraća istu (ovisno o
     * unesenom parametru).
     *
     * @return String
     * @author Nikolina Bukovec
     */
    public String kreirajNaredbe() {
        if (korisnikSustava.radi.equals("--spavanje")) {
            naredba = "CEKAJ " + korisnikSustava.spavaj + ";";
        }
        if (korisnikSustava.radi == "" && korisnikSustava.dat != null) {
            naredba = "IOT " + citajJsonDatoteku(korisnikSustava.dat) + ";";
        }
        return naredba;
    }

    /**
     * Čita sadržaj Json datoteke čiji naziv je unesen kao parametar.
     *
     * @return String sadrzaj
     * @author Nikolina Bukovec
     */
    private String citajJsonDatoteku(String datoteka) {
        try {
            if (datoteka == null || datoteka.length() == 0) {
                throw new NemaKonfiguracije("Treba unjeti naziv datoteke!");
            }
            File datKonf = new File(datoteka);
            if (!datKonf.exists()) {
                throw new NemaKonfiguracije("Datoteka: " + datoteka + " ne postoji!");
            }
            try {
                byte[] encoded = Files.readAllBytes(Paths.get(datoteka));
                return new String(encoded, StandardCharsets.UTF_8);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(KonfiguracijaJSON.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(KlijentSustava.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NemaKonfiguracije ex) {
            System.out.println("Nema datoteke sa IOT podatcima");
        }
        return "No file";
    }
}
