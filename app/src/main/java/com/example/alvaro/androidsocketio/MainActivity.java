package com.example.alvaro.androidsocketio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public EditText userName;
    private List<User> mContacts = new ArrayList<User>();
    private List<User> contactsList = new ArrayList<User>();

    private RecyclerView mContactsView;
    private RecyclerView.Adapter mAdapter;

    private Socket socket;
    private User author;

    {
        try{
            socket = IO.socket("http://192.168.56.1:3000");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        onViewCreated(findViewById(R.id.contacts), savedInstanceState);

        onAttach(MainActivity.this);

        socket.connect();
        socket.on("users", handleContacts);
        if(socket.connected()) {
            Random gerador = new Random();
            User u = new User();
            u.setuName("Anonym_" + gerador.nextLong());
            u.setuNickName("anonym_" + gerador.nextLong());
            u.setuId(socket.id());
            this.author = u;
            contactsList.add(u);
            JSONArray jsArray = new JSONArray();
            JSONObject jO = new JSONObject();
            try {
                jO.put("name", u.getName());
                jO.put("nickname", u.getNickName());
                jO.put("id", u.getuId());
                jsArray.put(jO);
                socket.emit("users", jsArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        mContactsView = (RecyclerView) view.findViewById(R.id.contacts);
        mContactsView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mContactsView.setAdapter(mAdapter);
    }

    public void startChat(View view)
    {
        Intent intent = new Intent(MainActivity.this, SocketActivity.class);
        intent.putExtra("userName", "Anonym");
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAttach(Activity activity) {
        mAdapter = new ContactAdapter(contactsList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mContactsView.setLayoutManager(llm);
        mContactsView.setAdapter( mAdapter );
    }

    private Emitter.Listener handleContacts = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            Activity a = MainActivity.this;
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args[0] instanceof JSONArray) {
                        JSONArray data = (JSONArray) args[0];

                        contactsList.clear();
                        try {
                            int len = data.length();
                            for (int i = 0; i < len; i++) {
                                System.out.println(data.toString());
                                JSONArray oUsr = (JSONArray) data.get(i);
                                User u = new User();
                                u.setuId(oUsr.getString(2));
                                u.setuName(oUsr.getString(0));
                                u.setuNickName(oUsr.getString(1));
                                contactsList.add(u);
                                mAdapter.notifyItemRangeChanged(0, contactsList.size());
                            }
                        } catch (JSONException e) {
                            // return;
                        }
                    }
                }
            });
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        contactsList.remove(this.author);
        mAdapter.notifyItemMoved(0, (contactsList.size() - 1));
        socket.emit("disconnect", contactsList);
        socket.disconnect();
    }
}
