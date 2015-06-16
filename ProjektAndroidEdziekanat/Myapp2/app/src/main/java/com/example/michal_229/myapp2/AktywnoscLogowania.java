package com.example.michal_229.myapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class AktywnoscLogowania extends ActionBarActivity {

    //public static final String SETTINGS_NAME = "EplaData";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aktywnosc_logowania);

        final Button button = (Button) findViewById(R.id.buttonZaloguj);
        final EditText login = (EditText) findViewById(R.id.usernameLogin);
        final EditText pass = (EditText) findViewById(R.id.passwordLogin);

        SharedPreferences settings = getSharedPreferences("asdf", 0);
        String preLogin = settings.getString("login","loginDefaultValue");
        String prePass = settings.getString("pass","passDefaultValue");

        System.out.println("Login act: login " + preLogin + ", " + prePass);

        /*if(preLogin!="loginDefaultValue" && prePass!="passDefaultValue")
        {
            startActivity(new Intent(AktywnoscLogowania.this, MainActivity7.class));
        }*/

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String text = login.getText() + " " + pass.getText();
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                SharedPreferences loginSettings = getSharedPreferences("asdf", 0);
                SharedPreferences.Editor editor = loginSettings.edit();
                editor.putString("login", login.getText().toString());
                editor.putString("pass", pass.getText().toString());
                editor.commit();

                startActivity(new Intent(AktywnoscLogowania.this, MainActivity7.class));

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
}
