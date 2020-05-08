package com.example.whatsappclone.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.whatsappclone.Contacts;
import com.example.whatsappclone.FindFriendActivity;
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
public class ContactFragment extends Fragment {

    private View view;
    private RecyclerView recyclerView;
    private DatabaseReference ContactRef, UserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public ContactFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_contact, container, false);
        recyclerView = view.findViewById(R.id.recycler_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull final Contacts model) {
                String userIDs = getRef(position).getKey();
                UserRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){


                            if (dataSnapshot.child("userState").hasChild("state")){
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")){
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }else if (state.equals("offline")){
                                    holder.onlineIcon.setVisibility(View.GONE);
                                }

                            }else {
                                holder.onlineIcon.setVisibility(View.GONE);
                            }





                            if (dataSnapshot.hasChild("image")){
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.Username.setText(profileName);
                                holder.Userstatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.profilelogo).into(holder.img);

                            }else {
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.Username.setText(profileName);
                                holder.Userstatus.setText(profileStatus);
                            }

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_list,parent,false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView Username, Userstatus;
        CircularImageView img;
        ImageView onlineIcon;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            Username = itemView.findViewById(R.id.new_User);
            Userstatus = itemView.findViewById(R.id.user_status);
            img = itemView.findViewById(R.id.user_profile);
            onlineIcon = itemView.findViewById(R.id.online_logo);
        }
    }
}
