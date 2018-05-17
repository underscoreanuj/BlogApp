package com.example.anuj.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by anuj on 30/4/18.
 */

public class SignupActivity extends AppCompatActivity {


    private EditText name_field;
    private EditText email_field;
    private EditText pass_field;

    private TextView already_user;

    private Button signup_btn;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");

        mProgress = new ProgressDialog(this);

        name_field = findViewById(R.id.name_field);
        email_field = findViewById(R.id.email_field);
        pass_field = findViewById(R.id.pass_field);

        already_user = findViewById(R.id.already_user_btn);
        already_user.setText(Html.fromHtml("<u>Already a User</u>"));

        signup_btn = findViewById(R.id.signup_btn);

        already_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String name = name_field.getText().toString().trim();
                String email = email_field.getText().toString().trim();
                String pass = pass_field.getText().toString().trim();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {

                    mProgress.setMessage("Signing-Up");
                    mProgress.show();

                    mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference curr_user_db = mDatabaseUsers.child(user_id);

                                curr_user_db.child("name").setValue(name);
                                curr_user_db.child("profile_image").setValue("default");

                                mProgress.dismiss();

                                //startActivity(new Intent(SignupActivity.this, MainActivity.class));

                                Intent mainIntent = new Intent(SignupActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(mainIntent);

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            mProgress.dismiss();
                        }
                    });

                }
                else {
                    Toast.makeText(SignupActivity.this, "Enter all Fields!!!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
