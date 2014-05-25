package io.github.izzyleung.zhihudailypurify.task;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import io.github.izzyleung.zhihudailypurify.support.lib.MyAsyncTask;

import java.io.IOException;

public abstract class BaseDownloadTask<Params, Progress, Result> extends MyAsyncTask<Params, Progress, Result> {
    protected String downloadStringFromUrl(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        return client.newCall(request).execute().body().string();
    }
}
