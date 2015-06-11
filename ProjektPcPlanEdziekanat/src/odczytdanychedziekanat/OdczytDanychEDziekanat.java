/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odczytdanychedziekanat;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 *
 * @author Michal B‚
 */
public class OdczytDanychEDziekanat {
    static String tempUrl; static Document tempDoc;
    static String url_ = "https://s1.wcy.wat.edu.pl/ed/";
    static String login = "";
    static String password = "";

    static String rokAkademicki_Zimowy = "20144"; 
    static String rokAkademicki_Letni = "20145"; 

    static String grupa = "I4B1S4";


    static String mid = "328";
    static String iid = rokAkademicki_Letni; // tu dac ifa, ktory zdecyduje czy semestr jest letni czy zimowy
    static String vrf = mid + iid;
    static String sid;
    static String tablicaPrzedmiotow[][][];
    
    
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        //javascript:executeCmm(328, 20145, 1, ''); // sem. letni
        //javascript:executeCmm(328, 20144, 1, ''); // sem zimowy
        //String jsExecCmm = "https://s1.wcy.wat.edu.pl/ed/menubody.js";
        //&mid=328&iid=20145&pos=0&rdo=1&t=6800770
        //&t=xxxxxxx trzeba obliczyc // albo i nie, wystarczy wymuszenie przekierowania :D
        //&mid=328&iid=20145&vrf=32820145&rdo=1&pos=0&exv=I4B1S4&vrf=!125&rdo=1&pos=0
        
        //String pobierzCsv = "&mid=328&iid=20145&vrf=32820145&rdo=1&pos=0&exv=I4B1S4&opr=DTXT";
        //nie uzywam bo nie moge sie dobrac do zawartosci pliku
    	
    	System.out.println("Test odczytu planu z e-dziekanatu.");
    	
    	Scanner in = new Scanner(System.in);
    	System.out.print("login: "); login = in.nextLine();
    	System.out.print("password: "); password = in.nextLine();
        
        initCertificateManager(); // potrzebne, aby zignorowac braki ertyfikatow (Handshake error)
        
        PlanLekcji planLekcji = new PlanLekcji();
        wypelnijPlanLekcjami(planLekcji);
        
        //System.out.println(planLekcji.toString());
        
        System.out.println(planLekcji.getNajblizszeLekcje(12).toString());
    }


    
    

    
    
    
    
    
    public static int mapRomanToArabicMonth(String romNum) {
        Map<String, Integer> dictionary = new HashMap<>();
        dictionary.put("I", 0);
        dictionary.put("II", 1);
        dictionary.put("III", 2);
        dictionary.put("IV", 3);
        dictionary.put("V", 4);
        dictionary.put("VI", 5);
        dictionary.put("VII", 6);
        dictionary.put("VIII", 7);
        dictionary.put("IX", 8);
        dictionary.put("X", 9);
        dictionary.put("XI", 10);
        dictionary.put("XII", 11);
        
        return dictionary.get(romNum).intValue();
    }
    
    public static int[] mapBlokToGodzina(int nrBloku) {
        int mapaBlokuNaGodzine[][] = {{8,00}, {9,50}, {11,40}, {13,30}, {15,45}, {17,35}, {19,25}};
        return mapaBlokuNaGodzine[nrBloku];
    }
    
    public static void initCertificateManager() throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    private static void wypelnijPlanLekcjami(PlanLekcji pl) throws IOException {
        // pobranie html strony logowania, pobranie sid, logowanie
        tempDoc = Jsoup.connect(url_).get();                                            //System.out.println(tempDoc.body().html());
        sid = tempDoc.getElementsByTag("form").attr("action").substring(10);            System.out.println(sid);
        tempUrl = url_ + "index.php?" +  sid;                                           System.out.println(tempUrl);
        Connection.Response res = Jsoup.connect(tempUrl).data("formname", "login", "userid", login, "password", password).method(Method.POST).execute();
        
        // pobranie html z tabela planu zajec -> tempDoc
        String getPlan = "&mid=" + mid + "&iid="+iid+"&vrf="+vrf+"&rdo=1&pos=0&exv="+grupa+"&vrf=!125&rdo=1&pos=0"; // url wymuszajacy przekierowanie (status 302 -> 200), ominiecie &t=xxxxxxx
        tempUrl = url_ + "logged.php?" + sid + getPlan;                                 System.out.println(tempUrl);
        res = Jsoup.connect(tempUrl).method(Method.GET).timeout(10000).execute();
        tempDoc = res.parse();                                                          //System.out.println(tempDoc.body().html());
        
        //pobieranie dat poniedzialkow
        Elements daty = tempDoc.select("th.thFormList1HSheTeaGrpHTM3");
        
        // pobieranie elementow planu (w tym pustych blokow)
        Elements przedmioty = tempDoc.select("td.tdFormList1DSheTeaGrpHTM3");
        
        int iloscDniTyg = 7; int iloscTygodni = daty.size(); int iloscBlokow = 7;
        tablicaPrzedmiotow = new String[iloscDniTyg][iloscTygodni][iloscBlokow];       
        
        // wypelanie tablicaPrzedmiotow
        int i = 0;
        for (Element e : przedmioty) {
            int dt = i / (iloscTygodni*iloscBlokow);
            int tyg = i % iloscTygodni;
            int b = (i / iloscTygodni) % iloscBlokow ;
            tablicaPrzedmiotow[dt][tyg][b] = e.text();
            //e = e.select("td[class^=tdFormList1DSheTeaGrpHTM4]");
            //System.out.println("\n\n- dzien tygodnia : " + dt + ", tydzien : " + tyg + ", blok : " + b + " ---------------\n" + e.text());
            //System.out.println(e.attr("title"));
            i++;
        }
        
        // generowanie daty pierwszego dnia na planie
        SimpleDateFormat formatKalendarza = new SimpleDateFormat("dd MMMM yyyy");
        String dataPierwszegoDnia[] = daty.first().text().split(" ");
        Calendar calendar = new GregorianCalendar(
                            Integer.parseInt(iid.substring(0, iid.length() - 2) + iid.charAt(iid.length()-1))
                            ,mapRomanToArabicMonth(dataPierwszegoDnia[1])
                            ,Integer.parseInt(dataPierwszegoDnia[0])
                    );
        
        // wyswietlanie dat dnia i przedmiotow wystepujacych w dany dzien (string)
        for (int tyg = 0; tyg < iloscTygodni; tyg++)
            for (int dt = 0; dt<iloscDniTyg; dt++) {
                boolean wyswietlDate = true;
                for (int b = 0; b<iloscBlokow; b++) {
                    String blok = tablicaPrzedmiotow[dt][tyg][b];
                    if (blok.length() > 1) {
                        if (wyswietlDate) {
                            //System.out.println(formatKalendarza.format(calendar.getTime()));
                            wyswietlDate = false;
                        }
                        //System.out.println(blok);
                        String paramBloku[] = blok.split(" ");
                        
                        Calendar c = (Calendar) calendar.clone();
                        c.set(Calendar.HOUR, mapBlokToGodzina(b)[0]);
                        c.set(Calendar.MINUTE, mapBlokToGodzina(b)[1]);
                        pl.addLekcje( 
                            new Lekcja( 
                                paramBloku[0],
                                paramBloku[1],
                                paramBloku[paramBloku.length - 1],
                                c, 
                                paramBloku[2]
                            )
                        );
                    }
                    
                    
                    
                    
                }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
    }
}




