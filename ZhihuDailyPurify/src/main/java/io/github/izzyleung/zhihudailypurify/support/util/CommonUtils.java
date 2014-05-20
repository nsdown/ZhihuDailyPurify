package io.github.izzyleung.zhihudailypurify.support.util;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import taobe.tec.jcc.JChineseConvertor;

import java.io.IOException;
import java.util.Locale;

public final class CommonUtils {
    public static final String simplifiedMultiQuestion = "这里包含多个知乎讨论，请点击后选择";
    public static final String traditionalMultiQuestion = "這裏包含多個知乎討論，請點擊後選擇";

    private CommonUtils() {

    }

    public static void listOnClick(final Context context, final DailyNews dailyNews) {
        //If the chosen news contains multi news, show an dialog for the user to choose one
        if (dailyNews.isMulti()) {
            String[] questionTitles = dailyNews.
                    getQuestionTitleList().
                    toArray(new String[dailyNews.getQuestionTitleList().size()]);

            if (Locale.getDefault().equals(Locale.TRADITIONAL_CHINESE)) {
                JChineseConvertor convertor = null;
                boolean canConvert = true;

                try {
                    convertor = JChineseConvertor.getInstance();
                } catch (IOException e) {
                    canConvert = false;
                }

                if (canConvert) {
                    for (int i = 0; i < questionTitles.length; i++) {
                        questionTitles[i] = convertor.s2t(questionTitles[i]);
                    }
                }
            }

            new AlertDialog.Builder(context).
                    setItems(questionTitles, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goToZhihu(context,
                                    dailyNews.getQuestionUrlList().get(which));
                        }
                    }).show();
        } else {
            //Or, just go to Zhihu
            goToZhihu(context,
                    dailyNews.getQuestionUrl());
        }
    }

    public static void goToZhihu(Context context, String url) {
        boolean isUsingClient = PreferenceManager.
                getDefaultSharedPreferences(context).getBoolean("using_client?", false);

        if (!isUsingClient) {
            openUsingBrowser(context, url);
        } else {
            //Open using Zhihu's official client
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                browserIntent.setPackage("com.zhihu.android");
                context.startActivity(browserIntent);
            } catch (ActivityNotFoundException e) {
                openUsingBrowser(context, url);
            }
        }
    }

    public static void openUsingBrowser(Context context, String url) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(browserIntent);
        } catch (ActivityNotFoundException ane) {
            Toast.makeText(context, context.getResources().getString(R.string.no_browser), Toast.LENGTH_SHORT).show();
        }
    }
}
