package com.example.anuj.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class AccountSetup extends AppCompatActivity {

    private ImageButton profileBtn;
    private EditText profileName;
    private Button saveBtn;

    private Uri uri = null;

    private StorageReference mStorage;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;

    private ProgressDialog mProgress;

    private static final int GALLERY_REQUEST_CODE = 1122;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_account);

        profileBtn = findViewById(R.id.setup_image_btn);
        profileName = findViewById(R.id.setup_name_txt);
        saveBtn = findViewById(R.id.setup_save_btn);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();

        mProgress = new ProgressDialog(this);

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user_name = profileName.getText().toString().trim();

                if (!TextUtils.isEmpty(user_name)) {


                    String rand_name = user_name + UUID.randomUUID().toString();

                    StorageReference path = mStorage.child("profile_images").child(rand_name);

                    mProgress.setMessage("Setting up your account");
                    mProgress.show();

                    path.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Uri imageUrl = taskSnapshot.getDownloadUrl();
                            final String user_id = mAuth.getCurrentUser().getUid();

                            DatabaseReference curr_user_db = mDatabaseUsers.child(user_id);

                            curr_user_db.child("name").setValue(user_name);
                            if (uri == null) {
                                curr_user_db.child("profile_image").setValue("default");
                            }
                            else {
                                curr_user_db.child("profile_image").setValue(imageUrl.toString());
                            }

                            mProgress.dismiss();
                            Toast.makeText(AccountSetup.this, "Account setup successful", Toast.LENGTH_SHORT).show();

                            finish();
                            //startActivity(new Intent(AccountSetup.this, MainActivity.class));

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgress.dismiss();
                            Toast.makeText(AccountSetup.this, "Account setup failed!!!", Toast.LENGTH_SHORT).show();
                        }
                    });


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
//Image cropping kara lena ---- arthur (art of android) github pe hai
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            uri = data.getData();
            profileBtn.setImageURI(uri);
            profileBtn.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

    }
}
