package org.fingerlinks.mobile.android.fingertube.tv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.support.v17.leanback.widget.VerticalGridPresenter;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.fingerlinks.mobile.android.fingertube.tv.firebase.FirebaseArray;
import org.fingerlinks.mobile.android.fingertube.tv.model.VideoModel;
import org.fingerlinks.mobile.android.fingertube.tv.presenter.VideoPresenter;

/**
 * Created by raphaelbussa on 13/10/16.
 */

public class AllVideoFragment extends VerticalGridFragment implements FirebaseArray.OnChangedListener, OnItemViewClickedListener {

    private SparseArrayObjectAdapter videoRowAdapter;
    private FirebaseArray videoArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoRowAdapter = new SparseArrayObjectAdapter(new VideoPresenter());
        setAdapter(videoRowAdapter);
        setTitle(getString(R.string.show_all));
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(5);
        setGridPresenter(gridPresenter);

        //load data from firebase db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child("video_list").orderByKey();
        videoArray = new FirebaseArray(query);
        videoArray.setOnChangedListener(this);

        setOnItemViewClickedListener(this);
    }

    @Override
    public void onChanged(EventType type, int index, int oldIndex) {
        VideoModel videoModel;
        switch (type) {
            case ADDED:
                videoModel = videoArray.getItem(index).getValue(VideoModel.class);
                videoModel.key = videoArray.getItem(index).getKey();
                videoRowAdapter.set(index, videoModel);
                videoRowAdapter.notifyArrayItemRangeChanged(0, videoArray.getCount() +1);
                break;
            case CHANGED:
                videoModel = videoArray.getItem(index).getValue(VideoModel.class);
                videoModel.key = videoArray.getItem(index).getKey();
                videoRowAdapter.set(index, videoModel);
                break;
            case REMOVED:
                videoRowAdapter.clear(index);
                break;
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        new AlertDialog.Builder(getActivity())
                .setTitle(databaseError.getMessage())
                .setMessage(databaseError.getDetails())
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof VideoModel) {
            VideoModel model = (VideoModel) item;
            //a video is clicked, play it
            Intent intent = new Intent(getActivity(), PlayVideoActivity.class);
            intent.putExtra("VIDEO", model);
            startActivity(intent);
        }
    }
}
