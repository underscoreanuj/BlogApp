package com.example.anuj.blogapp;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.SingleRefDataBufferIterator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class PostView extends AppCompatActivity {

    private String post_key = null;

    private DatabaseReference mDatabase;

    private ImageView singleBlogImage;
    private TextView singleBlogTitle;
    private TextView singleBlogContent;
    private ImageButton removeBlogBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_post);

        post_key = getIntent().getExtras().getString("post_key");

        //Toast.makeText(this, post_key, Toast.LENGTH_SHORT).show();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blogs");
        mDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        singleBlogImage = findViewById(R.id.single_blog_image);
        singleBlogTitle = findViewById(R.id.single_blog_title);
        singleBlogContent = findViewById(R.id.single_blog_content);
        removeBlogBtn = findViewById(R.id.remove_blog_btn);

        mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_title = dataSnapshot.child("title").getValue().toString();
                String post_content = dataSnapshot.child("content").getValue().toString();
                final String post_image = dataSnapshot.child("image_url").getValue().toString();
                String post_user_id = dataSnapshot.child("uid").getValue().toString();

                singleBlogTitle.setText(post_title);
                singleBlogContent.setText(post_content);

                Picasso.get().load(post_image).networkPolicy(NetworkPolicy.OFFLINE).into(singleBlogImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(post_image).into(singleBlogImage);
                    }
                });

                singleBlogImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

                //Toast.makeText(PostView.this, mAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();

                if (mAuth.getCurrentUser().getUid().equals(post_user_id)) {
                    removeBlogBtn.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        removeBlogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabase.child(post_key).removeValue();
                finish();

            }
        });

    }
}
