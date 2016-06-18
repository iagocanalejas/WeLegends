package andiag.coru.es.welegends.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;
import java.util.concurrent.Callable;

import andiag.coru.es.welegends.R;
import andiag.coru.es.welegends.Utils;
import andiag.coru.es.welegends.fragments.FindSummonerFragment;
import andiag.coru.es.welegends.fragments.SummonerHistoricFragment;
import andiag.coru.es.welegends.persistence.DBSummoner;
import andiag.coru.es.welegends.persistence.Version;
import andiag.coru.es.welegends.rest.RestClient;
import andiag.coru.es.welegends.rest.entities.Summoner;
import andiag.coru.es.welegends.rest.entities.persist.Champion;
import andiag.coru.es.welegends.rest.utils.GenericStaticData;
import bolts.Task;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySummoner extends AppCompatActivity {

    private final static String TAG = "ActivitySummoner";
    public final static String FIND_SUMMONER_FRAGMENT = "findSummonerFragment";
    public final static String SUMMONER_HISTORIC_FRAGMENT = "summonerHistoricFragment";
    public static ActivitySummoner activity = null;

    private DBSummoner db;

    public DBSummoner getDb() {
        if (db == null) {
            db = DBSummoner.getInstance(this);
        }
        return db;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActivitySummoner.activity = this;
        db = DBSummoner.getInstance(this);

        //Make checkServerVersion exec just one per run
        if (!Utils.isServerLoaded) {
            Utils.isServerLoaded = true;
            checkServerVersion();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (findViewById(R.id.fragmentContainer) != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(FIND_SUMMONER_FRAGMENT);
            String displayedFragmentTag = (fragment != null && fragment.isVisible())
                    ? FIND_SUMMONER_FRAGMENT : SUMMONER_HISTORIC_FRAGMENT;
            outState.putString("FRAGMENT", displayedFragmentTag);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summoner);

        //Initialize fragments if necessary
        if (savedInstanceState == null) {
            if (findViewById(R.id.fragmentContainer) != null) {
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer,
                        new FindSummonerFragment(), FIND_SUMMONER_FRAGMENT).commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragmentFindSummoner, new FindSummonerFragment(), FIND_SUMMONER_FRAGMENT)
                        .add(R.id.fragmentSummonerHistoric, new SummonerHistoricFragment(), SUMMONER_HISTORIC_FRAGMENT)
                        .commit();
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        ActivitySummoner.activity = null;
        this.db = null;
    }

    public void throwNewActivity(Summoner summoner, String callbackFragment) {
        Intent i = new Intent(this, ActivitySummonerData.class);
        if (callbackFragment != null) {
            i.putExtra(ActivitySummonerData.ARG_FRAGMENT, callbackFragment);
        }
        i.putExtra(ActivitySummonerData.ARG_SUMMONER, summoner);
        startActivity(i);
    }

    //region Server Static Data
    private void checkServerVersion() {
        Call<List<String>> call = RestClient.getWeLegendsData().getServerVersion();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, final Response<List<String>> response) {
                Task.callInBackground(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        String newVersion = response.body().get(0);
                        Log.d(TAG, "BACKGROUND: checkServerVersion -> FOUND: " + newVersion);
                        if (!newVersion.equals(Version.getVersion(activity))) {
                            Version.setVersion(newVersion, activity);
                            Log.d(TAG, "BACKGROUND: checkServerVersion -> NEW VERSION -> LOAD DATA");
                            //Version.setVersion(newVersion, activity);
                            String locale = getResources().getConfiguration().locale.getLanguage() + "_" + getResources().getConfiguration().locale.getCountry();

                            loadServerChampions(newVersion, locale);
                            //TODO load other data
                            return null;
                        }
                        stopProgressBar();
                        return null;
                    }
                });
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e(TAG, "BACKGROUND: checkServerVersion -> FAIL");
            }
        });
    }

    private void loadServerChampions(final String version, final String locale) {
        Call<GenericStaticData<String, Champion>> call = RestClient.getDdragonStaticData(version, locale).getChampions();
        call.enqueue(new Callback<GenericStaticData<String, Champion>>() {
            @Override
            public void onResponse(Call<GenericStaticData<String, Champion>> call, final Response<GenericStaticData<String, Champion>> response) {
                Task.callInBackground(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        if (response.body() == null) {
                            Log.d(TAG, "BACKGROUND: loadServerChampions -> FAIL");
                            if (!locale.equals(Utils.DEFAULT_LOCALE)) {
                                Log.d(TAG, "BACKGROUND: loadServerChampions -> RELOAD WITH: " + Utils.DEFAULT_LOCALE);
                                loadServerChampions(version, Utils.DEFAULT_LOCALE);
                            }
                        }
                        Log.d(TAG, "BACKGROUND: loadServerChampions -> LOADED: " + String.valueOf(response.body().getData().size()));
                        //TODO save in database
                        return null;
                    }
                });
            }

            @Override
            public void onFailure(Call<GenericStaticData<String, Champion>> call, Throwable t) {
                Log.d(TAG, "BACKGROUND: loadServerChampions -> FAIL");
                if (!locale.equals(Utils.DEFAULT_LOCALE)) {
                    Log.d(TAG, "BACKGROUND: loadServerChampions -> RELOAD WITH: " + Utils.DEFAULT_LOCALE);
                    loadServerChampions(version, Utils.DEFAULT_LOCALE);
                }
            }
        });
    }
    //endregion

    //region View Control

    public void stopProgressBar() {
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setVisibility(View.GONE);
    }

    public void startProgressBar() {
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);
    }

    //Redirect View action to fragment
    public void onClickFindSummoner(View v) {
        FindSummonerFragment fragmentFindSummoner = (FindSummonerFragment) getSupportFragmentManager()
                .findFragmentByTag(FIND_SUMMONER_FRAGMENT);

        fragmentFindSummoner.findSummoner(fragmentFindSummoner.getView());
    }

    public void onClickSwapFragment(View view) {
        if (findViewById(R.id.fragmentContainer) != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new SummonerHistoricFragment(), SUMMONER_HISTORIC_FRAGMENT)
                    .addToBackStack(null)
                    .commit();

        }
    }
    //endregion
}
