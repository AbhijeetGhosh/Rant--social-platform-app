package com.apps.abhijeet.rant;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SearchButton;
    private EditText SearchInputText;

    private RecyclerView SearchResultList;

    private DatabaseReference allUsersDatabaseRef;

    public String SearchBoxInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_find_friends);

        allUsersDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.find_friend_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


        SearchResultList=(RecyclerView)findViewById(R.id.Search_Result_List);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton=(ImageButton)findViewById(R.id.Search_Button);
        SearchInputText=(EditText)findViewById(R.id.search_box_input);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchBoxInput=SearchInputText.getText().toString();

                onStart();
            }
        });


    }


        /*

    private void SearchFriends(String SearchBoxInput)
    {
        Toast.makeText(this,"Searching",Toast.LENGTH_LONG).show();

        Query searchFriendQuery = allUsersDatabaseRef.orderByChild("fullname").startAt(SearchBoxInput).endAt(SearchBoxInput + "\uf8ff");

        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>
                (
                        FindFriends.class,
                        R.layout.all_users_display_layout,
                        FindFriendsViewHolder.class,
                        allUsersDatabaseRef
                )
                }}}}


        FirebaseRecyclerOptions<FindFriends> options=new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(searchFriendQuery,FindFriends.class)
                .build();
        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>
                firebaseRecyclerAdapter = new FirebaseRecyclerAdapter< FindFriends, FindFriendsViewHolder>(options)

                {
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder viewHolder, int position, @NonNull FindFriends model) {
                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setStatus(model.getStatus());
                        viewHolder.setProfileimage(model.getProfileimage());
                    }

                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout, viewGroup, false);
                        FindFriendsViewHolder viewHolder=new FindFriendsViewHolder(view);
                        return viewHolder;
                    }
                };



        SearchResultList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    */

        //we use firebase adapter within onStart as we have to have onStart method to work

    @Override
    protected void onStart() {
        super.onStart();

        Toast.makeText(this,"Searching",Toast.LENGTH_LONG).show();

        Query searchFriendQuery = allUsersDatabaseRef.orderByChild("fullname").startAt(SearchBoxInput).endAt(SearchBoxInput + "\uf8ff");

       /* FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>
                (
                        FindFriends.class,
                        R.layout.all_users_display_layout,
                        FindFriendsViewHolder.class,
                        allUsersDatabaseRef
                )
                }}}}

                */
        FirebaseRecyclerOptions<FindFriends> options=new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(searchFriendQuery,FindFriends.class)
                .build();// searchFriendQuery is applied here
        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>
                firebaseRecyclerAdapter = new FirebaseRecyclerAdapter< FindFriends, FindFriendsViewHolder>(options)

        {
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder viewHolder, final int position, @NonNull FindFriends model) {
                viewHolder.setFullname(model.getFullname());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setProfileimage(model.getProfileimage());

                //to send current human to the profile of the humman he is clicking on
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(FindFriendsActivity.this , PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }

            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout, viewGroup, false);
                FindFriendsViewHolder viewHolder=new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };



        SearchResultList.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();
    }






    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setProfileimage(String profileimage) {
            CircularImageView myImage = (CircularImageView) mView.findViewById(R.id.all_users_profile_img);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFullname(String fullname) {
            TextView myName=(TextView)mView.findViewById(R.id.all_users_profile_name);
            myName.setText(fullname);
        }

        public void setStatus(String status) {
            TextView myStatus=(TextView)mView.findViewById(R.id.all_users_status);
            myStatus.setText(status);
        }

    }

}
