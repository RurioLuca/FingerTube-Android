package org.fingerlinks.mobile.android.fingertube.tv.presenter;

import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fingerlinks.mobile.android.fingertube.tv.R;

/**
 * Created by raphaelbussa on 26/07/16.
 */
public class ViewAllVideoPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.presenter_view_all_video, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        TextView text = (TextView) viewHolder.view.findViewById(R.id.text);
        text.setText((String) item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }

}
