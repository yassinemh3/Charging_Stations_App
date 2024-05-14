package de.h_da.nzse.praktikum_sose22;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends AppCompatActivity {

    public static final String IS_REPAIR_MAN = "isRepairman";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }


    public void startDriverActivity(View v) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isRepairman", false); // Storing boolean - true/false
        editor.apply(); // apply changes
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        intent.putExtra(IS_REPAIR_MAN, false);
        startActivity(intent);
    }

    public void startRepairmanActivity(View v) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isRepairman", true); // Storing boolean - true/false
        editor.apply(); // apply changes
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        intent.putExtra(IS_REPAIR_MAN, true);
        startActivity(intent);
    }
}