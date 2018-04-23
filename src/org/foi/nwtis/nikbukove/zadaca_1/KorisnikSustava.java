package org.foi.nwtis.nikbukove.zadaca_1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.nikbukove.konfiguracije.Konfiguracija;

/**
 * Klasa koja ovisno o vrsti korisnika instancira objekta određene klase
 * (admnistrator ili klijent).
 *
 * @author Nikolina Bukovec
 */
public class KorisnikSustava {

    String korisnik = "";
    String lozinka = "";
    String adresa = "";
    int port;
    String radi = "";
    String dat = "";
    int spavaj;
    boolean administrator = false;
    boolean klijent = false;
    public String parametri = "";
    boolean provjera = false;
    Konfiguracija konf;

    /**
     * Main metoda, kreira odgovarajući objekt klase ovisno o vrsti korisnika te
     * pokreće metodu za preuzimanje daljnje kontorole.
     *
     * @param args String[]
     * @author Nikolina Bukovec
     */
    public static void main(String[] args) {
        KorisnikSustava ks = new KorisnikSustava();
        ks.preuzmiPostavke(args);
        if (ks.administrator) {
            AdministratorSustava as = new AdministratorSustava(ks);
            as.preuzmiKontorolu();
        } else {
            KlijentSustava kls = new KlijentSustava(ks);
            kls.preuzmiKontorolu();

        }
    }

    /**
     * Preuzima unesene postavke(parametre) i šalje ih na provjeru.
     *
     * @author Nikolina Bukovec
     */
    private void preuzmiPostavke(String[] args) {
        String unosArg = "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        unosArg = sb.toString().trim();
        provjeraParametara(unosArg);
    }

    /**
     * Provjerava točnost unesenih parametara i prepoznaje je li korisnik
     * administrator ili klijent.
     *
     * @param p String
     * @author Nikolina Bukovec
     */
    public void provjeraParametara(String p) {
        parametri = p;
        String[] param = p.split(" ");
        String[] paramK = p.split(" ");
        String pocAdmin = "^-k [A-Za-z0-9_-]{3,10} -l [A-Za-z0-9_-[#!]]{3,10}$";
        String pocKlijent = "^-s (((25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})|([^\\s]+)) -p (8[0-9][0-9][0-9]|9[0-9][0-9][0-9])$";
        Pattern pattern = Pattern.compile(pocAdmin);
        Matcher m = pattern.matcher(param[0] + " " + param[1] + " " + param[2] + " " + param[3]);
        boolean status = m.matches();
        if (status) {
            klijent = false;
            administrator = true;
            korisnik = param[1].toString();
            lozinka = param[3].toString();
            provjera = provjeriSintaksuAdmin(this.parametri);
        }
        Pattern patt = Pattern.compile(pocKlijent);
        Matcher ma = patt.matcher(paramK[0] + " " + paramK[1] + " " + paramK[2] + " " + paramK[3]);
        boolean status1 = ma.matches();
        if (status1) {
            klijent = true;
            administrator = false;
            provjera = provjeriSintaksuKlijent(this.parametri);
        }
        if (administrator == false && klijent == false) {
            System.out.println("Uneseni parametri nisu dobri!");
        }
    }

    /**
     * Provjerava sintaksu unesenog parametra administratora po dijelovima.
     * Vraća true ili false osvisno o točnosti unesenog.
     *
     * @param p String
     * @return boolean
     * @author Nikolina Bukovec
     */
    private boolean provjeriSintaksuAdmin(String p) {
        String[] param1 = p.split(" ");
        String adresaS = "^-s (((25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})|([^\\s]+))$";
        String portS = "^-p (8[0-9][0-9][0-9]|9[0-9][0-9][0-9])$";
        String naredba = "^(--pauza|--kreni|--zaustavi|--stanje|--evidencija|--iot)$";
        String naredbaDat = "([^\\s]+(\\.(?i)(txt|bin|json)))$";
        Pattern pattern = Pattern.compile(adresaS);
        Matcher m = pattern.matcher(param1[4] + " " + param1[5]);
        boolean correct = m.matches();
        if (correct) {
            this.adresa = param1[5].toString();
            pattern = Pattern.compile(portS);
            m = pattern.matcher(param1[6] + " " + param1[7]);
            correct = m.matches();
            if (correct) {
                this.port = Integer.parseInt(param1[7].toString());
                pattern = Pattern.compile(naredba);
                m = pattern.matcher(param1[8]);
                correct = m.matches();
                if (correct && param1.length <= 9) {
                    this.radi = param1[8].toString();
                    return true;
                } else if (correct && param1.length > 9) {
                    this.radi = param1[8].toString();
                    pattern = Pattern.compile(naredbaDat);
                    m = pattern.matcher(param1[9]);
                    correct = m.matches();
                    if (correct) {
                        this.dat = param1[9].toString();
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }

            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Provjerava sintaksu unesenog parametra klijenta po dijelovima. Vraća true
     * ili false osvisno o točnosti unesenog.
     *
     * @param p String
     * @return boolean
     * @author Nikolina Bukovec
     */
    private boolean provjeriSintaksuKlijent(String p) {
        String[] param = p.split(" ");
        String adresaS = "^-s (((25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3})|([^\\s]+))$";
        String portS = "^-p (8[0-9][0-9][0-9]|9[0-9][0-9][0-9])$";
        String spavanje = "^(--spavanje ([1-600]{1,3}))$";
        String datoteka = "([^\\s]+(\\.(?i)(json|txt)))$";
        Pattern patt = Pattern.compile(adresaS);
        Matcher ma = patt.matcher(param[0] + " " + param[1]);
        boolean status1 = ma.matches();
        if (status1) {
            this.adresa = param[1];
            patt = Pattern.compile(portS);
            ma = patt.matcher(param[2] + " " + param[3]);
            status1 = ma.matches();
            if (status1 && param.length <= 5) {
                this.port = Integer.parseInt(param[3]);
                patt = Pattern.compile(datoteka);
                ma = patt.matcher(param[4]);
                status1 = ma.matches();
                if (status1) {
                    this.dat = param[4];
                    return true;
                } else {
                    return false;
                }
            } else if (status1 && param.length > 5) {
                this.port = Integer.parseInt(param[3]);
                patt = Pattern.compile(spavanje);
                ma = patt.matcher(param[4] + " " + param[5]);
                status1 = ma.matches();
                if (status1) {
                    this.radi = param[4];
                    this.spavaj = Integer.parseInt(param[5]);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
