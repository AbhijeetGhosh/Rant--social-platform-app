package com.apps.abhijeet.rant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class PostActivity extends AppCompatActivity {

    private Toolbar mtoolbar; // i am not using this
    private ProgressDialog loadingBar;

    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private EditText postTitle;

    private final static int Gallery_Pick = 1;

    private String Description;
    private String Title;
    private Uri ImageUri;

    private StorageReference PostImageReference; //firebase storage is used to store images and videos
    private DatabaseReference UsersRef, PostRef;
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;
    private long countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        SelectPostImage = (ImageButton)findViewById(R.id.imageButton);
       UpdatePostButton = (Button)findViewById(R.id.FinalButton);
        PostDescription = (EditText)findViewById(R.id.EditT);
        postTitle = (EditText)findViewById(R.id.rantTitle);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        try
        {
            current_user_id = mAuth.getCurrentUser().getUid();
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        PostImageReference = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");

       SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidatePostInfo();

            }
        });


    }

    //cheking if the user has entered the required fields and storing the info
    private void ValidatePostInfo()
    {
        Description = PostDescription.getText().toString();
       // Title = postTitle.getText().toString();
        //checking if the user has entered image
      /*  if(ImageUri == null)
        {
            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show();
        }
        //checking if the user has entered the text description
        else*/ if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add new post");
            loadingBar.setMessage("Please wait while your new post is being updated");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImageToFirebaseStorage();
        }
    }

     //since getDownloadUrl method is depricated we will be using this instead.
    private void StoringImageToFirebaseStorage()
    {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = PostImageReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        //this will put image in database of firebase
        filePath.putFile(ImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
        {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
            {
                if(!task.isSuccessful())
                {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>()
        {
            @Override public void onComplete(@NonNull Task<Uri> task)
            {
                if (task.isSuccessful())
                {
                    Uri downUri = task.getResult();

                    Toast.makeText(PostActivity.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();

                    downloadUrl = downUri.toString();
                    SavingPostInformationToDatabase();
                }
                    else
                        {
                            String message = task.getException().getMessage();

                            Toast.makeText(PostActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                        }
            }
        });
    }

    private void SavingPostInformationToDatabase()
    {
        //to organize posts in right order
        PostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    countPosts = dataSnapshot.getChildrenCount();
                }
                else
                {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //to update post data
        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
               if(dataSnapshot.exists())
               {
                   String userFullname = dataSnapshot.child("fullname").getValue().toString();
                   String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                   //creating the number of elements we need inside the child folder post
                   HashMap postMap = new HashMap();
                        postMap.put("uid",current_user_id);
                        postMap.put("date",saveCurrentDate);
                       // postMap.put("time",saveCurrentTime);
                        postMap.put("description",Description);
                      //  postMap.put("title",Title);
                        postMap.put("postimage",downloadUrl);
                        postMap.put("profileimage",userProfileImage);
                        postMap.put("fullname",userFullname);
                        postMap.put("counter",countPosts);

                   //saving this hashmap inside post ref
                PostRef.child(current_user_id+postRandomName).updateChildren(postMap) //the current_user_id + postRandomName is just to make it more unique
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(PostActivity.this, "New Post is updated successfully",Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                    sendUserToMainActivity();
                                }
                                else
                                {
                                    Toast.makeText(PostActivity.this, "New Post is updated successfully",Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
               }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void openGallery()
    {
        Intent GalleryIntent = new Intent();
        GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        GalleryIntent.setType("image/*");
        startActivityForResult(GalleryIntent,Gallery_Pick);
    }

    //this is to get image from gallery after openning gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.home)
        {
            sentUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    */

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }


}
