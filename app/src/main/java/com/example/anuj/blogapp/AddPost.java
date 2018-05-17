package com.example.anuj.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

/**
 * Created by anuj on 28/4/18.
 */

public class AddPost extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 1001;

    private ImageButton addImg;
    private EditText titleTxt;
    private EditText contTxt;

    private Button postBtn;

    private Uri uri = null;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    DatabaseReference mDatabaseUsers;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_add);

        addImg = findViewById(R.id.add_image_btn);
        titleTxt = findViewById(R.id.title_txt);
        contTxt = findViewById(R.id.content_txt);

        postBtn = findViewById(R.id.post_btn);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blogs");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users").child(mUser.getUid().toString());

        mProgress = new ProgressDialog(AddPost.this);

        addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String title_val = titleTxt.getText().toString().trim();
                final String cont_val = contTxt.getText().toString().trim();

                if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(cont_val) && uri != null) {

                    String rand_name = UUID.randomUUID().toString();

                    StorageReference path = mStorage.child("blog_images").child(rand_name);

                    mProgress.setMessage("Posting Blog");
                    mProgress.show();

                    path.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            final Uri imageUrl = taskSnapshot.getDownloadUrl();

                            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    DatabaseReference newBlog = mDatabase.push();

                                    newBlog.child("title").setValue(title_val);
                                    newBlog.child("content").setValue(cont_val);
                                    newBlog.child("image_url").setValue(imageUrl.toString());
                                    newBlog.child("uid").setValue(mUser.getUid().toString());
                                    newBlog.child("user_name").setValue(dataSnapshot.child("name").getValue().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mProgress.dismiss();
                                            Toast.makeText(AddPost.this, "Blog Posted", Toast.LENGTH_SHORT).show();

                                            finish();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            mProgress.dismiss();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgress.dismiss();
                            Toast.makeText(AddPost.this, "Upload Failed!!!", Toast.LENGTH_SHORT).show();
                            Toast.makeText(AddPost.this, "Try Again!!!", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                else {
                    Toast.makeText(AddPost.this, "Must add something to each field!!!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            uri = data.getData();
            addImg.setImageURI(uri);
            addImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

    }
}
