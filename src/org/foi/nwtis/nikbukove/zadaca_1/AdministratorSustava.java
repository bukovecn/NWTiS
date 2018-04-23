package org.foi.nwtis.nikbukove.zadaca_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.nikbukove.konfiguracije.Konfiguracija;

/**
 * Klasa koja nasljeđuje svojstva KorisnikaSustava i realizira funkcionalnost
 * administratora sustava kao jedne vrste korisnika.
 *
 * @author Nikolina Bukovec
 */
public class AdministratorSustava extends KorisnikSustava {

    Konfiguracija konf;
    KorisnikSustava korisnikSustava;
    String naredba = "";
    String radim = "";

    public AdministratorSustava(KorisnikSustava korisnikSustava) {
        super();
        this.korisnikSustava = korisnikSustava;
    }

    /**
     * Preuzima kontrolu kao administrator, nad daljnim radom. Kreira novi
     * socket na unesenoj adresi i portu.
     *
     * @author Nikolina Bukovec
     */
    public void preuzmiKontorolu() {
        InputStream is = null;
        OutputStream os = null;
        Socket socket = null;
        if (korisnikSustava.provjera) {
            radim = kreirajNaredbe();
        } else {
            System.out.println("Parametri su neispravni!");
            return;
        }
            try{
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
         * @return String naredba
         * @author Nikolina Bukovec
         */
    public String kreirajNaredbe() {

        if (korisnikSustava.radi.equals("--pauza")) {
            naredba = "KORISNIK " + korisnikSustava.korisnik + "; LOZINKA " + korisnikSustava.lozinka + "; PAUZA;";
        }
        if (korisnikSustava.radi.equals("--kreni")) {
            naredba = "KORISNIK " + korisnikSustava.korisnik + "; LOZINKA " + korisnikSustava.lozinka + "; KRENI;";
        }
        if (korisnikSustava.radi.equals("--zaustavi")) {
            naredba = "KORISNIK " + korisnikSustava.korisnik + "; LOZINKA " + korisnikSustava.lozinka + "; ZAUSTAVI;";
        }
        if (korisnikSustava.radi.equals("--stanje")) {
            naredba = "KORISNIK " + korisnikSustava.korisnik + "; LOZINKA " + korisnikSustava.lozinka + "; STANJE;";
        }
        if (korisnikSustava.radi.equals("--evidencija")) {
            naredba = "KORISNIK " + korisnikSustava.korisnik + "; LOZINKA " + korisnikSustava.lozinka + "; EVIDENCIJA;";
        }
        if (korisnikSustava.radi.equals("--iot")) {
            naredba = "KORISNIK " + korisnikSustava.korisnik + "; LOZINKA " + korisnikSustava.lozinka + "; IOT;";
        }
        return naredba;
    }
}
