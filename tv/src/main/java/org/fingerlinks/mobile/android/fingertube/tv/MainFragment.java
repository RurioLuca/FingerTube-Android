package org.fingerlinks.mobile.android.fingertube.tv;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.support.v4.content.ContextCompat;
import android.text.Html;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.tumblr.remember.Remember;

import org.fingerlinks.mobile.android.fingertube.tv.firebase.FirebaseArray;
import org.fingerlinks.mobile.android.fingertube.tv.model.VideoModel;
import org.fingerlinks.mobile.android.fingertube.tv.presenter.InfoPresenter;
import org.fingerlinks.mobile.android.fingertube.tv.presenter.VideoPresenter;
import org.fingerlinks.mobile.android.fingertube.tv.presenter.ViewAllVideoPresenter;

/**
 * Created by raphaelbussa on 13/10/16.
 */

public class MainFragment extends BrowseFragment implements FirebaseArray.OnChangedListener {

    private ArrayObjectAdapter categoryRowAdapter;

    private DatabaseReference databaseReference;

    private FirebaseArray videoArray;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        categoryRowAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        prepareEntranceTransition();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        setUpAdapter();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setTitle(getString(R.string.app_name));
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.primary_dark));
        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.accent));
        prepareEntranceTransition();
        setAdapter(categoryRowAdapter);

        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof VideoModel) {
                    VideoModel model = (VideoModel) item;

                    //a video is clicked, play it
                    Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
                    intent.putExtra("MOVIE", model);

                    startActivity(intent);
                    return;
                }
                if (item instanceof String) {
                    //a settings or other is selected, check correct item with a simple equals()
                    String value = (String) item;
                    if (value.equals(getString(R.string.show_all))) {
                        //show all section, open it
                        startActivity(new Intent(getActivity(), AllVideoActivity.class));
                        return;
                    }
                    if (value.equals(getString(R.string.account))) {

                        final String loginCode = Utils.getLoginCode();
                        Remember.putString("login_code", loginCode);

                        new AlertDialog.Builder(getActivity())
                                .setTitle("Login")
                                .setMessage(Utils.fromHtml(getString(R.string.login_msg, loginCode)))
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }

                }
            }
        });

    }

    private SparseArrayObjectAdapter videoRowAdapter;

    private void setUpAdapter() {
        categoryRowAdapter.clear();
        HeaderItem videoHeaderItem = new HeaderItem(getString(R.string.section_video));
        PresenterSelector presenterSelector = new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object item) {
                if (item instanceof VideoModel) {
                    return new VideoPresenter();
                }
                if (item instanceof String) {
                    return new ViewAllVideoPresenter();
                }
                return new VideoPresenter();
            }
        };
        videoRowAdapter = new SparseArrayObjectAdapter(presenterSelector);

        ListRow videoListRow = new ListRow(videoHeaderItem, videoRowAdapter);
        videoListRow.setId(100);

        categoryRowAdapter.add(videoListRow);

        HeaderItem info = new HeaderItem(getString(R.string.settings));
        InfoPresenter presenter = new InfoPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(presenter);
        gridRowAdapter.add(getString(R.string.account));
        gridRowAdapter.add(getString(R.string.info));
        ListRow row = new ListRow(info, gridRowAdapter);
        categoryRowAdapter.add(row);

        Query query = databaseReference.child("video_list").orderByKey().limitToFirst(3);
        videoArray = new FirebaseArray(query);
        videoArray.setOnChangedListener(this);
    }

    @Override
    public void onChanged(EventType type, int index, int oldIndex) {
        VideoModel videoModel;
        switch (type) {
            case ADDED:
                videoModel = videoArray.getItem(index).getValue(VideoModel.class);
                videoRowAdapter.set(index, videoModel);
                videoRowAdapter.notifyArrayItemRangeChanged(0, videoArray.getCount() +1);
                break;
            case CHANGED:
                videoModel = videoArray.getItem(index).getValue(VideoModel.class);
                videoRowAdapter.set(index, videoModel);
                break;
            case REMOVED:
                videoRowAdapter.clear(index);
                break;
        }
        if (videoRowAdapter.lookup(100) == null) {
            videoRowAdapter.set(100, getString(R.string.show_all));
            startEntranceTransition();
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

}