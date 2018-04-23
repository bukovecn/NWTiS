package org.foi.nwtis.nikbukove.zadaca_1;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.nikbukove.konfiguracije.Konfiguracija;
import org.foi.nwtis.nikbukove.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.nikbukove.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.nikbukove.konfiguracije.NemaKonfiguracije;
import static org.foi.nwtis.nikbukove.zadaca_1.ServerSustava.datotekaKonf;
import static org.foi.nwtis.nikbukove.zadaca_1.ServerSustava.serverPauza;
import static org.foi.nwtis.nikbukove.zadaca_1.ServerSustava.serverSocket;

/**
 * Klasa koja ima osobine dretva te obavlja odgovarajući posao.
 *
 * @author Nikolina Bukovec
 */
class RadnaDretva extends Thread {

    private String nazivDretve;
    private Socket socket;
    private Konfiguracija konf;
    public Evidencija evidencija;
    long vrijeme_pocetka;
    OutputStream out;
    IOT iot;
    IOT_device device;
    Gson gson = new Gson();

    public RadnaDretva(Socket socket, String nazivDretve, Konfiguracija konf, Evidencija evidencija, IOT iot) {
        super(nazivDretve);
        this.socket = socket;
        this.nazivDretve = nazivDretve;
        this.konf = konf;
        this.evidencija = evidencija;
        this.iot = iot;
    }

    @Override
    public void interrupt() {
        try {
            evidencija.setBrojPrekinutihZahtjeva(true);
            evidencija.setUkupanBrojZahtjeva(true);
            evidencija.setUkupnoVrijemeRadaRadnihDrtevi(System.currentTimeMillis() - vrijeme_pocetka);
            this.socket.close();
            super.interrupt();
        } catch (IOException ex) {
            System.out.println("ERROR; Problem sa zatvaranjem socketa!");
        }
    }

    @Override
    public void run() {
        vrijeme_pocetka = System.currentTimeMillis();
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            this.out = os;
            StringBuffer buffer = new StringBuffer();
            while (true) {
                int znak = is.read();
                if (znak == -1) {
                    break;
                }
                buffer.append((char) znak);
            }
            System.out.println("Dretva: " + nazivDretve + " Komanda: " + buffer.toString());
            String naredba = buffer.toString().trim();
            String radi = "";
            int status = testirajNaredbu(naredba);
            if (status == 1) {
                radi = pauzairaj(naredba);
            } else if (status == 2) {
                radi = kreni(naredba);
            } else if (status == 3) {
                radi = zaustavi(naredba);
            } else if (status == 4) {
                radi = stanje(naredba);
            } else if (status == 5) {
                radi = iotAdmin(naredba);
            } else if (status == 6) {
                radi = dajEvidenciju(naredba);
            } else if (status == 7) {
                if (!serverPauza) {
                    radi = iotKlijent(naredba);
                } else {
                    radi = "ERROR; Server je pauziran!";
                }
            } else {
                if (!serverPauza) {
                    radi = cekaj(naredba);
                } else {
                    radi = "ERROR; Server je pauziran!";
                }
            }
            os.write(radi.getBytes());
            os.flush();
            socket.shutdownOutput();
        } catch (IOException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
        ServerSustava.aktivneDretve.remove(this);
        evidencija.setUkupnoVrijemeRadaRadnihDrtevi(System.currentTimeMillis() - vrijeme_pocetka);
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    /**
     * Testira sintaksu naredbe i vraća odgovarajuću oznaku (broj za svaki oblik
     * naredbe) kako bi se raspoznalo o kojoj je naredbi riječ.
     *
     * @param String naredba
     * @return int
     * @author Nikolina Bukovec
     */
    private int testirajNaredbu(String naredba) {
        String[] param = naredba.split(" ");
        String kor1 = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); (PAUZA|KRENI|ZAUSTAVI|STANJE);$";
        String kor2 = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); (IOT|EVIDENCIJA);$";
        String kor3 = "^IOT ([^\\s]+);$";
        String kor4 = "^CEKAJ ([^\\d]+);$";

