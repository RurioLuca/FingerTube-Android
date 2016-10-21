package org.fingerlinks.mobile.android.fingertube.tv;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tumblr.remember.Remember;

import org.fingerlinks.mobile.android.fingertube.tv.firebase.FirebaseArray;
import org.fingerlinks.mobile.android.fingertube.tv.model.TvModel;
import org.fingerlinks.mobile.android.fingertube.tv.model.UserModel;
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
    private SparseArrayObjectAdapter videoRowAdapter;

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
        //setBadgeDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_logo));
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
                        if (!Remember.getBoolean("tv_match", false)) {
                            //generate tv item in tv_list
                            TvModel tvModel = new TvModel("", "Android TV", "");
                            databaseReference.child("tv_list").child(Remember.getString("tv_id", "")).setValue(tvModel).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(R.string.login)
                                                .setMessage(Utils.fromHtml(getString(R.string.login_msg, Remember.getString("tv_id", "").toUpperCase())))
                                                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //get current user id from my tv_id
                                                        Query query = databaseReference.child("tv_list").child(Remember.getString("tv_id", ""));
                                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                TvModel dataSnapshotValue = dataSnapshot.getValue(TvModel.class);
                                                                if (!dataSnapshotValue.user_id.isEmpty()) {
                                                                    //user correctly login save user id in shared preference
                                                                    Remember.putString("user_id", dataSnapshotValue.user_id);
                                                                    Remember.putBoolean("tv_match", true);
                                                                    Toast.makeText(getActivity(), R.string.login_success, Toast.LENGTH_LONG).show();
                                                                } else {
                                                                    //error matching tv in smart phone
                                                                    new AlertDialog.Builder(getActivity())
                                                                            .setTitle(R.string.error_login_title)
                                                                            .setMessage(R.string.error_login_msg)
                                                                            .setPositiveButton(R.string.ok, null)
                                                                            .show();
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
                                                        });
                                                    }
                                                })
                                                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .show();
                                    }
                                }
                            });
                        } else {
                            Query query = databaseReference.child("user_list").child(Remember.getString("user_id", ""));
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot dataSnapshot) {
                                    UserModel dataSnapshotValue = dataSnapshot.getValue(UserModel.class);
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle("Ciao, " + dataSnapshotValue.display_name)
                                            .setMessage(dataSnapshotValue.email)
                                            .setPositiveButton(R.string.un_link, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dataSnapshot.getRef().child("tv_id").setValue("");
                                                    databaseReference.child("tv_list").child(Remember.getString("", Remember.getString("tv_id", ""))).removeValue();
                                                    Remember.putString("tv_id", Utils.getLoginCode());
                                                    Remember.putBoolean("tv_match", false);
                                                }
                                            })
                                            .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            })
                                            .show();
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
                            });
                        }
                    }

                }
            }
        });

    }

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
                videoModel.key = videoArray.getItem(index).getKey();
                videoRowAdapter.set(index, videoModel);
                videoRowAdapter.notifyArrayItemRangeChanged(0, videoArray.getCount() + 1);
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