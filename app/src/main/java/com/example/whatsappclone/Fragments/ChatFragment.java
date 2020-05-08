package com.example.whatsappclone.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.whatsappclone.ChatActivity;
import com.example.whatsappclone.Contacts;
import com.example.whatsappclone.FindFriendActivity;
import com.example.whatsappclone.LoginActivity;
import com.example.whatsappclone.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    View view;
    RecyclerView recyclerView;
    DatabaseReference ChatRef, UserRef;
    FirebaseAuth mAuth;
    String currentUserId;


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_chat, container, false);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Intent goToLogin = new Intent(getContext(), LoginActivity.class);
            startActivity(goToLogin);
        }

        currentUserId = mAuth.getCurrentUser().getUid();
        ChatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView = view.findViewById(R.id.recycler_view_List);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts contacts) {

                final String[] retreiveImage = {"default_image"};
                final String userIDs = getRef(position).getKey();
                UserRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       if (dataSnapshot.exists()){

                           if (dataSnapshot.hasChild("image")){
                               retreiveImage[0] = dataSnapshot.child("image").getValue().toString();
                               Picasso.get().load(retreiveImage[0]).placeholder(R.drawable.profilelogo).into(holder.chatImage);
                           }


                           final String retreiveName = dataSnapshot.child("name").getValue().toString();
                           final String retreiveStatus = dataSnapshot.child("status").getValue().toString();

                           holder.chatUser.setText(retreiveName);
                         //  holder.chatStatus.setText("Last Seen: "+"\n"+ "Date "+ "Time");

                           if (dataSnapshot.child("userState").hasChild("state")){
                               String state = dataSnapshot.child("userState").child("state").getValue().toString();
                               String date = dataSnapshot.child("userState").child("date").getValue().toString();
                               String time = dataSnapshot.child("userState").child("time").getValue().toString();

                               if (state.equals("online")){
                                   holder.chatStatus.setText("online");
                               }else if (state.equals("offline")){
                                   holder.chatStatus.setText("Last Seen: "+ date + " "+ time);
                               }

                           }else {
                               holder.chatStatus.setText("offline");
                           }







                           holder.itemView.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View view) {
                                   Intent intent = new Intent(getContext(), ChatActivity.class);
                                   intent.putExtra("visit_user_id", userIDs);
                                   intent.putExtra("visit_user_name", retreiveName);
                                   intent.putExtra("visit_user_image", retreiveImage[0]);
                                   startActivity(intent);
                               }
                           });
                       }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_list, parent, false);
                ChatsViewHolder viewHolder = new ChatsViewHolder(view);
                return viewHolder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        TextView chatUser, chatStatus;
        CircularImageView chatImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            chatUser = itemView.findViewById(R.id.new_User);
            chatStatus = itemView.findViewById(R.id.user_status);
            chatImage = itemView.findViewById(R.id.user_profile);
        }
    }
}
