package org.fingerlinks.mobile.android.fingertube.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tumblr.remember.Remember;

import org.fingerlinks.mobile.android.fingertube.tv.model.TvModel;
import org.fingerlinks.mobile.android.fingertube.tv.model.VideoModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by raphaelbussa on 13/10/16.
 */

public class PlayVideoActivity extends Activity implements PlayVideoFragment.OnPlayPauseClickedListener {

    private static final String TAG = PlayVideoActivity.class.getName();

    private VideoView videoView;
    private MediaSession mediaSession;
    private PlaybackState playbackState;

    private String videoUrl = "";
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        String videoKey = ((VideoModel) getIntent().getSerializableExtra("VIDEO")).key;
        setVideoId(videoKey);

        videoView = (VideoView) findViewById(R.id.video_view);
        videoView.setFocusable(false);
        videoView.setFocusableInTouchMode(false);

        setupCallbacks();

        mediaSession = new MediaSession(this, getString(R.string.app_name));
        mediaSession.setCallback(new PlayVideoActivity.MediaSessionCallback());
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);

    }

    private void setupCallbacks() {
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                videoView.stopPlayback();
                playbackState = PlaybackState.IDLE;
                return false;
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (playbackState == PlaybackState.PLAYING) {
                    videoView.start();
                }
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playbackState = PlaybackState.IDLE;
                PlayVideoFragment playbackOverlayFragment = (PlayVideoFragment) getFragmentManager().findFragmentById(R.id.play_video_fragment);
                if (playbackOverlayFragment == null) return;
                playbackOverlayFragment.stopPlayback();
                Log.d(TAG, "onCompletion");
            }
        });
    }

    public VideoView getVideoView() {
        return videoView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setVideoId("");
        videoView.suspend();
    }

    @Override
    public void onResume() {
        super.onResume();
        mediaSession.setActive(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            if (!requestVisibleBehind(true)) {
                if (videoView != null) {
                    videoView.stopPlayback();
                }
            }
        } else {
            requestVisibleBehind(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaSession.release();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        PlayVideoFragment playbackOverlayFragment = (PlayVideoFragment) getFragmentManager().findFragmentById(R.id.play_video_fragment);
        if (playbackOverlayFragment == null) return super.onKeyUp(keyCode, event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                playbackOverlayFragment.togglePlayback(false);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                playbackOverlayFragment.togglePlayback(false);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                playbackOverlayFragment.togglePlayback(playbackState != PlaybackState.PLAYING);
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void updatePlaybackState(int position) {
        android.media.session.PlaybackState.Builder stateBuilder = new android.media.session.PlaybackState.Builder().setActions(android.media.session.PlaybackState.ACTION_PAUSE | android.media.session.PlaybackState.ACTION_PLAY);
        int state = android.media.session.PlaybackState.STATE_PLAYING;
        if (playbackState == PlaybackState.PAUSED) {
            state = android.media.session.PlaybackState.STATE_PAUSED;
        }
        stateBuilder.setState(state, position, 1.0f);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void updateMetadata(VideoModel movie) {
        final MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, movie.title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, movie.title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, movie.image);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, movie.title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, movie.url);
        Glide.with(PlayVideoActivity.this)
                .load(movie.image)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(500, 500) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
                        mediaSession.setMetadata(metadataBuilder.build());
                    }
                });
    }

    @Override
    public void onFragmentPlayPause(VideoModel movie, int position, Boolean playPause) {
        if (videoUrl.isEmpty()) {
            videoUrl = movie.url;
            videoView.setVideoPath(videoUrl);
        }
        if (position == 0 || playbackState == PlaybackState.IDLE) {
            setupCallbacks();
            playbackState = PlaybackState.IDLE;
        }

        if (playPause && playbackState != PlaybackState.PLAYING) {
            playbackState = PlaybackState.PLAYING;
            if (position >= 0) {
                videoView.seekTo(position);
                videoView.start();
            }
        } else {
            playbackState = PlaybackState.PAUSED;
            videoView.pause();
        }
        updatePlaybackState(position);
        updateMetadata(movie);
    }

    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
    }

    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }

    private class MediaSessionCallback extends MediaSession.Callback {

    }

    private void setVideoId(final String id) {
        Query query = databaseReference.child("tv_list").child(Remember.getString("tv_id", ""));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    TvModel dataSnapshotValue = dataSnapshot.getValue(TvModel.class);
                    dataSnapshotValue.video_id = id;
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/tv_list/" + Remember.getString("tv_id", ""), dataSnapshotValue.toMap());
                    databaseReference.updateChildren(childUpdates);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
