package com.example.naveedshah.mimicme3;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

public class RecyclerActivity extends Activity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ChatRoomContent myChatRoomContent;
    private ItemFragment myItemFragement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);

        // connect to the recycler view in recycler_view.xml
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        // list is the id for fragment_item_list.xml

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Create a new chat room content object
        myChatRoomContent = new ChatRoomContent();

        myItemFragement = new ItemFragment();
        myChatRoomContent.getData();
        // specify an adapter (see also next example)
        mAdapter = new MyItemRecyclerViewAdapter(myChatRoomContent.ITEMS, myItemFragement.mListener);
        // This is going to be another class
        mRecyclerView.setAdapter(mAdapter);
    }
}
