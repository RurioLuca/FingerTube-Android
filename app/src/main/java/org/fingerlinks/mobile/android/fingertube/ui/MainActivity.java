package org.fingerlinks.mobile.android.fingertube.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tumblr.remember.Remember;

import org.fingerlinks.mobile.android.fingertube.R;
import org.fingerlinks.mobile.android.fingertube.model.TvModel;
import org.fingerlinks.mobile.android.fingertube.model.UserModel;

import java.util.HashMap;
import java.util.Map;

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private Toolbar toolbar;
    private Button addTvCode;
    private LinearLayout addTvContainer;

    private MaterialDialog progressDialog;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        addTvCode = (Button) findViewById(R.id.add_tv_code);
        addTvContainer = (LinearLayout) findViewById(R.id.add_tv_container);

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
                                                            restartApp();
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
            if (Remember.getBoolean("tv_connected", false)) {

            } else {
                //no tv connect show dialog to connect
                addTvCode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new MaterialDialog.Builder(MainActivity.this)
                                .title(R.string.inserisci_codice)
                                .content(R.string.inserisci_codice_msg)
                                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
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
                });
            }
        }

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
                            if (dataSnapshot.exists()) {
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
                                        Remember.putBoolean("tv_connected", true);
                                        progressDialog.dismiss();
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

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

}
