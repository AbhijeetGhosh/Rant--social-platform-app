package com.apps.abhijeet.rant;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView postImage;
    private TextView postDescription;
    private Button deletePost, editPost;

    private FirebaseAuth mAtuth;

    private String PostKey, currentUserId, databaseUserId, description, image;

    private DatabaseReference ClickPostRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        PostKey = getIntent().getExtras().get("PostKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        postImage = (ImageView)findViewById(R.id.click_post_image);
        postDescription = (TextView)findViewById(R.id.click_post_description);
        deletePost = (Button)findViewById(R.id.delete_post_button);
        editPost = (Button)findViewById(R.id.edit_post_button);


        //If edit post and delete post button will be invisible in the beginning but if the id of current user and post match then they will be visible

        deletePost.setVisibility(View.INVISIBLE);
        editPost.setVisibility(View.INVISIBLE);

        mAtuth = FirebaseAuth.getInstance();
        currentUserId = mAtuth.getCurrentUser().getUid();


        //to fetch the image and description of post in this activity
        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check if the post exists or not because after you delete it, post will not exist and this may crash
                if(dataSnapshot.exists())
                {
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();

                    databaseUserId = dataSnapshot.child("uid").getValue().toString();

                    postDescription.setText(description);
                    Picasso.get().load(image).into(postImage);

                    //checking if the id of post and current user who is pressing post is same
                    if(currentUserId.equals(databaseUserId))
                    {
                        deletePost.setVisibility(View.VISIBLE);
                        editPost.setVisibility(View.VISIBLE);
                    }

                    //functionality to edit post button to edit description
                    editPost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editCurrentPost(description);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteCurrentPost();
            }
        });

    }

    private void editCurrentPost(String description)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        //function to update button
        builder.setPositiveButton("update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                     ClickPostRef.child("description").setValue(inputField.getText().toString());
                     Toast.makeText(ClickPostActivity.this,"Post updated",Toast.LENGTH_SHORT).show();
            }
        });

        //function to cancel button
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void DeleteCurrentPost()
    {
        ClickPostRef.removeValue();

        sendUserToMainActivity();

        Toast.makeText(this, "Post is deleted in this universe", Toast.LENGTH_SHORT).show();
    }

    private void sendUserToMainActivity()
    {
        Intent MainActivityIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainActivityIntent);
        finish();
    }
}
