package com.example.michal_229.myapp2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Connection;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;



public class MainActivity7 extends ActionBarActivity {

    Button buttonCalyPlan, buttonNajblizszyPlan, buttonEksport, buttonZalogujEdziekanat, buttonZalogujGoogle;
    EditText editTextInformacje;

    String login = null;
    String password = null;
    boolean loggedToEdziekanat = false;
    boolean loggedToGoogle = false;

    PlanWorker pw = new PlanWorker(3);
    PlanLekcji pl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity7);

        buttonCalyPlan = (Button) findViewById(R.id.buttonCalyPlan);
        buttonNajblizszyPlan = (Button) findViewById(R.id.buttonNajblizszyPlan);
        buttonZalogujEdziekanat = (Button) findViewById(R.id.buttonZalogujEdziekanat);

        editTextInformacje = (EditText) findViewById(R.id.editTextInformacje);

        SharedPreferences settings = getSharedPreferences("asdf", 0);
        login = settings.getString("login","loginDefaultValue");
        password = settings.getString("pass","passDefaultValue");

        if (pl == null) {
            if (login != null && password != null) {
                pw.execute();
            }
            else {
                startActivity(new Intent(MainActivity7.this, AktywnoscLogowania.class));
            }
        }
        else {
            editTextInformacje.setText(pl.getNajblizszeLekcje(12).toString());
        }


        //editTextInformacje.setText("nie otrzymano danych");

        /*if (login != null || password != null) loggedToEdziekanat = true;

        if (!loggedToEdziekanat) {
            editTextInformacje.setText("musisz sie zalogowac!");
        }
        else {
            pw.execute();
        }*/


        buttonCalyPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pw = new PlanWorker(0);
                //pw.execute();



                if (pl != null)
                    editTextInformacje.setText(pl.toString());
                else if (loggedToEdziekanat)
                    editTextInformacje.setText("nie otrzymano danych");
                else
                    editTextInformacje.setText("musisz się zalogować do edziekanatu!");
            }
        });

        buttonNajblizszyPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //pw = new PlanWorker(1);
            //pw.execute();
            if (pl != null)
                editTextInformacje.setText(pl.getNajblizszeLekcje(12).toString());
            else if (loggedToEdziekanat)
                editTextInformacje.setText("nie otrzymano danych");
            else
                editTextInformacje.setText("musisz się zalogować do edziekanatu!");


            }
        });




        buttonZalogujEdziekanat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (!loggedToEdziekanat) {


                startActivity(new Intent(MainActivity7.this, AktywnoscLogowania.class));

                //    try {
                //        pw.execute();
                //        loggedToEdziekanat = true;
                //    }
                //    catch (Exception e) {
                //        editTextInformacje.setText("błąd połączenia");
                //    }
                //}
                //else {
                //    editTextInformacje.setText("jestes juz zalogowany!");
                //}
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity7, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private class PlanWorker extends AsyncTask<Void, String, Void>{
        public PlanLekcji planLekcji;

        String tempUrl; Document tempDoc;
        String url_ = "https://s1.wcy.wat.edu.pl/ed/";


        String rokAkademicki_Zimowy = "20144";
        String rokAkademicki_Letni = "20145";

        String grupa = "I4B1S4";


        String mid = "328";
        String iid = rokAkademicki_Letni; // tu dac ifa, ktory zdecyduje czy semestr jest letni czy zimowy
        String vrf = mid + iid;
        String sid;
        String tablicaPrzedmiotow[][][];


        int opcja = 1;

        // 0 - caly plan
        // 1 - najblizsze zajecia
        // 2 - zapis na kalendarz google
        PlanWorker(int option) {
            this.opcja = option;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //editTextInformacje.setText("");
            try {
                initCertificateManager();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            publishProgress("logowanie do edziekanatu...");

            //Scanner in = new Scanner(System.in);
            //System.out.print("login: "); login = in.nextLine();
            //System.out.print("password: "); password = in.nextLine();

            //publishProgress("ignorowanie certyfikatow");

            try {
                initCertificateManager(); // potrzebne, aby zignorowac braki ertyfikatow (Handshake error)

                //publishProgress("zignorowano certyfikaty");

                //publishProgress("tworzenie planu");


                //publishProgress("wypelnianie planu");

                try {
                    planLekcji = new PlanLekcji();
                    wypelnijPlanLekcjami(planLekcji);
                    if (planLekcji.toString().length() > 50)
                        switch (opcja) {
                            case 0:
                                publishProgress(planLekcji.toString());
                                pl = planLekcji;
                                break;
                            case 1:
                                publishProgress(planLekcji.getNajblizszeLekcje(12).toString());
                                pl = planLekcji;
                                break;
                            default:
                                publishProgress(planLekcji.getNajblizszeLekcje(12).toString());
                                pl = planLekcji;
                                break;
                        }
                    else {
                        publishProgress("błąd logowania");
                    }

                } catch (IOException e) {
                    publishProgress("nie udalo sie wypelnic planu");
                }

            } catch (NoSuchAlgorithmException e) {
                publishProgress("nie udalo sie zignorowac certyfikatow: NoSuchAlgorithmException");
            } catch (KeyManagementException e) {
                publishProgress("nie udalo sie zignorowac certyfikatow: KeyManagementException");
            }







            if (planLekcji == null)
                startActivity(new Intent(MainActivity7.this, AktywnoscLogowania.class));

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //super.onProgressUpdate(values);
            //editTextInformacje.setText(StringUtil.join(Arrays.asList(values), " "));
            editTextInformacje.setText(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }






        public int mapRomanToArabicMonth(String romNum) {
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

        public int[] mapBlokToGodzina(int nrBloku) {
            int mapaBlokuNaGodzine[][] = {{8,00}, {9,50}, {11,40}, {13,30}, {15,45}, {17,35}, {19,25}};
            return mapaBlokuNaGodzine[nrBloku];
        }

        public void initCertificateManager() throws NoSuchAlgorithmException, KeyManagementException {
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

        private void wypelnijPlanLekcjami(PlanLekcji pl) throws IOException {
            // pobranie html strony logowania, pobranie sid, logowanie
            tempDoc = Jsoup.connect(url_).get();                                            //System.out.println(tempDoc.body().html());
            sid = tempDoc.getElementsByTag("form").attr("action").substring(10);            System.out.println(sid);
            tempUrl = url_ + "index.php?" +  sid;                                           System.out.println(tempUrl);
            Connection.Response res = Jsoup.connect(tempUrl).data("formname", "login", "userid", login, "password", password).method(Connection.Method.POST).execute();

            // pobranie html z tabela planu zajec -> tempDoc
            String getPlan = "&mid=" + mid + "&iid="+iid+"&vrf="+vrf+"&rdo=1&pos=0&exv="+grupa+"&vrf=!125&rdo=1&pos=0"; // url wymuszajacy przekierowanie (status 302 -> 200), ominiecie &t=xxxxxxx
            tempUrl = url_ + "logged.php?" + sid + getPlan;                                 System.out.println(tempUrl);
            res = Jsoup.connect(tempUrl).method(Connection.Method.GET).timeout(10000).execute();



            tempDoc = res.parse();
                                                            //System.out.println(tempDoc.body().html())

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
            try {
                String dataPierwszegoDnia[] = daty.first().text().split(" "); // potencjalnie NullPointerException

                Calendar calendar = new GregorianCalendar(
                        Integer.parseInt(iid.substring(0, iid.length() - 2) + iid.charAt(iid.length() - 1))
                        , mapRomanToArabicMonth(dataPierwszegoDnia[1])
                        , Integer.parseInt(dataPierwszegoDnia[0])
                );

                // wyswietlanie dat dnia i przedmiotow wystepujacych w dany dzien (string)
                for (int tyg = 0; tyg < iloscTygodni; tyg++)
                    for (int dt = 0; dt < iloscDniTyg; dt++) {
                        boolean wyswietlDate = true;
                        for (int b = 0; b < iloscBlokow; b++) {
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
            catch (NullPointerException e) {
                publishProgress("błąd logowania");
                loggedToEdziekanat = false;
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
}

