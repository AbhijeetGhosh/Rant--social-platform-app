package com.apps.abhijeet.rant;

import android.icu.util.Freezable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PersonProfileActivity extends AppCompatActivity {

    private CircularImageView userProfImg;
    private TextView userFullname;
    private TextView userUsername;
    private TextView userDob;
    private TextView userStatus;
    private TextView userGender;
    private TextView userCountry;

    private Button SendFriendRequestBtn, DeclineFriendReqBtn;

    private DatabaseReference FriendRequestRef, UsersRef, FriendsRef;
    private FirebaseAuth mAuth;

    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid(); // receive sender user id from firebase db

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        initializeFields();


        //this retrieves the information of the person who is being visited
        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
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

                    Picasso.Builder picassoBuilder = new Picasso.Builder(PersonProfileActivity.this);
                    Picasso picasso = picassoBuilder.build();
                    picasso.load(myProfImg).placeholder(R.mipmap.ic_person).into(userProfImg);

                    userUsername.setText("@" + myUsername);
                    userStatus.setText(myStatus);
                    userFullname.setText(myFullname);
                    userDob.setText("Date Of Birth" + myDob);
                    userCountry.setText("Country" + myCountry);
                    userGender.setText("Gender" + myGender);

                    //to have the button text as "Cancel friend request" once the activity is opened again
                    maintananceOFButton();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DeclineFriendReqBtn.setVisibility(View.INVISIBLE);
        DeclineFriendReqBtn.setEnabled(false);

        //user cannot send friend request to themselves
        if(!senderUserId.equals(receiverUserId))
        {
            SendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendFriendRequestBtn.setEnabled(false);

                    //send friend request
                    if(CURRENT_STATE.equals("not_friends"))
                    {
                        sendFriendRequestToaPerson();
                    }
                    if(CURRENT_STATE.equals("request_sent"))
                    {
                        cancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received"))
                    {
                        acceptFriendReq();
                    }
                    if(CURRENT_STATE.equals("friends"))
                    {
                        unfriendAnExistingFriend();
                    }
                }
            });
        }
        else
        {
            DeclineFriendReqBtn.setVisibility(View.INVISIBLE);
            SendFriendRequestBtn.setVisibility(View.INVISIBLE);
        }


    }

    private void unfriendAnExistingFriend()
    {
        //removing the value
        FriendsRef.child(senderUserId)
                .child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            //
                            FriendsRef.child(receiverUserId)
                                    .child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                //
                                                SendFriendRequestBtn.setEnabled(true);
                                                CURRENT_STATE="not_friends";
                                                SendFriendRequestBtn.setText("Send Friend Request");

                                                DeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                                DeclineFriendReqBtn.setEnabled(false);

                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void acceptFriendReq()
    {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //
                        if(task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //
                                            if(task.isSuccessful())
                                            {
                                                // if two users are friend then we will remove the data we stored
                                                // such as date and other thing to cancel friend request
                                                //removing the value
                                                FriendRequestRef.child(senderUserId)
                                                        .child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    //
                                                                    FriendRequestRef.child(receiverUserId)
                                                                            .child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        //
                                                                                        SendFriendRequestBtn.setEnabled(true);
                                                                                        CURRENT_STATE="friends";
                                                                                        SendFriendRequestBtn.setText("Unfriend this person");

                                                                                        DeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                                                                        DeclineFriendReqBtn.setEnabled(false);

                                                                                    }
                                                                                }
                                                                            });

                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelFriendRequest()
    {
        //removing the value
        FriendRequestRef.child(senderUserId)
                .child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            //
                            FriendRequestRef.child(receiverUserId)
                                    .child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                //
                                                SendFriendRequestBtn.setEnabled(true);
                                                CURRENT_STATE="not_friends";
                                                SendFriendRequestBtn.setText("Send Friend Request");

                                                DeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                                DeclineFriendReqBtn.setEnabled(false);

                                            }
                                        }
                                    });

                        }
                    }
                });
    }


    //to have the button text as "Cancel friend request" once the activity is opened again and other things
    private void maintananceOFButton()
    {
        FriendRequestRef.child(senderUserId)
                  .addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                      {
                          //the request is send and the request is yet to be responded
                          if(dataSnapshot.hasChild(receiverUserId))
                          {
                              String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                              if(request_type.equals("sent"))
                              {
                                  CURRENT_STATE = "request_sent";
                                  SendFriendRequestBtn.setText("Cancel Friend Request * 2");

                                  DeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                  DeclineFriendReqBtn.setEnabled(false);

                              }
                              else if(request_type.equals("received"))
                              {
                                  CURRENT_STATE="request_received";
                                  SendFriendRequestBtn.setText("Accept Friend Request");

                                  //DeclineFriendrequest button will be made visible we will not need this
                                  DeclineFriendReqBtn.setVisibility(View.VISIBLE);
                                  DeclineFriendReqBtn.setEnabled(true);

                                  //to decline friend request, we will not need this
                                  DeclineFriendReqBtn.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View view) {
                                          cancelFriendRequest();
                                      }
                                  });

                              }
                          }
                          //the request is responded and they are friends
                          else
                          {
                              FriendsRef.child(senderUserId)
                                       .addListenerForSingleValueEvent(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                           {
                                                if(dataSnapshot.hasChild(receiverUserId))
                                                {
                                                    CURRENT_STATE="friends";
                                                    SendFriendRequestBtn.setText("Unfriend");

                                                    DeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                                    DeclineFriendReqBtn.setEnabled(false);
                                                }
                                           }

                                           @Override
                                           public void onCancelled(@NonNull DatabaseError databaseError)
                                           {

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

    private void sendFriendRequestToaPerson()
    {
        //sending the friend request
        FriendRequestRef.child(senderUserId)
                         .child(receiverUserId)
                          .child("request_type")
                           .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            //after the request is sent
                            FriendRequestRef.child(receiverUserId)
                                             .child(senderUserId)
                                              .child("request_type")
                                               .setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                //to cancel the request
                                                SendFriendRequestBtn.setEnabled(true);
                                                CURRENT_STATE="request_sent";
                                                SendFriendRequestBtn.setText("Cancel Friend Req");

                                                DeclineFriendReqBtn.setVisibility(View.INVISIBLE);
                                                DeclineFriendReqBtn.setEnabled(false);

                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    //this initializes fields
    private void initializeFields()
    {

        //Information of person being visited
        userFullname = (TextView) findViewById(R.id.Person_FullName);
        userUsername = (TextView) findViewById(R.id.Person_UserName);
        userGender = (TextView) findViewById(R.id.Person_Gender);
        userDob = (TextView) findViewById(R.id.Person_Dob);
        userCountry = (TextView) findViewById(R.id.Person_Country);
        userStatus = (TextView) findViewById(R.id.Person_Status);
        userProfImg = (CircularImageView) findViewById(R.id.person_profile_pic);

        //Buttons
        SendFriendRequestBtn = (Button)findViewById(R.id.Person_Send_Friend_request_btn);
        DeclineFriendReqBtn = (Button)findViewById(R.id.Decline_Friend_Req);

        //String
        CURRENT_STATE = "not_friends";

    }

}
