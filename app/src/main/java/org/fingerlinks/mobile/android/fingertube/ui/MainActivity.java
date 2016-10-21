package org.fingerlinks.mobile.android.fingertube.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tumblr.remember.Remember;

import org.fingerlinks.mobile.android.fingertube.R;
import org.fingerlinks.mobile.android.fingertube.Utils;
import org.fingerlinks.mobile.android.fingertube.model.CommentModel;
import org.fingerlinks.mobile.android.fingertube.model.TvModel;
import org.fingerlinks.mobile.android.fingertube.model.UserModel;
import org.fingerlinks.mobile.android.fingertube.model.VideoModel;
import org.fingerlinks.mobile.android.fingertube.viewholder.CommentViewHolder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private Toolbar toolbar;
    private Button addTvCode;
    private FloatingActionButton newComment;
    private RecyclerView commentList;
    private ImageView videoImage;
    private TextView videoTitle;

    private LinearLayout addTvContainer;
    private RelativeLayout commentContainer;
    private LinearLayout openVideoInTv;

    private MaterialDialog progressDialog;
    private DatabaseReference databaseReference;
    private Query myTvQUery;

    private FirebaseRecyclerAdapter<CommentModel, CommentViewHolder> firebaseRecyclerAdapter;
    private MaterialDialog commentDialog;
    private TvValueEventListener tvValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        addTvCode = (Button) findViewById(R.id.add_tv_code);
        newComment = (FloatingActionButton) findViewById(R.id.new_comment);
        addTvContainer = (LinearLayout) findViewById(R.id.add_tv_container);
        commentList = (RecyclerView) findViewById(R.id.comment_list);
        commentContainer = (RelativeLayout) findViewById(R.id.comment_container);
        openVideoInTv = (LinearLayout) findViewById(R.id.open_video_in_tv);

        videoTitle = (TextView) findViewById(R.id.video_title);
        videoImage = (ImageView) findViewById(R.id.video_image);

        commentList.setNestedScrollingEnabled(false);
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        commentList.setLayoutManager(manager);

        newComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String id = (String) view.getTag();
                if (!id.isEmpty()) {
                    commentDialog = new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.new_comment)
                            .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
                            .inputRange(0, 1000)
                            .negativeText(R.string.cancel)
                            .positiveText(R.string.ok)
                            .input(getString(R.string.new_comment_hint), "", false, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull final MaterialDialog dialog, CharSequence input) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    CommentModel model = new CommentModel(user.getUid(), user.getDisplayName(), user.getEmail(), System.currentTimeMillis(), input.toString());
                                    databaseReference.child("comment_list").child(id).push().setValue(model);
                                }
                            }).show();
                }
            }
        });

        toolbar.inflateMenu(R.menu.activity_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.logout:
                        new MaterialDialog.Builder(MainActivity.this)
                                .title(R.string.logout)
                                .content(R.string.logout_msg)
                                .positiveText(R.string.ok)
                                .negativeText(R.string.cancel)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        AuthUI.getInstance().signOut(MainActivity.this)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Remember.clear(new Remember.Callback() {
                                                                @Override
                                                                public void apply(Boolean success) {
                                                                    restartApp();
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                    }
                                })
                                .show();
                        break;
                }
                return false;
            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            progressDialog = new MaterialDialog.Builder(MainActivity.this)
                    .content(R.string.loading)
                    .progress(true, 0)
                    .cancelable(false)
                    .build();
            progressDialog.show();

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(
                                    AuthUI.EMAIL_PROVIDER)
                            .setTheme(R.style.AppTheme_Auth)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_SIGN_IN);
        } else {
            //user is logged, check if tv is connected
            if (!Remember.getString("tv_id", "").isEmpty()) {
                //tv is connected, show correct view and comments
                showCurrentView();
            } else {
                //no tv connect show dialog to connect
                addTvContainer.setVisibility(View.VISIBLE);
                commentContainer.setVisibility(View.GONE);
                openVideoInTv.setVisibility(View.GONE);

                addTvCode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAddTvDialog();
                    }
                });
            }
        }

    }

    private void showAddTvDialog() {
        new MaterialDialog.Builder(MainActivity.this)
                .title(R.string.inserisci_codice)
                .content(R.string.inserisci_codice_msg)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
                .inputRange(6, 6)
                .negativeText(R.string.cancel)
                .positiveText(R.string.ok)
                .input("HA23M2", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull final MaterialDialog dialog, CharSequence input) {
                        progressDialog = new MaterialDialog.Builder(MainActivity.this)
                                .content(R.string.loading)
                                .progress(true, 0)
                                .cancelable(false)
                                .build();
                        progressDialog.show();
                        setTvId(input.toString().toLowerCase());
                    }
                }).show();
    }

    private void setTvId(final String id) {
        final String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = databaseReference.child("user_list").child(key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Query query = databaseReference.child("tv_list").child(id);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {
                            if (dataSnapshot1.exists()) {
                                Log.d(TAG, "dataSnapshot.exists()");
                                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                                userModel.tv_id = id;
                                TvModel tvModel = dataSnapshot1.getValue(TvModel.class);
                                tvModel.user_id = key;
                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("/tv_list/" + id, tvModel.toMap());
                                childUpdates.put("/user_list/" + key, userModel.toMap());
                                databaseReference.updateChildren(childUpdates).addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "onComplete");
                                        Remember.putString("tv_id", id);
                                        progressDialog.dismiss();
                                        showCurrentView();
                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title(R.string.code_error_tite)
                                        .content(R.string.code_error_msg)
                                        .positiveText(R.string.ok)
                                        .show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            progressDialog.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                //finish login check if user id is present in user_list in database
                final String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Query query = databaseReference.child("user_list").child(key);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onDataChange");
                        if (dataSnapshot.exists()) {
                            UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            if (!userModel.tv_id.isEmpty()) {
                                Remember.putString("tv_id", userModel.tv_id);
                            }
                            Log.d(TAG, "dataSnapshot.exists()");
                            restartApp();
                        } else {
                            UserModel user = new UserModel("", FirebaseAuth.getInstance().getCurrentUser().getEmail(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                            databaseReference.child("user_list").child(key).setValue(user).addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        restartApp();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled");
                        progressDialog.dismiss();
                        new MaterialDialog.Builder(MainActivity.this)
                                .title(databaseError.getMessage())
                                .content(databaseError.getDetails())
                                .cancelable(false)
                                .positiveText(R.string.ok)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        AuthUI.getInstance().signOut(MainActivity.this)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            restartApp();
                                                        }
                                                    }
                                                });
                                    }
                                })
                                .show();
                    }
                });


            } else {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showCurrentView() {
        myTvQUery = databaseReference.child("tv_list").child(Remember.getString("tv_id", ""));
        tvValueEventListener = new TvValueEventListener();
        myTvQUery.addValueEventListener(tvValueEventListener);

    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private class TvValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                TvModel tvModel = dataSnapshot.getValue(TvModel.class);
                Log.d(TAG, "tvModel.video_id [" + tvModel.video_id + "]");
                String videoId = tvModel.video_id;
                if (videoId.isEmpty()) {
                    if (commentDialog != null && commentDialog.isShowing()) commentDialog.dismiss();
                    newComment.setTag("");
                    addTvContainer.setVisibility(View.GONE);
                    commentContainer.setVisibility(View.GONE);
                    openVideoInTv.setVisibility(View.VISIBLE);
                } else {
                    if (commentDialog != null && commentDialog.isShowing()) commentDialog.dismiss();
                    newComment.setTag(videoId);
                    addTvContainer.setVisibility(View.GONE);
                    commentContainer.setVisibility(View.VISIBLE);
                    openVideoInTv.setVisibility(View.GONE);
                    Query queryVideo = databaseReference.child("video_list").child(videoId);
                    queryVideo.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                VideoModel videoModel = dataSnapshot.getValue(VideoModel.class);
                                videoTitle.setText(videoModel.title);
                                Glide.with(MainActivity.this)
                                        .load(videoModel.image)
                                        .asBitmap()
                                        .error(R.drawable.ic_banner)
                                        .placeholder(R.drawable.ic_banner)
                                        .into(videoImage);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    Query query = databaseReference.child("comment_list").child(videoId);
                    firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<CommentModel, CommentViewHolder>(CommentModel.class, R.layout.row_comment, CommentViewHolder.class, query) {
                        @Override
                        protected void populateViewHolder(CommentViewHolder viewHolder, CommentModel model, int position) {
                            Glide.with(MainActivity.this)
                                    .load(Utils.gravatarUrl(model.user_email))
                                    .asBitmap()
                                    .into(viewHolder.profile);
                            viewHolder.name.setText(model.user_display_name);
                            viewHolder.email.setText(model.user_email);
                            viewHolder.data.setText(Utils.dataRelativa(MainActivity.this, new Date(model.timestamp)));
                            viewHolder.comment.setText(model.comment);
                        }
                    };
                    commentList.setAdapter(firebaseRecyclerAdapter);
                }
            } else {
                Log.d(TAG, "tvModel removed");
                if (commentDialog != null && commentDialog.isShowing()) commentDialog.dismiss();
                Remember.remove("tv_id");
                myTvQUery.removeEventListener(tvValueEventListener);
                addTvContainer.setVisibility(View.VISIBLE);
                commentContainer.setVisibility(View.GONE);
                openVideoInTv.setVisibility(View.GONE);
                addTvCode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAddTvDialog();
                    }
                });
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

}
