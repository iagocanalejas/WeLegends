package andiag.coru.es.welegends.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;

import andiag.coru.es.welegends.R;
import andiag.coru.es.welegends.activities.ActivityMain;
import andiag.coru.es.welegends.entities.Summoner;
import andiag.coru.es.welegends.utils.CircledNetworkImageView;
import andiag.coru.es.welegends.utils.requests.VolleyHelper;
import andiag.coru.es.welegends.utils.static_data.APIHandler;

/**
 * Created by Iago on 11/07/2015.
 */
public class FragmentPlayerStats extends SwipeRefreshLayoutFragment {

    private static FragmentPlayerStats fragmentPlayerStats;
    private static ActivityMain activityMain;

    private View rootView;

    private ImageLoader imageLoader;
    private APIHandler apiHandler;
    private Summoner summoner;

    public FragmentPlayerStats() {
        imageLoader = VolleyHelper.getInstance(getActivity()).getImageLoader();
        apiHandler = APIHandler.getInstance();
    }

    public static void deleteFragment() {
        fragmentPlayerStats = null;
    }

    public static FragmentPlayerStats getInstance(ActivityMain aM) {
        activityMain = aM;
        if (fragmentPlayerStats != null) {
            return fragmentPlayerStats;
        }
        fragmentPlayerStats = new FragmentPlayerStats();
        return fragmentPlayerStats;
    }

    public void setSummoner(Summoner summoner) {
        this.summoner = summoner;

        CircledNetworkImageView networkImg = (CircledNetworkImageView) rootView.findViewById(R.id.imageSummoner);
        networkImg.setErrorImageResId(R.drawable.item_default);
        networkImg.setDefaultImageResId(R.drawable.item_default);
        networkImg.setImageUrl("http://ddragon.leagueoflegends.com/cdn/" + apiHandler.getServer_version() + "/img/profileicon/" +
                summoner.getProfileIconId() + ".png"
                , imageLoader);

    }

    @Override
    protected void initializeRefresh(View view) {
        setRefreshLayout((SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh));
        setColors(null); //NULL means default colors

        changeRefreshingValue(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_playerstats, container, false);

        return rootView;
    }
}
