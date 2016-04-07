package com.example.alvaro.androidsocketio;

import android.app.Activity;;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private List<User> contactsList = new ArrayList<User>();
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mContactsView;

    private Socket socket;
    private User author;

    {
        try{
            socket = IO.socket("http://192.168.56.1:3000");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
   /* *//**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     *//*
    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle extras = getActivity().getIntent().getExtras();
        Bundle args = getArguments();

        socket.connect();
        if(socket.connected()) {
            Random gerador = new Random();
            User u = new User();
            u.setuName("Anonym_" + gerador.nextLong());
            u.setuNickName("anonym_" + gerador.nextLong());
            contactsList.add(u);
            JSONArray jsArray = new JSONArray();
            for (User contact: contactsList) {
                JSONObject jO = new JSONObject();
                try {
                    jO.put("name", contact.getName());
                    jO.put("nickname", contact.getNickName());
                    jsArray.put(jO);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            socket.emit("users", jsArray);
        }
        socket.on("users", handleContacts);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.content_main, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new ContactAdapter((List<User>) mContactsView);
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContactsView = (RecyclerView) view.findViewById(R.id.contacts);
        mContactsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactsView.setAdapter(mAdapter);
    }

    private  void checkin(){
        Map mapA = new HashMap();

        if(this.author == null) {
            mapA.put("name", "Anonmy");
            mapA.put("nick", "anonmy");
        } else {
            mapA.put("name", author.getName());
            mapA.put("nick", author.getNickName());
        }

        JSONObject sendText = new JSONObject();
        try{
            sendText.put("user", mapA);
            socket.emit("users", sendText);
        }catch(JSONException e){

        }
    }

    public void sendNotifications(){
        if(socket.connected()){
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Intent pendingIntent = new Intent(getActivity(), SocketActivity.class);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getActivity())
                    .setSmallIcon(R.drawable.ic_action_attachment)
                    .setContentTitle("Socketdroid")
                    .setContentText(author.getNickName() + " se conectou.")
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                            // Lollipop settings
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_PROMO)
                    .setColor(getResources().getColor(R.color.colorPrimary));
            Context mContext = getActivity();
            NotificationManager notificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }

    private void scrollToBottom() {
        mContactsView.scrollToPosition(mAdapter.getItemCount() - 1);
    }


    private Emitter.Listener handleContacts = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    String imageText;
                    contactsList.clear();
                    try {
                        JSONArray list = (JSONArray) data.get("list");
                        int len = list.length();
                        for (int i = 0; i < len; i++) {
                            JSONObject oUsr = (JSONObject) list.get(i);
                            User u = new User();
                            u.setuName(oUsr.getString("name"));
                            u.setuNickName(oUsr.getString("nickname"));
                            contactsList.add(u);
                        }
                    } catch (JSONException e) {
                        // return;
                    }
                }
            });
        }
    };

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

}
