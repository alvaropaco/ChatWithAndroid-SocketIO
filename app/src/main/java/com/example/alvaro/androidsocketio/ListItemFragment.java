package com.example.alvaro.androidsocketio;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListItemFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String mParam3;
    private EditText mInputMessageView;
    private RecyclerView mMessagesView;
    private OnFragmentInteractionListener mListener;
    private List<User> mUsers = new ArrayList<User>();
    private ArrayList<Message> mMessages = new ArrayList<Message>();
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
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @param param3 Parameter 3.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListItemFragment newInstance(String param1, String param2) {
        ListItemFragment fragment = new ListItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        //args.putString(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }

    public ListItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle extras = getActivity().getIntent().getExtras();
        Bundle args = getArguments();

        socket.connect();

        socket.on("users", handleCheckins);
        socket.on("message", handleIncomingMessages);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mListener != null && keyCode == 66) {
            sendMessage();
        }
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new MessageAdapter( mMessages);
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

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        TextView userFrom = (TextView) view.findViewById(R.id.userFrom);

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    sendMessage();
                }
                return false;
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
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

    private void sendMessage(){
        String message = mInputMessageView.getText().toString().trim();
        mInputMessageView.setText("");

        Map mapA = new HashMap();
        if(this.author == null) {
            mapA.put("name", "Anonmy");
            mapA.put("nick", "anonmy");
        } else {
            mapA.put("name", author.getName());
            mapA.put("nick", author.getNickName());
        }

        JSONObject jAuthor = new JSONObject(mapA);

        addMessage(message, jAuthor);

        JSONObject sendText = new JSONObject();
        try{
            sendText.put("text", message);
            sendText.put("author", jAuthor);
            socket.emit("message", sendText);
        }catch(JSONException e){

        }
    }

    public void sendImage(String path)
    {
        JSONObject sendData = new JSONObject();
        try{
            sendData.put("image", encodeImage(path));
            Bitmap bmp = decodeImage(sendData.getString("image"));
            addImage(bmp);
            socket.emit("message",sendData);
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

    private void addMessage(String message, JSONObject author) {
        if(null == message) return;
        if (message.equals("")) return;
        if (message.equalsIgnoreCase("\\s+")) return;

        mMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
                .author(message, author).build());

        mAdapter = new MessageAdapter(mMessages);

        mAdapter.notifyItemRangeChanged(0, mMessages.size());

        scrollToBottom();
    }

    private void addMessage(String message) {
        if(null == message) return;
        if (message.equals("")) return;
        if (message.equalsIgnoreCase("\\s+")) return;

        mMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
                .message(message).build());

        mAdapter = new MessageAdapter(mMessages);

        mAdapter.notifyItemRangeChanged(0, mMessages.size());

        scrollToBottom();
    }

    private void addImage(Bitmap bmp){
        mMessages.add(new Message.Builder(Message.TYPE_IMAGE)
                .image(bmp).build());
        mAdapter = new MessageAdapter( mMessages);
        mAdapter.notifyItemRangeChanged(0,mMessages.size());
        scrollToBottom();
    }
    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private String encodeImage(String path)
    {
        File imagefile = new File(path);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(imagefile);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        //Base64.de
        return encImage;

    }

    private Bitmap decodeImage(String data)
    {
        byte[] b = Base64.decode(data,Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(b,0,b.length);
        return bmp;
    }

    private Emitter.Listener handleCheckins = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Usuário conectado");
                    sendNotifications();
                    /*String message;
                    String imageText;
                    try {
                        message = data.getString("text").toString();
                        addMessage(message,(JSONObject) data.getJSONObject("author"));

                    } catch (JSONException e) {
                        // return;
                    }
                    try {
                        imageText = data.getString("image");
                        addImage(decodeImage(imageText));
                    } catch (JSONException e) {
                        //retur
                    }*/
                }
            });
        }
    };

    private Emitter.Listener handleIncomingMessages = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    String imageText;
                    try {
                        message = data.getString("text").toString();
                        addMessage(message,(JSONObject) data.getJSONObject("author"));
                        /*if (jAuthor != null) {
                            addMessage(message, jAuthor);
                            return;
                        }
                        addMessage(message);*/
                    } catch (JSONException e) {
                        // return;
                    }
                    try {
                        imageText = data.getString("image");
                        addImage(decodeImage(imageText));
                    } catch (JSONException e) {
                        //retur
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

    public void setUser(User user) {
        this.author = user;
        checkin();
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
