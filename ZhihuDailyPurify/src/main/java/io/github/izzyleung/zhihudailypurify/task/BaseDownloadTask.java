package io.github.izzyleung.zhihudailypurify.task;

import io.github.izzyleung.zhihudailypurify.support.lib.MyAsyncTask;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class BaseDownloadTask <Params, Progress, Result> extends MyAsyncTask<Params, Progress, Result> {
    protected String downloadStringFromUrl(String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        InputStream in = null;
        StringBuilder str = new StringBuilder();

        try {
            in = client.execute(request).getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;

            while ((line = reader.readLine()) != null) {
                str.append(line);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {

                }
            }
        }

        return str.toString();
    }
}
