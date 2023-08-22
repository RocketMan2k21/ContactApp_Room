package com.example.contactapproom.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactapproom.MainActivity;
import com.example.contactapproom.R;
import com.example.contactapproom.db.entitity.Contact;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<Contact> contacts;
    private Context context;
    private MainActivity mainActivity;


    public MyAdapter(ArrayList<Contact> contacts, Context context, MainActivity mainActivity) {
        this.contacts = contacts;
        this.context = context;
        this.mainActivity = mainActivity;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView contact_name;
        TextView contact_email;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            contact_name = itemView.findViewById(R.id.name);
            contact_email = itemView.findViewById(R.id.email);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.contact_list_item, null);

        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
         final Contact contact = contacts.get(position);
         holder.contact_email.setText(contact.getEmail());
         holder.contact_name.setText(contact.getName());
         holder.itemView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mainActivity.addAndEditContacts(true, contact, holder.getAdapterPosition());
             }
         });

    }
}