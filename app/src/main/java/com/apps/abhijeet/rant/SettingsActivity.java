package com.apps.abhijeet.rant;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import static com.apps.abhijeet.rant.SetupActivity.Gallery_Pick;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName;
    private EditText status;
    private EditText full_name;
    private EditText country;
    private EditText dob;
    private EditText gender;
    private CircularImageView mSettingProfImg;
    private Button mSaveBtn;
    private DatabaseReference settingUserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    final static int Gallery_Pick = 1;
    private ProgressDialog loadingBar;
    private StorageReference UsersProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        settingUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (EditText) findViewById(R.id.setting_username);
        status = (EditText) findViewById(R.id.setting_status);
        full_name = (EditText) findViewById(R.id.setting_full_name);
        country = (EditText) findViewById(R.id.setting_country);
        dob = (EditText) findViewById(R.id.setting_dob);
        gender = (EditText) findViewById(R.id.setting_gender);
        mSettingProfImg = (CircularImageView) findViewById(R.id.profImgSetting);
        mSaveBtn = (Button) findViewById(R.id.save_settings_btn);
        loadingBar = new ProgressDialog(this);
        UsersProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");//with name string.

        settingUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfImg = dataSnapshot.child("profileimage").getValue().toString();
                    String myUsername = dataSnapshot.child("username").getValue().toString();
                    String myFullname = dataSnapshot.child("fullname").getValue().toString();
                    String myStatus = dataSnapshot.child("status").getValue().toString();
                    String myDob = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("Gender").getValue().toString();

                    Picasso.Builder picassoBuilder = new Picasso.Builder(SettingsActivity.this);
                    Picasso picasso = picassoBuilder.build();
                    picasso.load(myProfImg).placeholder(R.mipmap.ic_person).into(mSettingProfImg);

                    userName.setText(myUsername);
                    status.setText(myStatus);
                    full_name.setText(myFullname);
                    dob.setText(myDob);
                    country.setText(myCountry);
                    gender.setText(myGender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAccountInfo();
            }
        });

        mSettingProfImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GalleryIntent = new Intent();
                GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                GalleryIntent.setType("image/*");
                startActivityForResult(GalleryIntent,Gallery_Pick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile image");
                loadingBar.setMessage("Please wait while your image is stored");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();
                StorageReference filePath = UsersProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Imgage is successfully saved in firebase storage databaes", Toast.LENGTH_SHORT).show();

                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override public void onSuccess(Uri uri)
                                {
                                    final String downloadUrl = uri.toString();

                                    settingUserRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SettingsActivity.this, "Image stored successfully second time", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage(); Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(SettingsActivity.this, "Image cannot be croped human", Toast.LENGTH_SHORT).show(); loadingBar.dismiss();
            }
        }
    }

    private void validateAccountInfo() {
        String sUsername = userName.getText().toString();
        String sStatus = status.getText().toString();
        String sCountry = country.getText().toString();
        String sDob = dob.getText().toString();
        String sGender = gender.getText().toString();
        String sFullname = full_name.getText().toString();

        if(TextUtils.isEmpty(sUsername)||TextUtils.isEmpty(sStatus)||TextUtils.isEmpty(sCountry)||TextUtils.isEmpty(sDob)
                ||TextUtils.isEmpty(sGender)||TextUtils.isEmpty(sFullname)){
            Toast.makeText(SettingsActivity.this,"You Have Missed Something...",Toast.LENGTH_LONG).show();
        }
        else {
            loadingBar.setTitle("Profile image");
            loadingBar.setMessage("Please wait while your image is stored");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            UpdateAccountInfo(sUsername,sStatus,sCountry,sDob,sGender,sFullname);
        }
    }

    private void UpdateAccountInfo(String sUsername, String sStatus, String sCountry, String sDob, String sGender, String sFullname) {

        HashMap userMap = new HashMap();
        userMap.put("username",sUsername);
        userMap.put("status",sStatus);
        userMap.put("country",sCountry);
        userMap.put("dob",sDob);
        userMap.put("Gender",sGender);
        userMap.put("fullname",sFullname);

        settingUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    sendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this,"Account Settings updated!!",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
                else {
                    Toast.makeText(SettingsActivity.this,"Account updation Failed!!",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }


    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
