package com.yanxit.currencies;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.yanxit.currencies.utils.WebResourceAccessor;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends Activity {
    public static final String INTENT_CURRENCIES = "currencies";
    public static final String URL_CODES = "http://openexchangerates.org/api/currencies.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        Log.e(this.getClass().getName(), "1");
        new AsyncTask<String,Void,Map<String,String>>(){
            @Override
            protected Map<String,String> doInBackground(String... params) {
                Log.e(this.getClass().getName(), "2");
                return WebResourceAccessor.getObjectFromUrl(params[0], HashMap.class);
            }

            @Override
            protected void onPostExecute(Map<String, String> currencies) {
                Intent mainActivityIntent = new Intent(SplashActivity.this,MainActivity.class);
                mainActivityIntent.putExtra(INTENT_CURRENCIES,(HashMap)currencies);
                startActivity(mainActivityIntent);
                finish();
            }
        }.execute(URL_CODES);
    }

}
