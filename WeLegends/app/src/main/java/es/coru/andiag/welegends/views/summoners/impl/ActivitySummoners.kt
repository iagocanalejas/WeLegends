package es.coru.andiag.welegends.views.summoners.impl

import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import es.coru.andiag.andiag_mvp.samples.AIActivityLoading
import es.coru.andiag.welegends.R
import es.coru.andiag.welegends.models.wrapped.api.RestClient
import es.coru.andiag.welegends.presenters.summoners.PresenterActivitySummoners


class ActivitySummoners : AIActivityLoading() {
    private val TAG = ActivitySummoners::class.java.simpleName


    @BindView(R.id.imageBackground)
    lateinit var imageBackground: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_summoner)
        ButterKnife.bind(this)

        setProgressBar(R.id.progressBar, true)
        presenter = PresenterActivitySummoners()

        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,
                FragmentFindSummoner(), FragmentFindSummoner.TAG)
                .commit()

    }

    fun setBackground(image: String) {
        val endpoint: String
        if (resources.configuration.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            endpoint = RestClient.loadingImgEndpoint + image
        } else
            endpoint = RestClient.loadingImgEndpoint + image
        Glide.with(this).load(endpoint).dontAnimate()
                .placeholder(R.drawable.background_default1).error(R.drawable.background_default1)
                .into(imageBackground)
    }

}
