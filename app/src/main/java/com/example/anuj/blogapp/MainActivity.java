package com.example.anuj.blogapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private RecyclerView blog_list_view;

    private Query query;
    private StorageReference mStorage;
    private FirebaseRecyclerAdapter<BlogContent, BlogViewHolder> adapter;

    private DatabaseReference mDatabaseUsers;

    private DatabaseReference mDatabaseLikes;

    private FirebaseAuth mAuth;

    private boolean liked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*else {
            startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build()
                            ))
                            .build(),
                    RC_SIGN_IN);
        }*/

        mAuth = FirebaseAuth.getInstance();

        query = FirebaseDatabase.getInstance().getReference().child("Blogs");
        query.keepSynced(true);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseUsers.keepSynced(true);

        mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLikes.keepSynced(true);

        mStorage = FirebaseStorage.getInstance().getReference();

        checkUserInDB();

        blog_list_view = findViewById(R.id.blog_view_list);
        blog_list_view.setHasFixedSize(true);
        blog_list_view.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<BlogContent> options = new FirebaseRecyclerOptions.Builder<BlogContent>()
                .setQuery(query, BlogContent.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<BlogContent, BlogViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, int position, @NonNull BlogContent model) {

                final String post_key = getRef(position).getKey();
                final String this_user_name = model.getUser_name();

                holder.setTitle(model.getTitle());
                holder.setContent(model.getContent());
                holder.setImage(getApplicationContext(), model.getImage_url());
                holder.setUserName(model.getUser_name());
                holder.setlikeBtn(post_key);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(MainActivity.this, post_key, Toast.LENGTH_SHORT).show();
                        Intent postIntent = new Intent(MainActivity.this, PostView.class);
                        postIntent.putExtra("post_key", post_key);
                        startActivity(postIntent);
                    }
                });

                holder.likeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        liked = true;

                        mDatabaseLikes.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (liked) {

                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                        mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        liked = false;

                                    } else {
                                        mDatabaseLikes.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(this_user_name);
                                        liked = false;
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                });

            }

            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.blog_view, parent, false);

                return new BlogViewHolder(view);
            }
        };

        blog_list_view.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void checkUserInDB() {
        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(user_id)) {

                        Toast.makeText(MainActivity.this, "Account setup required!!!", Toast.LENGTH_SHORT).show();
                        //finish();
                        startActivity(new Intent(MainActivity.this, AccountSetup.class));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageButton likeBtn;

        DatabaseReference mDatabaseLikes;
        FirebaseAuth mAuth;

        public BlogViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            likeBtn = mView.findViewById(R.id.like_button);

            mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
            mDatabaseLikes.keepSynced(true);
            mAuth = FirebaseAuth.getInstance();
        }

        public void setlikeBtn(final String post_key) {
            mDatabaseLikes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                        likeBtn.setImageResource(R.drawable.like_filled);
                    }
                    else {
                        likeBtn.setImageResource(R.drawable.like_empty);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String mTitle) {
            TextView blog_title = mView.findViewById(R.id.blog_title);
            blog_title.setText(mTitle);
        }

        public void setContent(String mContent) {
            TextView blog_cont = mView.findViewById(R.id.blog_content);
            blog_cont.setText(mContent);
        }

        public void setImage(final Context context, final String mUrl) {

            final ImageView blog_image = mView.findViewById(R.id.blog_image);
            Picasso.get().load(mUrl).networkPolicy(NetworkPolicy.OFFLINE).into(blog_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(mUrl).into(blog_image);
                }
            });
            blog_image.setScaleType(ImageView.ScaleType.FIT_CENTER);

            /*
            StorageReference urlRef = FirebaseStorage.getInstance().getReferenceFromUrl(mUrl);

            final long TWO_MEGABYTE = 2 * 1024 * 1024;

            urlRef.getBytes(TWO_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    ImageView blog_image = mView.findViewById(R.id.blog_image);
                    blog_image.setImageBitmap(bmp);
                    blog_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
            */
        }

        public void setUserName(String mUserName) {
            TextView user_name_txt = mView.findViewById(R.id.blog_user_name);
            user_name_txt.setText(mUserName);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ico, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(MainActivity.this, AddPost.class));
        }

        if (item.getItemId() == R.id.action_logout) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(logoutIntent);
                    //finish();
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }
}
