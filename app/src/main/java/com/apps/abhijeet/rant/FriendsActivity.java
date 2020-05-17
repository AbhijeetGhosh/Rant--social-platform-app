package com.apps.abhijeet.rant;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendList;

    private DatabaseReference friendsRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        myFriendList = (RecyclerView)findViewById(R.id.friend_list);

        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true); //as new post will be above
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        displayAllFriends();

    }
    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
    }

    private void displayAllFriends() {
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(friendsRef, Friends.class).build();
        FirebaseRecyclerAdapter<Friends, friendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, friendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final friendsViewHolder holder, int position, @NonNull final Friends model) {
                holder.date.setText("Friends Since: " + model.getDate());
                final String online_friends_id = getRef(position).getKey();
                usersRef.child(online_friends_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final String userName = dataSnapshot.child("fullname").getValue().toString();
                            final String userprofileimage = dataSnapshot.child("profileimage").getValue().toString();
                            holder.allusernames.setText(userName);
                            Picasso.get().load(userprofileimage).placeholder(R.drawable.profile).into(holder.alluserprofilepicture);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public friendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                friendsViewHolder viewHolder = new friendsViewHolder(view);
                return viewHolder;
            }
        };
        myFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class friendsViewHolder extends RecyclerView.ViewHolder
    {
        final CircularImageView alluserprofilepicture;
        final TextView allusernames, date;
        public friendsViewHolder(View itemView)
        {
            super(itemView);
            alluserprofilepicture = itemView.findViewById(R.id.all_users_profile_img);
            allusernames = itemView.findViewById(R.id.all_users_profile_name);
            date = itemView.findViewById(R.id.all_users_status);
        }
    }

}
