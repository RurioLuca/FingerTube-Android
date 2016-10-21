package org.fingerlinks.mobile.android.fingertube.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.fingerlinks.mobile.android.fingertube.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by raphaelbussa on 21/10/16.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView profile;
    public TextView name;
    public TextView email;
    public TextView data;
    public TextView comment;

    public CommentViewHolder(View itemView) {
        super(itemView);
        profile = (CircleImageView) itemView.findViewById(R.id.profile);
        name = (TextView) itemView.findViewById(R.id.name);
        email = (TextView) itemView.findViewById(R.id.email);
        data = (TextView) itemView.findViewById(R.id.data);
        comment = (TextView) itemView.findViewById(R.id.comment);
    }
}
