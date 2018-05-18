package com.example.naveedshah.mimicme3;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.Button;
import android.widget.EditText;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.database.DataSetObserver;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URI;
import java.net.URISyntaxException;


public class ChatRoomActivity extends Activity {
    WebSocketClient mWebSocketClient;
    private ChatArrayAdapter adp;
    private ListView list;
    private EditText chatText;
    private Button send;
    private String roomNumber = null;
    private Integer userId = null;
    private String username = null;
    private boolean side = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectWebSocket();
        setContentView(R.layout.chat_room_activity);

        // Retrieve the userID, username, and room number saved in the Shared Preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userId = preferences.getInt("uid",0);
        username = preferences.getString("username",null);
        roomNumber = preferences.getString("room",null);

        // The send button
        send = (Button) findViewById(R.id.btn);

        list = (ListView) findViewById(R.id.listview);

        // Initiate a new ChatArrayAdapter
        adp = new ChatArrayAdapter(getApplicationContext(), R.layout.chat_room_activity);
        list.setAdapter(adp);
        chatText = (EditText) findViewById(R.id.chat_text);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setAdapter(adp);
        adp.registerDataSetObserver(new DataSetObserver() {
            public void OnChanged(){
                super.onChanged();
                list.setSelection(adp.getCount() -1);
            }
        });
    }

    private boolean sendChatMessage() {
        side = false;

        // On a thread run the command to add the message to the recycler view containing all the messages
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // get the message that the user has typed and add to the chat array adapter
                adp.add(new ChatMessage(side, chatText.getText().toString()));

            }
        });

        // Check if the socket got closed and if it did, reconnect
        if (mWebSocketClient.isClosed()) {

            mWebSocketClient.reconnect();
        }

        // create a JSON object containing the new message from the user that will be given to the server
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command", "send");
            jsonObject.put("room", roomNumber);
            jsonObject.put("message", chatText.getText().toString());
            jsonObject.put("username", username);
            jsonObject.put("uid", userId);

        } catch (JSONException e) {
            // catch any JSON exception that may be made
            e.printStackTrace();
        }

        // convert the JSON object to a string and send to the server
        mWebSocketClient.send(jsonObject.toString());

        // reset the chat text (area where user types)
        chatText.setText("");

        return true;
    }

    // Connects the websocket
    private void connectWebSocket() {

        new Thread() {

            public void run() {

                URI uri;
                try {
                    // URL to the socket
                    uri = new URI("ws://159.65.38.56:8000/socket");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return;
                }

                // Instantiate the websocket client with the URL to the socket
                mWebSocketClient = new WebSocketClient(uri) {
                    @Override
                    public void onOpen(ServerHandshake serverHandshake) {

                        // Create a JSON object to make a "join" request to the server to join the roomNumber that the user chose
                        JSONObject jsonObject2 = new JSONObject();
                        try {
                            jsonObject2.put("command", "join");
                            jsonObject2.put("room", roomNumber);
                            jsonObject2.put("username", username);
                            jsonObject2.put("uid", userId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Send to the backend
                        mWebSocketClient.send(jsonObject2.toString());

                    }

                    // Upon receiving a message from the socket this function will run this
                    @Override
                    public void onMessage(final String s) {

                        // This will change the side to the left of the screen
                        side = true;

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                // The server returns the message and the username that sent it. This attempts to attain that information
                                try {
                                    JSONObject json_obj=new JSONObject(s.toString());
                                    String value1=json_obj.getString("username");
                                    if (!value1.equals(username)) {
                                        // If the username associated with the message is not the current user, add that message to the
                                        // recycler view.
                                        String value2=json_obj.getString("message");
                                        adp.add(new ChatMessage(side,value2));
                                    }

                                } catch (JSONException e) {
                                    // catch any JSON exceptions
                                    Log.d("JSON Exception: ", e.toString());
                                }

                            }
                        });

                    }

                    @Override
                    public void onClose(int i, String s, boolean b) {
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                };
                // connect the websocket
                mWebSocketClient.connect();

            }
        }.start(); // Start the thread
    }
}