package com.apps.abhijeet.rant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.Random;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, AgreeRef,DisagreeRef;
    private CircularImageView NavProfileImage;
    private TextView NavProfileUserName;
    boolean agreeChecker = false;
    boolean disagreeChecker = false;
    String currentUserID;
    private RecyclerView postList;

    String AgreePopup = "ikr";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);


        mAuth = FirebaseAuth.getInstance(); //for authentications
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        //agree and disagree
        AgreeRef = FirebaseDatabase.getInstance().getReference().child("Agrees");
        DisagreeRef = FirebaseDatabase.getInstance().getReference().child("Disagrees");

        postList = (RecyclerView)findViewById(R.id.postList);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true); //as new post will be above
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        //this is for toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //this is for floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_new_post_fab); //changed fab to add_new_post_fab
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                sendUserToPostActivity();
            }
        });

        //this is for navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        View navView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        NavProfileImage = (CircularImageView)navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView)navView.findViewById(R.id.nav_profile_user_name);


        //if logged in currentUserId will be given value of the id of the user, or else they will shifted to login activity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            //taking id of user
            currentUserID=mAuth.getCurrentUser().getUid();
            //getting user's fullname and display image from database and using that in navigation bar of that user with the id we took in above line of code
            setUserInfoOnNavDrawer();
        }

        DisplayAllUsersPosts();

    }

    //Done below according to some sources
    /*
    private void DisplayAllUersPost()
    {
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>() {
                    @Override
                    protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull com.apps.abhijeet.rant.Posts model) {

                    }

                    @NonNull
                    @Override
                    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        return null;
                    }
                };
    }
    */

    //using firebase ui library, tutorial 23 comments
    private void DisplayAllUsersPosts()
    {
        Query sortPostsInDecendingOrder = PostsRef.orderByChild("counter");

        FirebaseRecyclerOptions<Posts> options=new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(sortPostsInDecendingOrder,Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
            new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options)
            {
                @Override protected void onBindViewHolder(@NonNull PostsViewHolder holder,final int position, @NonNull Posts model)
                {

                    //generating unique key for a post according to it's position in recycler view to edit post later
                    final String PostKey = getRef(position).getKey();

                    //here it is fetching the methods from Posts.class
                    holder.username.setText(model.getFullname());
                    holder.date.setText(model.getDate());
                    holder.description.setText(model.getDescription());
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                         //   startActivity(new Intent(MainActivity.this,CommentActivity.class));
                        }
                    });
                    holder.commentText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                           // startActivity(new Intent(MainActivity.this,CommentActivity.class));
                        }
                    });
                   // holder.title.setText(model.getTitle());
                   // Picasso.get().load(model.getPostimage()).into(holder.postImage);
                    Picasso.get().load(model.getProfileimage()).into(holder.user_post_image);

                    //click post activity opens when someone clicks on post
                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent ClickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                            ClickPostIntent.putExtra("PostKey",PostKey);
                            startActivity(ClickPostIntent);
                        }
                    });
                    //calling functions to show total agrees and disagrees
                    holder.setAgreeTextStatus(PostKey);
                    holder.setDisagreeTextStatus(PostKey);

                    //
                    holder.agree.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            agreeChecker = true;
                            AgreeRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (agreeChecker){
                                        if(dataSnapshot.child(PostKey).hasChild(currentUserID))
                                        {
                                            //if agree is already clicked then it will remove agree on clicking it again
                                            AgreeRef.child(PostKey).child(currentUserID).removeValue(); //remove agree from database
                                            agreeChecker = false;
                                          //  disagreeChecker = false;
                                            //disagreeChecker = true;

                                        }
                                        else {
                                            final String PostKey = getRef(position).getKey();

                                             AgreeRef = FirebaseDatabase.getInstance().getReference().child("Agrees");

                                             AgreeRef.child(PostKey).child(currentUserID).setValue(true); //adding agree
                                            // DisagreeRef.child(PostKey).child(currentUserID).removeValue();//removing disagree
                                            // disagreeChecker = false;



                                             //ikr popup
                                            ikrToastDisplay();
                                            Toasty.info(MainActivity.this,AgreePopup,Toasty.LENGTH_SHORT).show();
                                            }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

                                }
                            });
                        }
                    });

                    //disagree

                    holder.disagree.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            disagreeChecker=true;
                            DisagreeRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (disagreeChecker){
                                        if(dataSnapshot.child(PostKey).hasChild(currentUserID)){
                                         //  DisagreeRef.child(postKey).child(currentUserID).removeValue();
                                         //  disagreeChecker = false;
                                         //  agreeChecker = false;
                                        }else {
                                            final String PostKey = getRef(position).getKey();
                                            DisagreeRef = FirebaseDatabase.getInstance().getReference().child("Disagrees");

                                            DisagreeRef.child(PostKey).child(currentUserID).setValue(true); //adding disagree
                                            AgreeRef.child(PostKey).child(currentUserID).removeValue();     //removing agree
                                            agreeChecker = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });


                }
                @NonNull @Override public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                {
                    View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.all_post_layout,parent,false);
                    PostsViewHolder viewHolder=new PostsViewHolder(view); return viewHolder;
                }
            };

            //we set the recycler view to firebaseRecyclerAdapter
                postList.setAdapter(firebaseRecyclerAdapter);
                firebaseRecyclerAdapter.startListening();
    }

    private void ikrToastDisplay()
    {
        Random rand = new Random();

        int i = rand.nextInt(7);

            switch(i)
            {
                case 1: AgreePopup = "you said it";
                break;

                case 2: AgreePopup = "true that";
                break;

                case 3: AgreePopup = "foshizzle";
                break;

                case 4: AgreePopup = "roger that";
                break;

                case 5: AgreePopup = "roger that2";
                break;

                case 6: AgreePopup = "roger that3";
                break;

                case 7: AgreePopup = "roger that w";
                break;

            }

    }

    //class used for firebase ui in tutorial 23 to acess the components of all_post_layout from DisplayAllUsersPosts() method metioned right over this
    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        TextView username,date,description,title;
        CircularImageView user_post_image;
         ImageView agree,disagree,comment;
        TextView agreeText, disagreeText, commentText;
        int countAgrees = 0, countDisagrees = 0;
        String currentUserId;
        DatabaseReference AgreeRef,DisagreeRef;
        //ImageView postImage;

        public PostsViewHolder(View itemView)
        {
            super(itemView);

            mView = itemView;

            agree = (ImageView) itemView.findViewById(R.id.ikr);
            disagree = (ImageView) itemView.findViewById(R.id.disagree);
            comment = (ImageView) itemView.findViewById(R.id.comment);
            agreeText = (TextView) itemView.findViewById(R.id.no_of_ikr);
            disagreeText = (TextView) itemView.findViewById(R.id.no_0f_disagrees);
            commentText = (TextView) itemView.findViewById(R.id.no_0f_comments);
            username=itemView.findViewById(R.id.postFullName);
            date=itemView.findViewById(R.id.postDate);
           // title=itemView.findViewById(R.id.postTitle);
            description=itemView.findViewById(R.id.postDesc);
           // postImage=itemView.findViewById(R.id.postImg);
            user_post_image=itemView.findViewById(R.id.profImg);

            AgreeRef = FirebaseDatabase.getInstance().getReference().child("Agrees");
            DisagreeRef = FirebaseDatabase.getInstance().getReference().child("Disagrees");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setAgreeTextStatus(final String postKey){
            AgreeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKey).hasChild(currentUserId)){
                        countAgrees = (int) dataSnapshot.child(postKey).getChildrenCount();
                      //  countDisagrees = (int) dataSnapshot.child(postKey).getChildrenCount();
                        agreeText.setText(Integer.toString(countAgrees));
                      //  disagreeText.setText(Integer.toString(countDisagrees));
                    }else{
                      //  countDisagrees = (int) dataSnapshot.child(postKey).getChildrenCount();
                        countAgrees = (int) dataSnapshot.child(postKey).getChildrenCount();
                        agreeText.setText(Integer.toString(countAgrees));
                       // disagreeText.setText(Integer.toString(countDisagrees));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

       public void setDisagreeTextStatus(final String postKey) {
           DisagreeRef.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if (dataSnapshot.child(postKey).hasChild(currentUserId)) {
                       countDisagrees = (int) dataSnapshot.child(postKey).getChildrenCount();
                       disagreeText.setText(Integer.toString(countDisagrees));
                   } else {
                       countDisagrees = (int) dataSnapshot.child(postKey).getChildrenCount();
                       disagreeText.setText(Integer.toString(countDisagrees));
                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
       }
    }


    private void sendUserToPostActivity()
    {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    private void setUserInfoOnNavDrawer()
    {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    //getting username and profile image from database
                    //setting the navigation bar with the user fullname and profile image

                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this,"Profile name does not existt",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //checks if the user has registered or successfully authenticated or not.
    @Override
    protected void onStart() {
        super.onStart();


        FirebaseUser currentUser = mAuth.getCurrentUser();

        //if the user is not authenticated since he needs to login, we send him to login activity
        if(currentUser==null)
        {
            sendUserToLoginActivity();
        }
        else//if user has an account but has not setup his profile
        {
            checkUserExistence();
        }
    }

    private void checkUserExistence()
    {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        //if users is authenticated but has not set up his profile
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id))
                {
                    sendUsersToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUsersToSetupActivity()
    {
        Intent SetupIntent = new Intent(MainActivity.this, SetupActivity.class);
        SetupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SetupIntent);
        finish();
    }

    private void sendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK); //this will be start of a new task and other
                                                                                             //same activity, if running it will be closed
        startActivity(loginIntent);
        finish();
    }

    //this is for when back is pressed
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //this is for adding to action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    //this is when something is selected in action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

        }

        return super.onOptionsItemSelected(item);
    }

    //this is when something is selected in navigation bar
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_users_rants) {
            // Handle the camera action
        }

        else if (id == R.id.nav_liked_rants) {

        }

        else if (id == R.id.nav_setting) {
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));
        }

        else if (id==R.id.nav_logout)
        {
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        else  if (id==R.id.Find_Friends)
        {
            sendUsersToFindFriendsActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void goToProfile(View view) {
        startActivity(new Intent(MainActivity.this,UsersActivity.class));
    }

    private void sendUsersToFindFriendsActivity()
    {
        Intent FindFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(FindFriendsIntent);
    }
}
