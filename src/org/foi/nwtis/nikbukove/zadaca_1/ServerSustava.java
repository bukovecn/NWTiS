package org.foi.nwtis.nikbukove.zadaca_1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.nikbukove.konfiguracije.Konfiguracija;
import org.foi.nwtis.nikbukove.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.nikbukove.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.nikbukove.konfiguracije.NemaKonfiguracije;

/**
 * Klasa koja pokreće server i izvršava ostale funkcije na serveru.
 *
 * @author Nikolina Bukovec
 */
public class ServerSustava {

    public static String datotekaKonf;
    public static ServerSocket serverSocket;
    public Evidencija evidencija = new Evidencija();
    public IOT iot;
    public static ArrayList<RadnaDretva> aktivneDretve = new ArrayList<RadnaDretva>();
    public static boolean serverPauza;
    public int redniDretve = 0;

    /**
     * Provjerava jesu li uneseni potrebni argumenti (naziv datoteke
     * konfiguracije) i postoji li datoteka. Ako ne postoji prekida rad.
     *
     * @param args naziv datoteke konfiguracije
     * @author Nikolina Bukovec
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Krivi broj argumenata!");
            return;
        }
        String sintaksa = "([^\\s]+(\\.(?i)(txt|bin|json|xml)))$";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String p = sb.toString().trim();
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(p);
        boolean status = m.matches();
        if (status) {
            datotekaKonf = m.group(0).toString();
            ServerSustava ss = new ServerSustava();
            ss.pokreniPosluzitelj(datotekaKonf);
        } else {
            System.out.println("Uneseni parametri ne odgovaraju!");
        }
        return;
    }

    /**
     * Pokreće server. Server kreira i pokreće dretvu za serijalizaciju
     * evidencije rada, a zatim kreira ServerSocket na zadanom portu iz
     * postavki, maksimalnim brojem zahtjeva na čekanju i čeka da se spoji
     * korisnik.
     *
     * @param konf datoteka konfiguracije
     * @author Nikolina Bukovec
     */
    private void pokreniPosluzitelj(String konf) {
        Konfiguracija konfiguracija = null;
        File dat = new File(konf);
        if (!dat.exists()) {
            System.out.println("Datoteka konfiguracije ne postoji!");
            return;
        }
        try {
            konfiguracija = KonfiguracijaApstraktna.preuzmiKonfiguraciju(konf);
            ucitajEvidenciju(konfiguracija);
            int port = Integer.parseInt(konfiguracija.dajPostavku("port"));
            int maksCekanje = Integer.parseInt(konfiguracija.dajPostavku("maks.broj.zahtjeva.cekanje"));
            int maksDretvi = Integer.parseInt(konfiguracija.dajPostavku("maks.broj.radnih.dretvi"));
            boolean radiDok = true;
            try {
                SerijalizatorEvidencije se = new SerijalizatorEvidencije("nikbukove - Serijalizator", konfiguracija, evidencija);
                se.start();
                iot = new IOT();
                serverSocket = new ServerSocket(port, maksCekanje); 
                serverPauza = false;
                while (radiDok) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Korisnik se spojio");
                    if (aktivneDretve.size() > maksDretvi) {
                        nemaRaspolozive(socket);
                        evidencija.setBrojNedozvoljenihZahtjeva(true);
                        evidencija.setUkupanBrojZahtjeva(true);
                    } else {
                        RadnaDretva radnaDretva = new RadnaDretva(socket, "nikbukove - dretva " + Integer.toBinaryString(redniDretve), konfiguracija, evidencija, iot);
                        povecaj();
                        radnaDretva.start();
                        aktivneDretve.add(radnaDretva);
                    } 
                } 
            } catch (IOException ex) {
                Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }

    /**
     * Provjerava i povećava broj trenutnih radnih dretvi za 1 ukoliko je broj
     * manji ili jednak 63, inače kreće od 0.
     *
     * @author Nikolina Bukovec
     */
    private void povecaj() {
        if (this.redniDretve >= 63) {
            redniDretve = 0;
        } else {
            redniDretve++;
        }
    }

    /**
     * Ispisuje informaciju korisniku da nema raspoloživih radnih dretvi.
     *
     * @param socket Socket
     * @author Nikolina Bukovec
     */
    private void nemaRaspolozive(Socket socket) {
        int znak;
        try (InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();) {
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                znak = inputStream.read();
                if (znak == -1) {
                    break;
                }
                stringBuffer.append((char) znak);
            }
            outputStream.write("ERROR 01; Nema raspolozivih radnih dretvi!".getBytes());
        } catch (IOException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Provjerava postoji li datoteka evidencije rada i učitava ju ako postoji.
     *
     * @param konf
     * @author Nikolina Bukovec
     */
    private void ucitajEvidenciju(Konfiguracija konf) throws NemaKonfiguracije, NeispravnaKonfiguracija {
        String datEvidencije = konf.dajPostavku("datoteka.evidencije.rada");
        if (datEvidencije == null || datEvidencije.length() == 0) {
            throw new NemaKonfiguracije("Ne postoji datoteka s tim nazivom!");
        }
        File dat = new File(datEvidencije);
        if (!dat.exists()) {
            System.out.println("Datoteka evidencije ne postoji!");
            System.out.println("Kreiram ju sada!");
            return;
        } else if (dat.isDirectory()) {
            throw new NeispravnaKonfiguracija("Datoteka " + dat + " je direktorij!");
        }
        try {
            InputStream is = Files.newInputStream(dat.toPath(), StandardOpenOption.READ);
            try (ObjectInputStream ois = new ObjectInputStream(is)) {
                evidencija = (Evidencija) ois.readObject();
            }
            ucitajPod();
        } catch (IOException i) {
            System.out.println("ERROR; Problem kod učitavanja datoteke evidencije!, i");
        } catch (ClassNotFoundException c) {
            System.out.println("ERROR; Problem kod učitavanja datoteke evidencije, c");
        }
    }

    /**
     * Učitava podatke iz evidencija rada.
     *
     * @author Nikolina Bukovec
     */
    private void ucitajPod() {
        System.out.println("SADRZAJ DATOTEKE EVIDENCIJA RADA:" + "\n"
                + "-----------------------------------------------" + "\n"
                + "Ukupan broj zahtjeva: " + evidencija.getUkupanBrojZahtjeva() + "\n"
                + "Broj neispravnih zahtjeva: " + evidencija.getBrojNeispravnihZahjteva() + "\n"
                + "Broj nedozvoljenih zahtjeva: " + evidencija.getBrojNedozvoljenihZahtjeva() + "\n"
                + "Broj uspjesnih zahtjeva: " + evidencija.getBrojUspjesnihZahtjeva() + "\n"
                + "Broj prekinutih zahtjeva: " + evidencija.getBrojPrekinutihZhatjeva() + "\n"
                + "Ukupno vrijeme rada radnih dretvi: " + evidencija.getUkupnoVrijemeRadaRadnihDrtevi() + "\n"
                + "Broj obavljenih serijalizacija: " + evidencija.getBrojObavljenihSeralizacija() + "\n"
                + "-----------------------------------------------" + "\n");
    }
}
