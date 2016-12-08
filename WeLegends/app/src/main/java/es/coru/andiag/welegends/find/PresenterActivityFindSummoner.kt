package es.coru.andiag.welegends.find

import android.content.Context
import android.util.Log
import es.coru.andiag.welegends.common.Presenter
import es.coru.andiag.welegends.models.Version
import es.coru.andiag.welegends.models.database.DBSummoner
import es.coru.andiag.welegends.models.entities.Champion
import es.coru.andiag.welegends.models.entities.dto.GenericStaticData
import es.coru.andiag.welegends.models.rest.RestClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.Semaphore


/**
 * Created by Canalejas on 08/12/2016.
 */
class PresenterActivityFindSummoner : Presenter<ViewActivityFindSummoner> {

    private val TAG = es.coru.andiag.welegends.find.PresenterActivityFindSummoner::class.java.simpleName

    private var view: ViewActivityFindSummoner? = null
    private var database: DBSummoner? = null

    private var semaphore: Semaphore? = null

    override fun onViewAttached(view: ViewActivityFindSummoner) {
        this.view = view
        if (database == null) {
            database = DBSummoner.getInstance(view as Context)
        }

        checkServerVersion()

    }

    override fun onViewDetached() {
        this.view = null
    }

    private fun checkServerVersion() {
        (view as ActivityFindSummoner).showLoading()
        val call: Call<List<String>> = RestClient.getWeLegendsData().getServerVersion()
        call.enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    semaphore = Semaphore(1)
                    doAsync {
                        val newVersion: String = response.body()[0]
                        Log.i(TAG, "Server Version: %s".format(newVersion))
                        if (newVersion != Version.getVersion(view as Context)) {
                            Version.setVersion(newVersion, view as Context)
                            val locale = Locale.getDefault().toString()

                            Log.i(TAG, "Updated Server Version To: %s".format(newVersion))
                            Log.i(TAG, "Mobile Locale: %s".format(locale))

                            uiThread {
                                (view as ViewActivityFindSummoner).updateVersion("Updating Data")
                            }
                            loadServerChampions(version = newVersion, locale = locale)
                            //TODO load other info
                        }
                        semaphore!!.acquire()
                        uiThread {
                            (view as ViewActivityFindSummoner).updateVersion(newVersion)
                            (view as ViewActivityFindSummoner).hideLoading()
                        }
                        semaphore!!.release()

                    }
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e(TAG, "ERROR: checkServerVersion - onFailure: %s".format(t.message))
                (view as ActivityFindSummoner).hideLoading()
            }
        })
    }

    private fun loadServerChampions(version: String, locale: String) {
        semaphore!!.acquire()
        val call = RestClient.getDdragonStaticData(version, locale).champions()
        call.enqueue(object : Callback<GenericStaticData<String, Champion>> {
            override fun onResponse(call: Call<GenericStaticData<String, Champion>>, response: Response<GenericStaticData<String, Champion>>) {
                if (response.isSuccessful) {
                    doAsync {
                        if (response.body() == null) {
                            Log.e(TAG, "ERROR: loadServerChampions - onResponse: %s".format(response.errorBody().string()))
                            if (locale != RestClient.DEFAULT_LOCALE) {
                                Log.i(TAG, "Reloading Locale From onResponse To: %s".format(RestClient.DEFAULT_LOCALE))

                                uiThread {
                                    loadServerChampions(version, RestClient.DEFAULT_LOCALE)
                                }
                            }
                        }
                        Log.i(TAG, "Loaded Champions: %s".format(response.body().data))
                        //TODO save in database

                        semaphore!!.release()

                    }
                }
            }

            override fun onFailure(call: Call<GenericStaticData<String, Champion>>, t: Throwable) {
                Log.e(TAG, "ERROR: loadServerChampions - onFailure: %s".format(t.message))
                if (locale != RestClient.DEFAULT_LOCALE) {
                    Log.i(TAG, "Reloading Locale From onFailure To: %s".format(RestClient.DEFAULT_LOCALE))
                    loadServerChampions(version, RestClient.DEFAULT_LOCALE)
                }
                //TODO handle error
            }
        })
    }

    companion object {

        private var presenter: es.coru.andiag.welegends.find.PresenterActivityFindSummoner? = null

        val instance: es.coru.andiag.welegends.find.PresenterActivityFindSummoner
            get() {
                if (presenter == null) {
                    presenter = PresenterActivityFindSummoner()
                }
                return presenter!!
            }

    }

}