class PlanLekcji {
    public List<Lekcja> listaLekcji = new ArrayList<>();

    public PlanLekcji() {

    }

    public void addLekcje(Lekcja l) {
        listaLekcji.add(l);
    }

    public PlanLekcji getNajblizszeLekcje(int ilosc) {
        PlanLekcji wynik = new PlanLekcji();
        Calendar teraz = Calendar.getInstance();
        
        for (Lekcja l : listaLekcji) {
            if (l.getGodzinaDataRozpoczecia().after(teraz))  
                wynik.addLekcje(l);
            if (wynik.size() >= ilosc) break;
        }
        
        return wynik;
    }
    
    public int size() {
        return listaLekcji.size();
    }
    public boolean dodajLekcjeDoKalendarzaGoogle() {
        return false;
    }
    
    public boolean dodajLekcjeDoKalendarzaOneDrive() {
        return false;
    }
    
    @Override
    public String toString() {
        String wynik = "";
        for (Lekcja l : listaLekcji) {
            wynik += l.toString() + "\n";
        }

        return wynik;
    }

}
    





class Lekcja {
    public String skrotNazwyPrzedmiotu;
    public String typZajec; //"w", "c", "s", "p", "l"
    public String numerLekcji;
    public Calendar godzinaDataRozpoczecia;
    public String sala;

    public Lekcja(String skrotNazwyPrzedmiotu, 
            String typZajec, 
            String numerLekcji, 
            Calendar godzinaDataRozpoczecia, 
            String sala) {

        this.skrotNazwyPrzedmiotu = skrotNazwyPrzedmiotu;
        this.typZajec = typZajec;
        this.numerLekcji = numerLekcji;
        this.godzinaDataRozpoczecia = godzinaDataRozpoczecia;
        this.sala = sala;
    }

    public String getSkrotNazwyPrzedmiotu() {
        return skrotNazwyPrzedmiotu;
    }

    public String getTypZajec() {
        return typZajec;
    }

    public String getNumerLekcji() {
        return numerLekcji;
    }

    public Calendar getGodzinaDataRozpoczecia() {
        return godzinaDataRozpoczecia;
    }

    public String getSala() {
        return sala;
    }



    @Override
    public String toString() {
        return (new SimpleDateFormat("MMM dd - hh:mm")).format(godzinaDataRozpoczecia.getTime()) 
                + ", " + skrotNazwyPrzedmiotu 
                + ", " + typZajec 
                + ", " + sala 
                + ", " + numerLekcji;
    }



}