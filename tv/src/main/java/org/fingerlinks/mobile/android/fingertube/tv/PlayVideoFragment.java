package org.fingerlinks.mobile.android.fingertube.tv;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.PlaybackOverlayFragment;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.fingerlinks.mobile.android.fingertube.tv.model.VideoModel;

import java.util.HashMap;

/**
 * Created by raphaelbussa on 13/10/16.
 */

public class PlayVideoFragment extends PlaybackOverlayFragment {

    private final static String TAG = PlayVideoFragment.class.getName();

    private VideoModel videoModel;

    private ArrayObjectAdapter rowsAdapter;
    private ArrayObjectAdapter primaryActionsAdapter;
    private PlaybackControlsRow playbackControlsRow;

    private PlaybackControlsRow.PlayPauseAction playPauseAction;
    private OnPlayPauseClickedListener playPauseClickedListener;

    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoModel = (VideoModel) getActivity().getIntent().getSerializableExtra("VIDEO");

        handler = new Handler();

        setBackgroundType(PlaybackOverlayFragment.BG_LIGHT);
        setFadingEnabled(false);
        setupRows();

        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemSelected: " + item + " row " + row);
            }
        });
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemClicked: " + item + " row " + row);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //playPauseClickedListener.onFragmentPlayPause(videoModel, 0, true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlayPauseClickedListener) {
            playPauseClickedListener = (OnPlayPauseClickedListener) context;
        }
    }

    private void setupRows() {
        ClassPresenterSelector classPresenterSelector = new ClassPresenterSelector();
        PlaybackControlsRowPresenter playbackControlsRowPresenter = new PlaybackControlsRowPresenter(new DescriptionPresenter());
        playbackControlsRowPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == playPauseAction.getId()) {
                    boolean isPlaying = playPauseAction.getIcon() == playPauseAction.getDrawable(PlayPauseAction.PLAY);
                    Log.d(TAG, "isPlaying [" + isPlaying + "]");
                    togglePlayback(isPlaying);
                }
            }
        });

        classPresenterSelector.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        classPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());

        rowsAdapter = new ArrayObjectAdapter(classPresenterSelector);

        //
        playbackControlsRow = new PlaybackControlsRow(videoModel);
        rowsAdapter.add(playbackControlsRow);

        updateVideoImage(videoModel.image);
        rowsAdapter.notifyArrayItemRangeChanged(0, 1);
        playbackControlsRow.setTotalTime(getDuration());
        playbackControlsRow.setCurrentTime(0);
        playbackControlsRow.setBufferedProgress(0);


        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        primaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        playbackControlsRow.setPrimaryActionsAdapter(primaryActionsAdapter);

        playPauseAction = new PlaybackControlsRow.PlayPauseAction(getActivity());
        playPauseAction.setIcon(playPauseAction.getDrawable(PlayPauseAction.PLAY));
        primaryActionsAdapter.add(playPauseAction);

        setAdapter(rowsAdapter);
    }

    public void stopPlayback() {
        playbackControlsRow.setCurrentTime(0);
        togglePlayback(false);
    }

    public void togglePlayback(boolean playPause) {
        if (playPause) {
            startProgressAutomation();
            playPauseAction.setIcon(playPauseAction.getDrawable(PlayPauseAction.PAUSE));
        } else {
            stopProgressAutomation();
            playPauseAction.setIcon(playPauseAction.getDrawable(PlayPauseAction.PLAY));
        }
        setFadingEnabled(playPause);
        playPauseClickedListener.onFragmentPlayPause(videoModel, playbackControlsRow.getCurrentTime(), playPause);
        notifyChanged(playPauseAction);
    }

    private void notifyChanged(Action action) {
        if (primaryActionsAdapter.indexOf(action) >= 0) {
            primaryActionsAdapter.notifyArrayItemRangeChanged(primaryActionsAdapter.indexOf(action), 1);
        }
    }

    private int getDuration() {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(videoModel.url, new HashMap<String, String>());
        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = Long.parseLong(time);
        return (int) duration;
    }

    private void updateVideoImage(String uri) {
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .into(new SimpleTarget<GlideDrawable>(200, 240) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        playbackControlsRow.setImageDrawable(resource);
                        rowsAdapter.notifyArrayItemRangeChanged(0, rowsAdapter.size());
                    }
                });
    }

    private void startProgressAutomation() {
        runnable = new Runnable() {
            @Override
            public void run() {
                PlayVideoActivity playVideoActivity = ((PlayVideoActivity) getActivity());
                if (playVideoActivity == null) {
                    stopProgressAutomation();
                    return;
                }
                VideoView videoView = ((PlayVideoActivity) getActivity()).getVideoView();
                if (videoView == null) {
                    stopProgressAutomation();
                    return;
                }
                int currentTime = videoView.getCurrentPosition();
                int totalTime = videoView.getDuration();
                playbackControlsRow.setCurrentTime(currentTime);
                playbackControlsRow.setTotalTime(totalTime);
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 500);
    }

    private void stopProgressAutomation() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    public interface OnPlayPauseClickedListener {
        void onFragmentPlayPause(VideoModel movie, int position, Boolean playPause);
    }

    static class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {
        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {
            viewHolder.getTitle().setText(((VideoModel) item).title);
            viewHolder.getSubtitle().setText(((VideoModel) item).url);
        }
    }

}
