package com.example.whatsappclone.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.whatsappclone.Fragments.ChatFragment;
import com.example.whatsappclone.Fragments.ContactFragment;
import com.example.whatsappclone.Fragments.GroupFragment;
import com.example.whatsappclone.Fragments.RequestFragment;

public class TabLayoutAdapter extends FragmentPagerAdapter {


    public TabLayoutAdapter(FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 1:
                GroupFragment groupFragment = new GroupFragment();
                return groupFragment;
            case 2:
                ContactFragment contactFragment = new ContactFragment();
                return contactFragment;

            case 3:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
               return "Chats";
            case 1:
                return "Group";
            case 2:
                return "Contact";

            case 3:
                return "Requests";

            default:
                return null;
        }

    }
}
