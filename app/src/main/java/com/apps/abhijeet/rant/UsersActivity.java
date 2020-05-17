package com.apps.abhijeet.rant;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class UsersActivity extends AppCompatActivity {

    private TextView userFullname;
    private TextView userUsername;
    private TextView userDob;
    private TextView userStatus;
    private TextView userGender;
    private TextView userCountry;
    private CircularImageView userProfImg;

    private DatabaseReference profileUserRef;
    private FirebaseAuth mAuth;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        userFullname = (TextView) findViewById(R.id.userFullName);
        userUsername = (TextView) findViewById(R.id.userUserName);
        userGender = (TextView) findViewById(R.id.userGender);
        userDob = (TextView) findViewById(R.id.userDob);
        userCountry = (TextView) findViewById(R.id.userCountry);
        userStatus = (TextView) findViewById(R.id.userStatus);
        userProfImg = (CircularImageView) findViewById(R.id.userProfImg);

        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    String myProfImg = dataSnapshot.child("profileimage").getValue().toString();
                    String myUsername = dataSnapshot.child("username").getValue().toString();
                    String myFullname = dataSnapshot.child("fullname").getValue().toString();
                    String myStatus = dataSnapshot.child("status").getValue().toString();
                    String myDob = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("Gender").getValue().toString();

                    Picasso.Builder picassoBuilder = new Picasso.Builder(UsersActivity.this);
                    Picasso picasso = picassoBuilder.build();
                    picasso.load(myProfImg).placeholder(R.mipmap.ic_person).into(userProfImg);

                    userUsername.setText("@" + myUsername);
                    userStatus.setText(myStatus);
                    userFullname.setText(myFullname);
                    userDob.setText("Date Of Birth" + myDob);
                    userCountry.setText("Country" + myCountry);
                    userGender.setText("Gender" + myGender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
