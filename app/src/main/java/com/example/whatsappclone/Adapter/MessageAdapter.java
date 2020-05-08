package com.example.whatsappclone.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.Messages;
import com.example.whatsappclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    DatabaseReference userRef;

    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String receiveImage = dataSnapshot.child("image").getValue().toString();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

       /* holder.receiverMessageText.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageRecieberPicture.setVisibility(View.GONE);
        holder.mesaageSenderPicture.setVisibility(View.GONE);
*/
        if (fromMessageType.equals("text")){

            if (fromUserId.equals(messageSenderId)){
                holder.receiverMessageText.setVisibility(View.GONE);
                /*holder.leftTime.setVisibility(View.GONE);*/
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.messageRecieberPicture.setVisibility(View.GONE);
                holder.mesaageSenderPicture.setVisibility(View.GONE);
                holder.senderMessageText.setText(messages.getMessage());
            }else {
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setVisibility(View.GONE);
                holder.messageRecieberPicture.setVisibility(View.GONE);
                holder.mesaageSenderPicture.setVisibility(View.GONE);
                /*holder.leftTime.setVisibility(View.VISIBLE);*/

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setText(messages.getMessage());
                /*holder.leftTime.setText(messages.getTime());*/

            }
        }else if (fromMessageType.equals("image")){
            if (fromUserId.equals(messageSenderId)){
                holder.mesaageSenderPicture.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.GONE);
                holder.messageRecieberPicture.setVisibility(View.GONE);
                holder.senderMessageText.setVisibility(View.GONE);
                Picasso.get().load(messages.getMessage()).into(holder.mesaageSenderPicture);
            }else {
                holder.messageRecieberPicture.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.GONE);
                holder.mesaageSenderPicture.setVisibility(View.GONE);
                holder.senderMessageText.setVisibility(View.GONE);
                Picasso.get().load(messages.getMessage()).into(holder.messageRecieberPicture);


            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText, receiverMessageText, leftTime;
        public ImageView  mesaageSenderPicture, messageRecieberPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            /*leftTime = itemView.findViewById(R.id.leftTime);*/
            mesaageSenderPicture = itemView.findViewById(R.id.message_sender_Image_view);
            messageRecieberPicture = itemView.findViewById(R.id.message_reciever_Image_view);
        }
    }
}
