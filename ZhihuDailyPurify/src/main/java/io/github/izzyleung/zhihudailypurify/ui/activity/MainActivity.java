package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.astuetz.PagerSlidingTabStrip;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViews;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.support.util.DateUtils;
import io.github.izzyleung.zhihudailypurify.ui.fragment.NewsListFragment;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends ActionBarActivity {
    private LinearLayout mainFrame;
    private MainPagerAdapter adapter;
    private boolean isGetFirstPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(this);
        isGetFirstPage = pref.getBoolean("get_first_page?", true);

        if (isGetFirstPage) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("get_first_page?", false);
            editor.commit();
            mainFrame = (LinearLayout) findViewById(R.id.main_frame);
        }

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.main_pager_tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.main_pager);
        viewPager.setOffscreenPageLimit(7);

        adapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabs.setViewPager(viewPager);
        tabs.setIndicatorColor(getResources().getColor(R.color.holo_blue));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(MainActivity.this);
        boolean isShowShowcase = pref.getBoolean("show_showcase?", true);
        if (isShowShowcase) {
            showCase(pref);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, PrefsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_pick_date:
                intent = new Intent();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                intent.putExtra("date", DateUtils.simpleDateFormat.format(calendar.getTime()));
                intent.setClass(MainActivity.this, PickDateActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_go_to_search:
                intent = new Intent();
                intent.setClass(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCase(SharedPreferences pref) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mainFrame.setAlpha(0.1f);
        }

        ShowcaseView.ConfigOptions options = new ShowcaseView.ConfigOptions();
        options.hideOnClickOutside = false;

        ShowcaseViews showcaseViews = new ShowcaseViews(this, new ShowcaseViews.
                OnShowcaseAcknowledged() {
            @Override
            public void onShowCaseAcknowledged(ShowcaseView showcaseView) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mainFrame.setAlpha(1.0f);
                }

                NewsListFragment firstPage = adapter.getFirstPage();
                if (firstPage != null) {
                    adapter.getFirstPage().refresh();
                }
            }
        });

        showcaseViews.addView(new ShowcaseViews.ItemViewProperties(
                R.id.action_go_to_search,
                R.string.showcase_search_title,
                R.string.showcase_search_message,
                ShowcaseView.ITEM_ACTION_ITEM,
                0.5f,
                options
        ));

        showcaseViews.addView(new ShowcaseViews.ItemViewProperties(
                R.id.action_pick_date,
                R.string.showcase_calendar_title,
                R.string.showcase_calendar_message,
                ShowcaseView.ITEM_ACTION_ITEM,
                0.5f,
                options
        ));

        showcaseViews.show();

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("show_showcase?", false);
        editor.commit();
    }

    final class MainPagerAdapter extends FragmentStatePagerAdapter {
        private WeakReference<NewsListFragment> firstPage;

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Bundle bundle = new Bundle();
            Fragment newFragment = new NewsListFragment();
            newFragment.setArguments(bundle);

            Calendar dateToGetUrl = Calendar.getInstance();
            dateToGetUrl.add(Calendar.DAY_OF_YEAR, 1 - i);
            String date = DateUtils.simpleDateFormat.format(dateToGetUrl.getTime());

            bundle.putBoolean("first_page?", i == 0);
            bundle.putBoolean("single?", false);
            bundle.putString("date", date);

            return newFragment;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object object = super.instantiateItem(container, position);

            if (position == 0 && isGetFirstPage) {
                firstPage = new WeakReference<NewsListFragment>((NewsListFragment) object);
            }

            return object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (position == 0 && isGetFirstPage && firstPage != null) {
                firstPage.clear();
            }
            super.destroyItem(container, position, object);
        }

        public NewsListFragment getFirstPage() {
            return firstPage == null ? null : firstPage.get();
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Calendar displayDate = Calendar.getInstance();
            displayDate.add(Calendar.DAY_OF_YEAR, -position);

            String date = new SimpleDateFormat(getString(R.string.display_format)).
                    format(displayDate.getTime());

            if (position == 0) {
                return getString(R.string.zhihu_daily_today) + " " + date;
            } else {
                return date;
            }
        }
    }
}