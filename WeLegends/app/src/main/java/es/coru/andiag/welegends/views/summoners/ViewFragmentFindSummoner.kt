package es.coru.andiag.welegends.views.summoners

import es.coru.andiag.andiag_mvp.base.BaseFragmentView
import es.coru.andiag.welegends.models.database.Summoner

/**
 * Created by andyq on 09/12/2016.
 */
interface ViewFragmentFindSummoner : BaseFragmentView {
    fun onSummonerFound(summoner: Summoner)
    fun onSummonerNotFound(error: Int)
    fun onVersionUpdate(version: String)
}