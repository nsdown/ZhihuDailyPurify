package io.github.izzyleung.zhihudailypurify.application;

import android.app.Application;
import android.content.Context;
import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import io.github.izzyleung.zhihudailypurify.db.DailyNewsDataSource;

public final class ZhihuDailyPurifyApplication extends Application {
    private static ZhihuDailyPurifyApplication applicationContext;
    private DailyNewsDataSource dataSource;

    @Override
    public void onCreate() {
        super.onCreate();

        applicationContext = this;

        Crashlytics.start(this);
        initImageLoader(getApplicationContext());
        dataSource = new DailyNewsDataSource(getApplicationContext());
        dataSource.open();
    }

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();

        ImageLoader.getInstance().init(config);
    }

    public static ZhihuDailyPurifyApplication getInstance() {
        return applicationContext;
    }

    public DailyNewsDataSource getDataSource() {
        return dataSource;
    }
}
