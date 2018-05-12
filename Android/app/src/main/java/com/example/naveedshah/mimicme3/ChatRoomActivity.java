package com.example.naveedshah.mimicme3;
import android.app.Activity;
import android.widget.ListView;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import android.os.Bundle;
import android.content.DialogInterface.OnKeyListener;
import android.view.View;
import android.view.KeyEvent;
import android.widget.AbsListView;
import android.database.DataSetObserver;


public class ChatRoomActivity extends Activity {
    private static final String TAG = "ChatActivity";
    private ChatArrayAdapter adp;
    private ListView list;
    private EditText chatText;
    private Button send;
    Intent intent;
    private boolean side = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        setContentView(R.layout.chat_room_activity);
        send = (Button) findViewById(R.id.btn);
        list = (ListView) findViewById(R.id.listview);
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
    private boolean sendChatMessage(){
        adp.add(new ChatMessage(side, chatText.getText().toString()));
        chatText.setText("");
        side = !side;
        return true;
    }
}