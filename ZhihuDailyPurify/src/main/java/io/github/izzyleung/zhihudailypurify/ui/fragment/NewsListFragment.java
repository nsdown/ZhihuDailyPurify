package io.github.izzyleung.zhihudailypurify.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.adapter.NewsAdapter;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.lib.MyAsyncTask;
import io.github.izzyleung.zhihudailypurify.support.util.CommonUtils;
import io.github.izzyleung.zhihudailypurify.support.util.NetworkUtils;
import io.github.izzyleung.zhihudailypurify.support.util.URLUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewsListFragment extends ListFragment implements OnRefreshListener {
    private String date;

    private boolean isAutoRefresh;
    private boolean isFirstPage;
    private boolean isSingle;
    private boolean isRefreshed = false;
    private boolean isCached = false;
    private boolean isRecovered = false;

    private List<DailyNews> newsList = new ArrayList<DailyNews>();
    private BaseAdapter listAdapter;
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            date = bundle.getString("date");
            isFirstPage = bundle.getBoolean("first_page?");
            isSingle = bundle.getBoolean("single?");

            if (!isSingle) {
                setRetainInstance(true);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.fragment_news_list, null);
        assert view != null;
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setOnScrollListener(
                new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewGroup viewGroup = (ViewGroup) view;
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
        ActionBarPullToRefresh.from(getActivity())
                .insertLayoutInto(viewGroup)
                .theseChildrenArePullable(getListView(), getListView().getEmptyView())
                .listener(this)
                .setup(mPullToRefreshLayout);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!isRecovered) {
            new FileToAdapterTask().
                    executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(getActivity());
        isAutoRefresh = pref.getBoolean("auto_refresh?", true);

        boolean isShowcase = pref.getBoolean("show_showcase?", true);

        if (isFirstPage || isSingle) {
            if (isAutoRefresh && !isRefreshed) {
                if (!isShowcase) {
                    refresh();
                }
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            if (isAutoRefresh && !isRefreshed) {
                refresh();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Crouton.cancelAllCroutons();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mPullToRefreshLayout.getHeaderTransformer().
                onConfigurationChanged(getActivity(), newConfig);
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        CommonUtils.listOnClick(getActivity(), newsList.get(position));
    }

    @Override
    public void onRefreshStarted(View view) {
        refresh();
    }

    public void refresh() {
        if (isFirstPage) {
            new OriginalGetNewsTask().execute();
        } else {
            if (getActivity() != null) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getActivity());

                if (sharedPreferences.getBoolean("using_accelerate_server?", false)) {
                    if (Integer.parseInt(sharedPreferences.getString("which_accelerate_server", "1")) == 1) {
                        new AccelerateGetNewsTask().execute(1);
                    } else {
                        new AccelerateGetNewsTask().execute(2);
                    }
                } else {
                    new OriginalGetNewsTask().execute();
                }
            }
        }
    }

    final class FileToAdapterTask extends MyAsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            fileToBeans();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            isRecovered = true;
            listAdapter = new NewsAdapter(
                    getActivity(),
                    newsList);
            setListAdapter(listAdapter);
        }

        private void fileToBeans() {
            if (NewsListFragment.this.isAdded()) {
                if (getActivity() != null) {
                    File file;
                    try {
                        file = new File(getActivity().getFilesDir(), date);
                    } catch (NullPointerException e) {
                        return;
                    }

                    FileInputStream fis;
                    BufferedReader reader;
                    String line;

                    Type listType = new TypeToken<List<DailyNews>>() {

                    }.getType();

                    Gson gson = new GsonBuilder().create();
                    StringBuilder sb = new StringBuilder();
                    String beanListToJson;

                    try {
                        if (file.exists()) {
                            if (getActivity() != null) {
                                fis = getActivity().openFileInput(date);

                                reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                                while ((line = reader.readLine()) != null) {
                                    sb.append(line).append("\n");
                                }

                                beanListToJson = sb.toString();
                                newsList = gson.
                                        fromJson(beanListToJson, listType);
                                reader.close();
                                fis.close();
                                isCached = true;
                            }
                        }
                    } catch (IOException ignored) {

                    }
                }
            }
        }
    }

    final class ListToFileTask extends MyAsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            beansToFile(newsList);
            return null;
        }

        private void beansToFile(List<DailyNews> newsList) {
            if (NewsListFragment.this.isAdded()) {
                if (getActivity() != null) {
                    Type listType = new TypeToken<List<DailyNews>>() {

                    }.getType();

                    String beanListToJson = new GsonBuilder().create().toJson(newsList, listType);

                    //File name is the date
                    if (getActivity() != null) {
                        File file = new File(getActivity().getFilesDir(), date);
                        FileOutputStream fout;

                        if (file.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            file.delete();
                        }

                        if (getActivity() != null) {
                            try {
                                fout = getActivity().openFileOutput(date, Context.MODE_PRIVATE);
                                fout.write(beanListToJson.getBytes());
                                fout.flush();
                                fout.close();
                            } catch (IOException ignored) {

                            }
                        }
                    }
                }
            }
        }
    }

    private abstract class BaseGetNewsTask<Params, Progress, Result> extends MyAsyncTask<Params, Progress, Result> {
        protected boolean isRefreshSuccess = true;
        protected boolean isTheSameContent = true;

        @Override
        protected void onPreExecute() {
            mPullToRefreshLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(Result result) {
            if (isRefreshSuccess && !isTheSameContent) {
                new ListToFileTask().execute();
            }

            mPullToRefreshLayout.setRefreshComplete();
            isRefreshed = true;
        }

        protected void warning(Exception e) {
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Crouton.makeText(getActivity(),
                                getActivity().getString(R.string.network_error),
                                Style.ALERT).show();

                    }
                });

                Crashlytics.log(
                        Log.ERROR,
                        "Error from Warning",
                        Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private class OriginalGetNewsTask extends BaseGetNewsTask<Void, DailyNews, Void> {
        private int position = 0;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject contents = new JSONObject(
                        NetworkUtils.downloadStringFromUrl(URLUtils.ZHIHU_DAILY_BEFORE_URL + date));

                if (isFirstPage) {
                    checkDate(getActivity(), contents.getString("date"));
                }

                JSONArray newsArray = contents.getJSONArray("stories");
                for (int i = 0; i < newsArray.length(); i++) {
                    JSONObject singleNews = newsArray.getJSONObject(i);

                    DailyNews dailyNews = new DailyNews();
                    String thumbnailUrl
                            = singleNews.has("images")
                            ? (String) singleNews.getJSONArray("images").get(0)
                            : null;
                    dailyNews.setThumbnailUrl(thumbnailUrl);
                    dailyNews.setDailyTitle(singleNews.getString("title"));

                    if (!newsList.contains(dailyNews)) {
                        String dailyInfoJson = NetworkUtils.
                                downloadStringFromUrl(URLUtils.ZHIHU_DAILY_OFFLINE_NEWS_URL
                                        + singleNews.getString("id"));
                        JSONObject dailyInfoJsonObject = new JSONObject(dailyInfoJson);
                        String htmlBody = dailyInfoJsonObject.getString("body");
                        Document doc = Jsoup.parse(htmlBody);
                        Elements viewMoreElements = doc.getElementsByClass("view-more");

                        if (!viewMoreElements.isEmpty()) {
                            isTheSameContent = false;
                            boolean shouldPublish = true;
                            //This is multi-stories mode
                            if (viewMoreElements.size() > 1) {
                                dailyNews.setMulti(true);
                                Elements questionTitleElements =
                                        doc.getElementsByClass("question-title");

                                for (int j = 0; j < viewMoreElements.size(); j++) {
                                    if (questionTitleElements.get(j).text().length() == 0) {
                                        dailyNews.addQuestionTitle(singleNews.getString("title"));
                                    } else {
                                        dailyNews.addQuestionTitle(questionTitleElements.get(j).text());
                                    }

                                    Elements viewQuestionElement = viewMoreElements.get(j).
                                            select("a");

                                    if (viewQuestionElement.text().equals("查看知乎讨论")) {
                                        dailyNews.addQuestionUrl(viewQuestionElement.attr("href"));
                                    } else {
                                        shouldPublish = false;
                                        break;
                                    }
                                }
                                if (shouldPublish) {
                                    publishProgress(dailyNews);
                                }
                                continue;
                            }

                            //This is single-story mode
                            dailyNews.setMulti(false);

                            Elements viewQuestionElement = doc.getElementsByClass("view-more").
                                    select("a");
                            if (viewQuestionElement.text().equals("查看知乎讨论")) {
                                dailyNews.setQuestionUrl(viewQuestionElement.attr("href"));
                            } else {
                                shouldPublish = false;
                            }

                            //Question title is the same with daily title
                            if (doc.getElementsByClass("question-title").text().length() == 0) {
                                dailyNews.setQuestionTitle(singleNews.getString("title"));
                            } else {
                                dailyNews.setQuestionTitle(doc.
                                        getElementsByClass("question-title").text());
                            }

                            if (shouldPublish) {
                                publishProgress(dailyNews);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                isRefreshSuccess = false;
                warning(e);
            } catch (IOException e) {
                isRefreshSuccess = false;
                warning(e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(DailyNews... values) {
            if (!isCached) {
                newsList.add(values[0]);
            } else {
                newsList.add(position++, values[0]);
            }
            listAdapter.notifyDataSetChanged();
        }

        private void checkDate(Activity activity, String dateString) {
            if (activity != null) {
                SharedPreferences sharedPreferences = PreferenceManager.
                        getDefaultSharedPreferences(activity);
                String cachedDateString = sharedPreferences.getString("date", null);
                boolean isSameDay = cachedDateString == null
                        || dateString.equals(cachedDateString);

                if (!isSameDay) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newsList.clear();
                            listAdapter.notifyDataSetChanged();
                        }
                    });
                }

                sharedPreferences.edit().putString("date", dateString).commit();
            }
        }
    }

    private class AccelerateGetNewsTask extends BaseGetNewsTask<Integer, DailyNews, Void> {
        private List<DailyNews> tempNewsList;

        @Override
        protected Void doInBackground(Integer... integers) {
            int type = integers[0];

            Type listType = new TypeToken<List<DailyNews>>() {

            }.getType();

            String jsonFromWeb;
            try {
                if (type == 1) {
                    jsonFromWeb = NetworkUtils.downloadStringFromUrl(URLUtils.
                            ZHIHU_DAILY_PURIFY_SAE_BEFORE_URL + date);
                } else {
                    jsonFromWeb = NetworkUtils.downloadStringFromUrl(URLUtils.
                            ZHIHU_DAILY_PURIFY_HEROKU_BEFORE_URL + date);
                }
            } catch (IOException e) {
                isRefreshSuccess = false;
                warning(e);
                return null;
            }

            String newsListJSON = Html.fromHtml(
                    Html.fromHtml(jsonFromWeb).toString()).toString();

            if (!TextUtils.isEmpty(newsListJSON)) {
                try {
                    tempNewsList = new GsonBuilder().create().
                            fromJson(newsListJSON, listType);
                } catch (JsonSyntaxException e) {
                    isRefreshSuccess = false;
                    warning(e);
                }
            } else {
                isRefreshSuccess = false;
                warning(new IOException("Nothing from web"));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isRefreshSuccess && !newsList.equals(tempNewsList)) {
                isTheSameContent = false;
                newsList = tempNewsList;
                if (getActivity() != null && isAdded()) {
                    listAdapter = new NewsAdapter(
                            getActivity(),
                            newsList);
                    setListAdapter(listAdapter);
                }
            }

            super.onPostExecute(aVoid);
        }
    }
}