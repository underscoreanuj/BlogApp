package com.example.anuj.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by anuj on 29/4/18.
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Login Activity TAG : ";
    private EditText mEmail;
    private EditText mPass;

    private TextView createNewBtn;

    private Button signInBtn;
    private SignInButton googleBtn;

    FirebaseAuth mAuth;
    DatabaseReference mDatabaseUsers;

    ProgressDialog mProgress;

    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseUsers.keepSynced(true);

        mProgress = new ProgressDialog(this);

        mEmail = findViewById(R.id.signin_email_field);
        mPass = findViewById(R.id.singin_pass_field);

        createNewBtn = findViewById(R.id.create_new_btn);
        createNewBtn.setText(Html.fromHtml("<u>Create new User</u>"));

        signInBtn = findViewById(R.id.signin_btn);
        googleBtn = findViewById(R.id.google_signin_button);

        createNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmail.getText().toString();
                String pass = mPass.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {

                    mProgress.setMessage("Signing In");
                    mProgress.show();

                    mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                final String user_id = mAuth.getCurrentUser().getUid();

                                mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //if (dataSnapshot.hasChild(user_id)) {
                                            mProgress.dismiss();
                                            Toast.makeText(LoginActivity.this, "Signed In", Toast.LENGTH_SHORT).show();
                                            //startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(mainIntent);
                                            /*
                                        }
                                        else {
                                            mProgress.dismiss();
                                            Toast.makeText(LoginActivity.this, "Account setup required!!!", Toast.LENGTH_SHORT).show();
                                            Intent addInfoIntent = new Intent(LoginActivity.this, AccountSetup.class);
                                            addInfoIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(addInfoIntent);
                                        }
                                            */
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                            else {
                                Toast.makeText(LoginActivity.this, "Error Logging-In", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

        //------------------------------------------------------------------------------------------------------

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        mProgress.setMessage("Signing In");
        mProgress.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            mProgress.dismiss();

                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(mainIntent);

                        } else {

                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();

                            mProgress.dismiss();

                        }

                    }
                });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {

                Log.w(TAG, "Google sign in failed", e);

            }
        }


    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
            else {
                if (response == null) {
                    Toast.makeText(this, "Sign-In Failed!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No Network!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, "Unknown Error!!!", Toast.LENGTH_SHORT).show();
            }
        }

    }
    */
}
