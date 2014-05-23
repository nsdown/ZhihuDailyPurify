package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.adapter.NewsAdapter;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.util.CommonUtils;
import io.github.izzyleung.zhihudailypurify.support.util.DateUtils;
import io.github.izzyleung.zhihudailypurify.support.util.URLUtils;
import io.github.izzyleung.zhihudailypurify.task.BaseDownloadTask;
import io.github.izzyleung.zhihudailypurify.ui.view.IzzySearchView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SearchActivity extends ActionBarActivity {
    private List<String> dateResultList = new ArrayList<String>();
    private List<DailyNews> newsList = new ArrayList<DailyNews>();
    private NewsAdapter newsAdapter;
    private IzzySearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        searchView = new IzzySearchView(this);
        searchView.setIconifiedByDefault(true);
        searchView.setIconified(false);
        searchView.setOnCloseListener(new IzzySearchView.OnCloseListener() {
            public boolean onClose() {
                return true;
            }
        });
        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new IzzySearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //noinspection deprecation
                new SearchTask().execute(URLEncoder.encode(query.trim()).replace("+", "%20"));
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        RelativeLayout relative = new RelativeLayout(this);
        relative.addView(searchView);
        getSupportActionBar().setCustomView(relative);

        StickyListHeadersListView listView = (StickyListHeadersListView)
                findViewById(R.id.result_list);
        newsAdapter = new NewsAdapter(this,
                newsList,
                dateResultList);
        listView.setAdapter(newsAdapter);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            listView.setDivider(null);
        }

        listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                CommonUtils.listOnClick(SearchActivity.this, newsList.get(position));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Crouton.cancelAllCroutons();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class SearchTask extends BaseDownloadTask<String, Void, Void> {
        private boolean isSearchSuccess = true;
        private boolean isResultNull = false;

        private ProgressDialog dialog;

        private Type newsType = new TypeToken<DailyNews>() {

        }.getType();
        private Gson gson = new GsonBuilder().create();

        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                getString(R.string.display_format));

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(SearchActivity.this);
            dialog.setMessage(getString(R.string.searching));
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    SearchTask.this.cancel(true);
                }
            });
            dialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            String result;
            try {
                result = Html.fromHtml(Html.fromHtml(
                        downloadStringFromUrl(
                                URLUtils.SEARCH_URL + params[0])).toString()).toString();
                if (!TextUtils.isEmpty(result) && !isCancelled()) {
                    JSONArray resultArray = new JSONArray(result);

                    if (resultArray.length() == 0) {
                        isResultNull = true;
                        return null;
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dateResultList.clear();
                                newsList.clear();

                                newsAdapter.notifyDataSetChanged();
                            }
                        });

                        for (int i = 0; i < resultArray.length(); i++) {
                            JSONObject newsObject = resultArray.getJSONObject(i);
                            String date = newsObject.getString("date");
                            final Calendar calendar = Calendar.getInstance();
                            calendar.setTime(DateUtils.simpleDateFormat.parse(date));
                            calendar.add(Calendar.DAY_OF_YEAR, -1);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dateResultList.add(simpleDateFormat.format(calendar.getTime()));
                                }
                            });

                            final DailyNews news = gson.fromJson(newsObject.getString("content"), newsType);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    newsList.add(news);
                                }
                            });
                        }
                    }
                } else {
                    isSearchSuccess = false;
                }
            } catch (IOException e) {
                isSearchSuccess = false;
                return null;
            } catch (JSONException e) {
                isSearchSuccess = false;
                return null;
            } catch (ParseException ignored) {
                isSearchSuccess = false;
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            if (isSearchSuccess && !isCancelled()) {
                newsAdapter.notifyDataSetChanged();
            } else {
                Crouton.makeText(SearchActivity.this,
                        getString(R.string.network_error),
                        Style.ALERT).show();
            }

            if (isResultNull && !isCancelled()) {
                Crouton.makeText(SearchActivity.this,
                        getString(R.string.no_result_found),
                        Style.ALERT).show();
            }

            searchView.clearFocus();
        }
    }
}