        Pattern pattern = Pattern.compile(kor1);
        Matcher m = pattern.matcher(naredba);
        boolean prvi = m.matches();
        pattern = Pattern.compile(kor2);
        m = pattern.matcher(naredba);
        boolean drugi = m.matches();
        pattern = Pattern.compile(kor3);
        m = pattern.matcher(naredba);
        boolean treci = m.matches();
        pattern = Pattern.compile(kor4);
        m = pattern.matcher(naredba);
        boolean cetvrti = m.matches();
        if (prvi) {
            if ("PAUZA;".equals(param[4])) {
                return 1;
            } else if ("KRENI;".equals(param[4])) {
                return 2;
            } else if ("ZAUSTAVI;".equals(param[4])) {
                return 3;
            } else {
                return 4;
            }
        } else if (drugi) {
            if ("IOT".equals(param[4])) {
                return 5;
            } else {
                return 6;
            }
        } else if (treci) {
            return 7;
        } else if (cetvrti) {
            return 8;
        } else {
            return 0;
        }
    }

    /**
     * Provjerava je li korisnik administrator. Pauzira rad servera ukoliko do
     * tada nije bio u pauzi, inače vraća tekst pogreške.
     *
     * @param naredba String
     * @return String
     * @author Nikolina Bukovec
     */
    private String pauzairaj(String naredba) {
        boolean status = provjeriAdmina(naredba);
        if (status) {
            if (serverPauza == false) {
                serverPauza = true;
                evidencija.setBrojUspjesnihZahtjeva(true);
                evidencija.setUkupanBrojZahtjeva(true);
                return "OK";
            } else if (serverPauza) {
                evidencija.setBrojNeispravnihZahjteva(true);
                evidencija.setUkupanBrojZahtjeva(true);
                return "ERROR 11; Server je pauziran!";
            } else {
                serverPauza = true;
                evidencija.setBrojUspjesnihZahtjeva(true);
                evidencija.setUkupanBrojZahtjeva(true);
                return "OK";
            }
        } else {
            evidencija.setBrojNedozvoljenihZahtjeva(true);
            evidencija.setUkupanBrojZahtjeva(true);
            return " ERROR 10; Korisnik nije administrator ili nije unesena ispravna lozinka!";
        }
    }

    /**
     * Provjerava je li korisnik administrator. Pokreće rad servera ukoliko je
     * do tada bio u pauzi, inače vraća tekst pogreške.
     *
     * @param naredba String
     * @return String
     * @author Nikolina Bukovec
     */
    public String kreni(String naredba) {
        boolean status = provjeriAdmina(naredba);
        if (status) {
            if (serverPauza == true) {
                serverPauza = false;
                evidencija.setBrojUspjesnihZahtjeva(true);
                evidencija.setUkupanBrojZahtjeva(true);
                return "OK";
            } else {
                evidencija.setBrojNeispravnihZahjteva(true);
                evidencija.setUkupanBrojZahtjeva(true);
                return "ERROR 12; Server nije u pauzi!";
            }
        } else {
            evidencija.setBrojNedozvoljenihZahtjeva(true);
            evidencija.setUkupanBrojZahtjeva(true);
            return "ERROR 10; Korisnik nije administrator ili nije unesena ispravna lozinka!";
        }
    }

    /**
     * Provjerava je li korisnik administrator. Obavlja serijalizaciju i
     * zaustavlja rad servera .
     *
     * @param naredba String
     * @return String
     * @author Nikolina Bukovec
     */
    public String zaustavi(String naredba) throws IOException {
        boolean status = provjeriAdmina(naredba);
        if (status) {
            Konfiguracija konfig;
            try {
                konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datotekaKonf);
                String k = konfig.dajPostavku("datoteka.evidencije.rada");
                evidencija.obaviSerijalizaciju(k);
                evidencija.setBrojUspjesnihZahtjeva(true);
                evidencija.setUkupanBrojZahtjeva(true);
                evidencija.setBrojObavljenihSeralizacija(true);
                try {
                    serverSocket.close();
                } catch (SocketException ex) {
                    return "Socket zatvoren!";
                }
                return "OK";
            } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
                return ex.getMessage();
            }
        } else {
            evidencija.setBrojNedozvoljenihZahtjeva(true);
            evidencija.setUkupanBrojZahtjeva(true);
            return "ERROR 10; Korisnik nije administrator ili nije unesena ispravna lozinka!";
        }
    }

    /**
     * Provjerava je li korisnik administrator. Vraća informaciju o trenutnom
     * stanju servera.
     *
     * @param naredba String
     * @return String
     * @author Nikolina Bukovec
     */
    public String stanje(String naredba) {
        boolean status = provjeriAdmina(naredba);
        if (status) {
            if (serverPauza == false) {
                evidencija.setBrojUspjesnihZahtjeva(true);
                evidencija.setUkupanBrojZahtjeva(true);
                return "OK; 0";
            } else {
                evidencija.setBrojUspjesnihZahtjeva(true);
                evidencija.setUkupanBrojZahtjeva(true);
                return "OK; 1";
            }
        } else {
            evidencija.setBrojNedozvoljenihZahtjeva(true);
            evidencija.setUkupanBrojZahtjeva(true);
            return "ERROR 10; Korisnik nije administrator ili nije unesena ispravna lozinka!";
        }
    }

    /**
     * Provjerava je li korisnik administrator. Kreira datoteku s podacima o iot
     * uređajima, u odgovarajućem formatu.
     *
     * @param naredba String
     * @return String
     * @author Nikolina Bukovec
     */
    public String iotAdmin(String naredba) {
        boolean status = provjeriAdmina(naredba);
        if (status) {
            return "Nije implementirano!";
        } else {
            return "ERROR 10; Korisnik nije administrator ili nije unesena ispravna lozinka!";
        }
    }

    /**
     * Provjerava je li korisnik administrator. Vraća podatke iz evidencije rada
     * administratoru.
     *
     * @param naredba String
     * @return String
     * @author Nikolina Bukovec
     */
    public String dajEvidenciju(String naredba) {
        boolean status = provjeriAdmina(naredba);

        if (status) {
            try {
                Konfiguracija konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datotekaKonf);
                convertiraj(konfig);
                evidencija.setBrojUspjesnihZahtjeva(true);
                evidencija.setUkupanBrojZahtjeva(true);
                napraviTxt();
                return "OK; ZN-KODOVI " + Charset.forName(konfig.dajPostavku("skup.kodova.znakova"))
                        + ";" + " DUZINA " + "\n"
                        + ("SADRZAJ DATOTEKE EVIDENCIJA RADA:" + "\n"
                        + "-----------------------------------------------" + "\n"
                        + "1. Ukupan broj zahtjeva: " + evidencija.getUkupanBrojZahtjeva() + "\n"
                        + "2. Broj neispravnih zahtjeva: " + evidencija.getBrojNeispravnihZahjteva() + "\n"
                        + "3. Broj nedozvoljenih zahtjeva: " + evidencija.getBrojNedozvoljenihZahtjeva() + "\n"
                        + "4. Broj uspješnih zahtjeva: " + evidencija.getBrojUspjesnihZahtjeva() + "\n"
                        + "5. Broj prekinutih zahtjeva: " + evidencija.getBrojPrekinutihZhatjeva() + "\n"
                        + "6. Ukupno vrijeme rada radnih dretvi: " + evidencija.getUkupnoVrijemeRadaRadnihDrtevi() + "\n"
                        + "7. Broj obavljenih serijalizacija: " + evidencija.getBrojObavljenihSeralizacija() + "\n"
                        + "-----------------------------------------------" + "\n");
            } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
                evidencija.setBrojNeispravnihZahjteva(true);
                evidencija.setUkupanBrojZahtjeva(true);
                return "ERROR 15; Nema postavke za znakove ili nešto nije u redu s evidencijom rada!";
            }
        } else {
            evidencija.setBrojNedozvoljenihZahtjeva(true);
            evidencija.setUkupanBrojZahtjeva(true);
            return "ERROR 10; Korisnik nije administrator ili nije unesena ispravna lozinka!";
        }
    }

    /**
     * Kreira txt dokument s podacima iz evidencije rada.
     *
     * @param naredba String
     * @author Nikolina Bukovec
     */
    private void napraviTxt() {
        PrintWriter printWriter;

        try {
            printWriter = new PrintWriter("datEvidencija");
            printWriter.println("1. Ukupan broj zahtjeva: " + evidencija.getUkupanBrojZahtjeva() + "\n");
            printWriter.println("2. Broj neispravnih zahtjeva: " + evidencija.getBrojNeispravnihZahjteva() + "\n");
            printWriter.println("3. Broj nedozvoljenih zahtjeva: " + evidencija.getBrojNedozvoljenihZahtjeva() + "\n");
            printWriter.println("4. Broj uspješnih zahtjeva: " + evidencija.getBrojUspjesnihZahtjeva() + "\n");
            printWriter.println("5. Broj prekinutih zahtjeva: " + evidencija.getBrojPrekinutihZhatjeva() + "\n");
            printWriter.println("6. Ukupno vrijeme rada radnih dretvi: " + evidencija.getUkupnoVrijemeRadaRadnihDrtevi() + "\n");
            printWriter.println("7. Broj obavljenih serijalizacija: " + evidencija.getBrojObavljenihSeralizacija() + "\n");
            printWriter.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Čita podatke iz datoteke evidencije rada (bin format).
     *
     * @param konf Konfiguracija
     * @author Nikolina Bukovec
     */
    public void convertiraj(Konfiguracija konf) {
        String datoteka = konf.dajPostavku("datoteka.evidencije.rada");
        try {
            FileInputStream fis = new FileInputStream(datoteka);
            try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                evidencija = (Evidencija) ois.readObject();
            }
        } catch (IOException i) {
            System.out.println("ERROR; Problem kod otvaranja datoteke evidencije");
        } catch (ClassNotFoundException c) {
            System.out.println("ERROR; Problem kod otvaranja datoteke evidencije");
        }
    }

    /**
     * Ažurira ili dodaje novi IOT uređaj, sa podacima iz unesenog Json sadrzaja
     * datoteke.
     *
     * @param naredba String
     * @return String
     * @author Nikolina Bukovec
     */
    public String iotKlijent(String naredba) {
        String[] param = naredba.split(" ");
        String sadrzaj = param[1].substring(0, (param[1].length() - 1));
        device = citajSadrzajJSon(sadrzaj);
        if (device != null) {
            String ret = iot.azurirajUredaj(device);
            return ret;
        } else {
            return "ERROR; Ne postoji zapis o tom uređaju!";
        }
    }

    /**
     * Čita Json sadržaj (dobiven iz datoteke) i vraća podatak u obliku instance
     * IOT_device objekta.
     *
     * @param sadrzaj String
     * @return IOT_device
     * @author Nikolina Bukovec
     */
    private IOT_device citajSadrzajJSon(String sadrzaj) {
        try {
            return gson.fromJson(sadrzaj, IOT_device.class);
        } catch (JsonSyntaxException e) {
            System.out.println("ERROR 20; Format JSON zapisa nije dobar!");
        }
        return null;
    }

    /**
     * Realizira čekanje za uneseni broj sekundi (pretvorenih u milisekunde) i
     * vraća odgovarajući odgovor klijentu.
     *
     * @param naredba String
     * @return String
     * @author Nikolina Bukovec
     */
    public String cekaj(String naredba) {
        String[] param = naredba.split(" ");
        String cekaSec = param[1].substring(0, (param[1].length() - 1));
        try {
            sleep(Integer.parseInt(cekaSec) * 1000);
            evidencija.setBrojUspjesnihZahtjeva(true);
            evidencija.setUkupanBrojZahtjeva(true);
            return "OK";
        } catch (InterruptedException ex) {
            evidencija.setBrojNeispravnihZahjteva(true);
            evidencija.setUkupanBrojZahtjeva(true);
            return "ERROR 22; Cekanje neuspjesno!";
        }
    }

    /**
     * Provjerava je li korisnik administrator. Vraća true ili false.
     *
     * @param naredba String
     * @return boolean
     */
    public boolean provjeriAdmina(String naredba) {
        String[] param = naredba.split(" ");
        String sintaksaCijelo = null;
        String postavka = null;
        String kor = param[1].substring(0, (param[1].length() - 1));
        String lozinka = param[3].substring(0, (param[3].length() - 1));
        try {
            Konfiguracija konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datotekaKonf);
            sintaksaCijelo = ("admin." + kor).toString();
            if (konfig.postojiPostavka(sintaksaCijelo) == true) {
                postavka = konfig.dajPostavku(sintaksaCijelo);
                if (postavka.equals(lozinka)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            return false;
        }
    }
}
