package org.fingerlinks.mobile.android.fingertube.tv.presenter;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.fingerlinks.mobile.android.fingertube.tv.R;
import org.fingerlinks.mobile.android.fingertube.tv.Utils;
import org.fingerlinks.mobile.android.fingertube.tv.model.CommentModel;

import java.util.Date;

/**
 * Created by raphaelbussa on 13/10/16.
 */

public class CommentPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.presenter_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ImageView image = (ImageView) viewHolder.view.findViewById(R.id.image);
        TextView name = (TextView) viewHolder.view.findViewById(R.id.name);
        TextView email = (TextView) viewHolder.view.findViewById(R.id.email);
        TextView data = (TextView) viewHolder.view.findViewById(R.id.data);
        TextView comment = (TextView) viewHolder.view.findViewById(R.id.comment);

        Context context = viewHolder.view.getContext();

        if (item instanceof CommentModel) {
            CommentModel model = (CommentModel) item;
            Glide.with(context)
                    .load(Utils.gravatarUrl(model.user_email))
                    .asBitmap()
                    .placeholder(R.drawable.ic_banner)
                    .error(R.drawable.ic_banner)
                    .into(image);
            name.setText(model.user_display_name);
            email.setText(model.user_email);
            data.setText(Utils.dataRelativa(context, new Date(model.timestamp)));
            comment.setText(model.comment);
        }

    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageView image = (ImageView) viewHolder.view.findViewById(R.id.image);
        Glide.clear(image);
    }
}
