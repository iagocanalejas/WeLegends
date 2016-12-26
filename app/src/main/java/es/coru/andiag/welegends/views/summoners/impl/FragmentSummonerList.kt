package es.coru.andiag.welegends.views.summoners.impl

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import butterknife.BindView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import es.coru.andiag.welegends.R
import es.coru.andiag.welegends.models.Version
import es.coru.andiag.welegends.models.wrapped.database.Summoner
import es.coru.andiag.welegends.presenters.summoners.PresenterFragmentSummonerList
import es.coru.andiag.welegends.views.adapters.AdapterSummonerList
import es.coru.andiag.welegends.views.summoners.ViewFragmentSummonerList
import es.coru.andoiag.andiag_mvp_utils.fragments.AIButterFragment
import java.util.*


/**
 * Created by andyq on 09/12/2016.
 */
class FragmentSummonerList : AIButterFragment<PresenterFragmentSummonerList>(), ViewFragmentSummonerList {

    @BindView(R.id.recyclerSummoners)
    lateinit var recycler: RecyclerView

    var adapter: AdapterSummonerList? = null

    //region Fragment Lifecycle
    override fun initLayout() {
        mFragmentLayout = R.layout.fragment_summoner_list
    }

    override fun initPresenter() {
        mPresenter = PresenterFragmentSummonerList.getInstance()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(mParentContext)
        initAdapter()

    }
    //endregion

    //region View Config
    private fun initAdapter() {
        adapter = AdapterSummonerList(R.layout.item_summoner_list, ArrayList<Summoner>(), Version.getVersion(null)!!)
        adapter!!.emptyView = LayoutInflater.from(mParentContext).inflate(R.layout.empty_summoner_view, null)
        adapter!!.openLoadAnimation()
        recycler.adapter = adapter
        recycler.addOnItemTouchListener(object : OnItemClickListener() {
            override fun onSimpleItemClick(adapter: BaseQuickAdapter<*, *>, view: View?, position: Int) {
                /**
                 * Launch [ActivityMain] on list element click
                 */
                startActivity((mParentContext as ActivitySummoners)
                        .createMainIntent((adapter as AdapterSummonerList)
                                .getItem(position), true))
                (mParentContext as ActivitySummoners).onBackPressed()
            }
        })
        mPresenter.loadSummoners()
    }
    //endregion

    //region Callbacks
    override fun onSummonersLoaded(summoners: List<Summoner>) {
        adapter!!.setNewData(summoners)
    }

    override fun onSummonersEmpty(error: Int?) {
        adapter!!.setNewData(null)
        Toast.makeText(mParentContext, error!!, Toast.LENGTH_SHORT).show()
    }
    //endregion

    companion object {
        val TAG: String = FragmentSummonerList::class.java.simpleName
    }
}
