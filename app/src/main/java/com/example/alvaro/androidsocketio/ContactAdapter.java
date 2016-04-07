package com.example.alvaro.androidsocketio;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<User> mContacts;
    private int[] mUsernameColors;

    public ContactAdapter(List<User> contacts) {
        //  mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        layout = R.layout.layout_contact;

        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        User contact = mContacts.get(position);

        if(contact.getName() != null) viewHolder.setName(contact.getName());
        if(contact.getImage() != null) viewHolder.setImage(contact.getImage());

    }

    @Override
    public int getItemCount() {
        if(mContacts != null) { return mContacts.size(); } else { return  0;}
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mNameView;
        private TextView mNickNameView;
        public TextView mAuthor;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.contact_image);
            mNameView = (TextView) itemView.findViewById(R.id.contact_name);
        }

        public void setName(String name) {
            if (null == mNameView) return;
            mNameView.setText(name);
        }

        public void setImage(Bitmap bmp){
            if(null == mImageView) return;
            if(null == bmp) return;
            mImageView.setImageBitmap(bmp);
        }

        private int getUsernameColor(String username) {
            int hash = 7;
            for (int i = 0, len = username.length(); i < len; i++) {
                hash = username.codePointAt(i) + (hash << 5) - hash;
            }
            int index = Math.abs(hash % mUsernameColors.length);
            return mUsernameColors[index];
        }
    }
}
