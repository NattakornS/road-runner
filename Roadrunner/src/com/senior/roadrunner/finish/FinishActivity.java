package com.senior.roadrunner.finish;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.ListView;

import com.senior.roadrunner.MapsActivity;
import com.senior.roadrunner.R;
import com.senior.roadrunner.racetrack.TrackMemberList;

public class FinishActivity extends FragmentActivity {
	private int imageArra[] = { R.drawable.action_search,
			R.drawable.ic_launcher, R.drawable.bar_bg, R.drawable.blue_menu_btn };

	private String text[] = new String[] { "Nattakorn Sanpabopit",
			"Jetarin Samuel", "Pathompong Kornkaseam",
			"Thanathan Choysongkroi", "Fernando Tortoa", "Max Ratthapol",
			"Mapraw Naja", "Mai Ramita", "NNNNNNNNNNNNN NNNNNNNNNNN",
			"Jaturong Panipak" };

	private ArrayList<TrackMemberList> trackMemberList;
	 private ViewPagerAdapter mPagerAdapter;

	private com.senior.roadrunner.finish.TabsAdapter mTabsAdapter;

	private ViewPager pager;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finish_layout);

		// Intent intent = getIntent();
		// trackMemberList = (ArrayList<TrackMemberList>) intent
		// .getSerializableExtra("TrackMemberList");
		trackMemberList = MapsActivity.getTrackMemberList();

		// ViewPager myPager = (ViewPager) findViewById(R.id.finishPager);
		// ViewPagerAdapter adapter = new
		// ViewPagerAdapter(getSupportFragmentManager());
		// myPager.setAdapter(adapter);
		// myPager.setCurrentItem(0);
		//
		// myPager.setOnTouchListener(new OnTouchListener() {
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// PointF downP = new PointF();
		// PointF curP = new PointF();
		// int act = event.getAction();
		// if (act == MotionEvent.ACTION_DOWN
		// || act == MotionEvent.ACTION_MOVE
		// || act == MotionEvent.ACTION_UP) {
		// ((ViewGroup) v).requestDisallowInterceptTouchEvent(true);
		// if (downP.x == curP.x && downP.y == curP.y) {
		// return false;
		// }
		// }
		// return false;
		// }
		// });
		if (trackMemberList != null) {
			FinishAdaptor aa = new FinishAdaptor(this, trackMemberList);
			final ListView ll = (ListView) findViewById(R.id.finishListView);
			ll.setAdapter(aa);
		}
		// Utility.setListViewHeightBasedOnChildren(ll);
		initialisePaging();
	}

	public void onItemClick(int mPosition) {
		// TODO Auto-generated method stub

	}
	private void initialisePaging() {
		 
//        List<Fragment> fragments = new Vector<Fragment>();
//        fragments.add(Fragment.instantiate(this, List_View.class.getName()));
//        fragments.add(Fragment.instantiate(this, List_View.class.getName()));
//        fragments.add(Fragment.instantiate(this, FinishMapFragment.class.getName()));
//        this.mPagerAdapter  = new ViewPagerAdapter(super.getSupportFragmentManager(), fragments);
        //
        ViewPager pager = (ViewPager)super.findViewById(R.id.finishPager);
//		pager = new ViewPager(this);
//		pager.setId(R.id.finishPager);
		ActionBar bar = getActionBar();
//		setContentView(pager);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mTabsAdapter = new TabsAdapter(this, pager);
		mTabsAdapter.addTab(bar.newTab().setText("List Fragment 1"), List_View.class, null);
		mTabsAdapter.addTab(bar.newTab().setText("List Fragment 2"), FinishMyListViewFragment.class, null);
		mTabsAdapter.addTab(bar.newTab().setText("List Fragment 3"), FinishMapFragment.class, null);
//        pager.setAdapter(this.mPagerAdapter);
    }
}
