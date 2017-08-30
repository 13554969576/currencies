package com.yanxit.currencies;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.yanxit.currencies.utils.PrefsMgr;
import com.yanxit.currencies.utils.WebResourceAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button btnCalc;
    private EditText edtFromCurrAmt;
    private TextView txtToCurrAmt;
    private Spinner spnFrom,spnTo;
    private String[] currencies;

    public  static final String PREF_FROM_CURR="PREF_FROM_CURR";
    public  static final String PREF_TO_CURR="PREF_TO_CURR";
    private static final String URL_BASE = "http://openexchangerates.org/api/latest.json?app_id=";
    private  String OPEN_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCalc = (Button) findViewById(R.id.btnCalc);
        edtFromCurrAmt = (EditText)findViewById(R.id.edtFromCurrAmt);
        txtToCurrAmt = (TextView)findViewById(R.id.txtToCurrAmt);
        spnFrom = (Spinner)findViewById(R.id.spnFromCurr);
        spnTo = (Spinner)findViewById(R.id.spnToCurr);

        OPEN_KEY = getKey("open_key");

        HashMap<String,String> currencyMap = (HashMap<String,String>)getIntent().getSerializableExtra(SplashActivity.INTENT_CURRENCIES);
        ArrayList<String> currencyLst = new ArrayList();
        for(Map.Entry<String,String> item: currencyMap.entrySet()){
            currencyLst.add(String.format("%s(%s)",item.getKey(),item.getValue()));
        }
        Collections.sort(currencyLst);
        currencies = currencyLst.toArray(new String[currencyLst.size()]);
        ArrayAdapter<String> currencyAdp = new ArrayAdapter<String>(this,R.layout.spinner_closed,currencies);
        spnFrom.setAdapter(currencyAdp);
        spnTo.setAdapter(currencyAdp);
        spnFrom.setOnItemSelectedListener(this);
        spnTo.setOnItemSelectedListener(this);

        if(savedInstanceState==null && PrefsMgr.getString(this,PREF_FROM_CURR)==null && PrefsMgr.getString(this,PREF_TO_CURR)==null){
            spnFrom.setSelection(getPosByCurrCode("CNY",currencies));
            spnTo.setSelection(getPosByCurrCode("USD",currencies));
            PrefsMgr.setString(this,PREF_FROM_CURR,"CNY");
            PrefsMgr.setString(this,PREF_TO_CURR,"USD");
        } else {
            spnFrom.setSelection(getPosByCurrCode(PrefsMgr.getString(this,PREF_FROM_CURR),currencies));
            spnTo.setSelection(getPosByCurrCode(PrefsMgr.getString(this,PREF_TO_CURR),currencies));
        }
        btnCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CurrencyCovertTask().execute(URL_BASE + OPEN_KEY);

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.mnu_codes:
                launchBrowser(SplashActivity.URL_CODES);
                break;
            case R.id.mnu_invert:
                invertCurrencies();
                break;
            case R.id.mnu_exit:
                finish();
                break;
        }
        return true;
    }

    public boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if(info!=null && info.isConnectedOrConnecting()) return true;
        return false;
    }

    private void launchBrowser(String sUri){
        if(isOnline()){
            Uri uri = Uri.parse(sUri);
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);

        }
    }

    private void invertCurrencies(){
        int posFrom = spnFrom.getSelectedItemPosition();
        int posTo = spnTo.getSelectedItemPosition();
        spnFrom.setSelection(posTo);
        spnTo.setSelection(posFrom);
        txtToCurrAmt.setText("");
        edtFromCurrAmt.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.spnFromCurr:
                break;
            case R.id.spnToCurr:
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private int getPosByCurrCode(String code, String[] currencies){
        if(code==null || code.trim().length()==0 || currencies==null || currencies.length==0)return 0;
        Pattern p = Pattern.compile("^"+code,Pattern.CASE_INSENSITIVE);
        for(int i=0;i<currencies.length;i++){
            if(p.matcher(currencies[i]).find()) return i;
        }
        return 0;
    }

    private String getKey(String keyName){
        AssetManager assetManager = getResources().getAssets();
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = assetManager.open("keys.properties");
            properties.load(in);
            return (String)properties.get(keyName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(in!=null) try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    public class CurrencyCovertTask extends AsyncTask<String,Void,JsonNode> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("正在进行货币换算...");
            progressDialog.setMessage("请稍候...");
            progressDialog.setCancelable(true);
            progressDialog.setButton(Dialog.BUTTON_NEGATIVE,"取消",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CurrencyCovertTask.this.cancel(true);
                    progressDialog.dismiss();
                }
            });
            progressDialog.dismiss();
        }

        @Override
        protected JsonNode doInBackground(String... params) {
            return WebResourceAccessor.getObjectFromUrl(params[0],JsonNode.class);
        }

        @Override
        protected void onPostExecute(JsonNode ret) {
            Double toAmt = 0.0;
            String fromCurr = currencies[spnFrom.getSelectedItemPosition()].substring(0,3);
            String toCurr = currencies[spnTo.getSelectedItemPosition()].substring(0,3);
            if(ret == null){
                throw new RuntimeException("no data available");
            }
            double fromAmt;
            try {

                fromAmt = Double.parseDouble(edtFromCurrAmt.getText().toString());
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
            JsonNode toRateNode = ret.get("rates").get(toCurr);
            JsonNode fromRateNode = ret.get("rates").get(fromCurr);
            if(fromRateNode==null){
                throw new RuntimeException("the currency converting from is not available");
            }
            if(toRateNode==null){
                throw new RuntimeException("the currency converting to is not available");
            }
            double baseAmt = fromAmt / fromRateNode.asDouble();
            toAmt = baseAmt * toRateNode.asDouble();
            txtToCurrAmt.setText(toAmt.toString());
        }
    }
















}
