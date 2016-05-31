package andiag.coru.es.welegends.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import andiag.coru.es.welegends.R;
import andiag.coru.es.welegends.rest.ApiClient;
import andiag.coru.es.welegends.rest.entities.Summoner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySummoner extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private final static String TAG = "ActivitySummoner";

    private String region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summoner);

        startSummonerListener();

        //Region picker
        Spinner spinner = (Spinner) findViewById(R.id.spinnerRegions);
        spinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        region = (String) parent.getItemAtPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void startSummonerListener(){
        EditText editText = (EditText) findViewById(R.id.editTextSummoner);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
                    onClickFindSummoner(v);
                    return true;
                }
                return false;
            }
        });
    }

    private boolean isNetworkAvailable() {
        //TODO Move to global utilities class
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    public void onClickFindSummoner(View v){
        String summonerName = ((EditText) findViewById(R.id.editTextSummoner)).getText().toString();
        if (isNetworkAvailable()) {
            summonerName = summonerName.toLowerCase().replaceAll(" ", "").replace("\n", "").replace("\r", "");
            if ((!(summonerName.isEmpty())) && (!(summonerName.equals("")))) {
                //TODO call retrofit API to get ID
                Log.d(TAG,"Searching " + summonerName + " " + region);
                Call<Summoner> call = ApiClient.get().getSummonerByName(region, summonerName);
                Log.d(TAG, call.request().toString());
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.voidSummonerError),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.networkUnavailable), Toast.LENGTH_LONG).show();
        }
    }

}
