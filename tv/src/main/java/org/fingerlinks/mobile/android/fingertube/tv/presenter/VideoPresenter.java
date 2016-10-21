package org.fingerlinks.mobile.android.fingertube.tv.presenter;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.fingerlinks.mobile.android.fingertube.tv.R;
import org.fingerlinks.mobile.android.fingertube.tv.model.VideoModel;

/**
 * Created by raphaelbussa on 13/10/16.
 */

public class VideoPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.presenter_video, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        TextView text = (TextView) viewHolder.view.findViewById(R.id.text);
        ImageView image = (ImageView) viewHolder.view.findViewById(R.id.image);
        Context context = viewHolder.view.getContext();

        if (item instanceof VideoModel) {
            VideoModel model = (VideoModel) item;
            text.setText(model.title);
            Glide.with(context)
                    .load(model.image)
                    .into(image);
        }

    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageView image = (ImageView) viewHolder.view.findViewById(R.id.image);
        Glide.clear(image);
    }
}
