package io.github.izzyleung.zhihudailypurify.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.util.CommonUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import taobe.tec.jcc.JChineseConvertor;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public final class NewsAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    private LayoutInflater mInflater;

    private List<DailyNews> newsList;
    private List<String> dateResultList;

    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.noimage)
            .showImageOnFail(R.drawable.noimage)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .considerExifParams(true)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

    private JChineseConvertor convertor;
    private boolean canConvert = true;
    private boolean shouldConvert = Locale.getDefault().equals(Locale.TRADITIONAL_CHINESE);

    public NewsAdapter(Context context, List<DailyNews> newsList) {
        this(context, newsList, null);
    }

    public NewsAdapter(Context context, List<DailyNews> newsList,
                       List<String> dateResultList) {
        this.mInflater = LayoutInflater.from(context);
        this.newsList = newsList;
        this.dateResultList = dateResultList;

        try {
            convertor = JChineseConvertor.getInstance();
        } catch (IOException e) {
            canConvert = false;
        }
    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CardViewHolder holder;
        if (convertView == null) {
            holder = new CardViewHolder();
            convertView = mInflater.inflate(R.layout.news_list_item,
                    null);

            assert convertView != null;
            holder.newsImage = (ImageView)
                    convertView.findViewById(R.id.thumbnail_image);
            holder.dailyTitle = (TextView)
                    convertView.findViewById(R.id.daily_title);
            holder.questionTitle = (TextView)
                    convertView.findViewById(R.id.question_title);

            convertView.setTag(holder);
        } else {
            holder = (CardViewHolder) convertView.getTag();
        }

        DailyNews dailyNews = new DailyNews(newsList.get(position));

        imageLoader.displayImage(dailyNews.getThumbnailUrl(),
                holder.newsImage,
                options,
                animateFirstListener);

        if (shouldConvert && canConvert) {
            if (dailyNews.isMulti()) {
                holder.questionTitle.setText(convertor.s2t(dailyNews.getDailyTitle()));
                holder.dailyTitle.setText(CommonUtils.traditionalMultiQuestion);
            } else {
                holder.questionTitle.setText(convertor.s2t(dailyNews.getQuestionTitle()));
                holder.dailyTitle.setText(convertor.s2t(dailyNews.getDailyTitle()));
            }
        } else {
            if (dailyNews.isMulti()) {
                holder.questionTitle.setText(dailyNews.getDailyTitle());
                holder.dailyTitle.setText(CommonUtils.simplifiedMultiQuestion);
            } else {
                holder.questionTitle.setText(dailyNews.getQuestionTitle());
                holder.dailyTitle.setText(dailyNews.getDailyTitle());
            }
        }

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.search_result_list_header,
                    null);

            assert convertView != null;
            holder.headerTitle = (TextView)
                    convertView.findViewById(R.id.header_title);

            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        holder.headerTitle.setText(dateResultList.get(position));
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return dateResultList.get(position).hashCode();
    }

    private final static class CardViewHolder {
        public ImageView newsImage;
        public TextView questionTitle;
        public TextView dailyTitle;
    }

    private final static class HeaderViewHolder {
        public TextView headerTitle;
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }
}