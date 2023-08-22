package com.example.contactapproom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.contactapproom.adapter.MyAdapter;
import com.example.contactapproom.db.ContactsAppDatabase;
import com.example.contactapproom.db.entitity.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    //Room database helper
    RecyclerView recyclerView;
    MyAdapter adapter;
    FloatingActionButton button;
    ArrayList<Contact> contacts = new ArrayList<>();
    private ContactsAppDatabase contactsAppDatabase;
    private RoomDatabase.Callback myCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //tool bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Favorite Contacts");

        //Callbacks
        RoomDatabase.Callback myCallBack = new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);

                //There are 4 contacts already created in the app when installed (Built-in contacts)
                createContact("Bill Gates", "billgates@microsoft.com");
                createContact("Nicola Tesla", "nicolatesla@tesla.com");
                createContact("Mark Zuckerberg", "mark_zuker@facebook.com");
                createContact("Satushi Namk", "satushi@bitcoin.com");

                Log.i("TAG", "Database was created");
            }

            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);

                Log.i("TAG", "Database was opened");
            }
        };

        //  Database
        contactsAppDatabase = Room.databaseBuilder(
                getApplicationContext(),
                ContactsAppDatabase.class,
                "ContactDB")
                        .addCallback(myCallBack)
                                .build();

        //display all contacts from database
        DisplayAllContactInBackground();



        //Recycler View
        recyclerView = findViewById(R.id.recycler_view_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new MyAdapter(contacts, this, MainActivity.this);
        recyclerView.setAdapter(adapter);

        button = (FloatingActionButton) findViewById(R.id.fab);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAndEditContacts(false, null, -1);
            }
        });

    }

    public void addAndEditContacts(final boolean isUpdated, final Contact contact, final int pos) {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.layout_add_contact, null);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setView(view);

        TextView titleView = view.findViewById(R.id.new_contact_title);
        final EditText newContact = view.findViewById(R.id.name);
        final EditText contactEmail = view.findViewById(R.id.email);



        titleView.setText(!isUpdated ? "Create Contact" : "Edit Contact");


        if(isUpdated && contact != null){
            newContact.setText(contact.getName());
            contactEmail.setText(contact.getEmail());
        }

        alertDialog.setCancelable(false)
                .setPositiveButton(isUpdated ? "Update" : "Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(isUpdated){
                            DeleteContact(contact, pos);
                        }else {
                            dialogInterface.cancel();
                        }
                    }
                });

        final AlertDialog alertDialog1 = alertDialog.create();
        alertDialog1.show();

        alertDialog1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(newContact.getText().toString())){
                    Toast.makeText(MainActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();

                    return;
                }else{
                    alertDialog1.dismiss();
                }

                if(isUpdated && contact != null){
                    updateContact(newContact.getText().toString(), contactEmail.getText().toString(), pos);
                }else{
                    createContact(newContact.getText().toString(), contactEmail.getText().toString());
                }


            }
        });

    }

    public void updateContact(String name, String email, int pos){
        Contact contact = contacts.get(pos);

        contact.setEmail(email);
        contact.setName(name);

        contactsAppDatabase.getContactDAO().updateContact(contact);
        contacts.set(pos, contact);
        adapter.notifyDataSetChanged();
    }

    public void DeleteContact(Contact contact, int pos){
        contactsAppDatabase.getContactDAO().deleteContact(contact);
        contacts.remove(pos);
        adapter.notifyDataSetChanged();
    }

    public void createContact(String name, String email){
        long id = contactsAppDatabase.getContactDAO().addContact(new Contact(name, email, 0));
        Contact contact = contactsAppDatabase.getContactDAO().getContact(id);
        if(contact != null) {
            contacts.add(0, contact);
            adapter.notifyDataSetChanged();
        }
    }

    public void DisplayAllContactInBackground(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                // Background Work
                contacts.addAll(contactsAppDatabase.getContactDAO().getContacts());
                // Executed after the background work had finished
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        });
    }


}