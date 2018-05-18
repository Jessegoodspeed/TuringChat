package com.example.naveedshah.mimicme3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.*;


public class ChatRoomContent {

    // The ITEMS list will store the ChatRoom Items created from data received from the server
    public static List<ChatRoom> ITEMS = new ArrayList<ChatRoom>();

    // Function to add a ChatRoom object to the ITEMS list
    private static void addItem(ChatRoom item) {
        ITEMS.add(item);
    }

    // This function fetches the list of chat rooms from the sever to populate the ITEMS list
    public static void getData() {
        // make json call here

        new Thread() {
            public void run() {
                HttpURLConnection conn = null;
                try {

                    // The link to fetch the chat rooms from the server
                    URL url = new URL("http://159.65.38.56:8000/chat/rooms");

                    conn = (HttpURLConnection) url.openConnection();

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                    StringBuilder sb = new StringBuilder();
                    String line;

                    // Obtain and record response from server
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();

                    try {

                        // convert the data received from the server into a JSONArray in order to be able to
                        // iterate through it
                        JSONArray json = new JSONArray(sb.toString());

                        // Iterate through the JSON array and create a new Chat Room object using the id and name
                        // provided by the server
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject row = json.getJSONObject(i);
                            addItem(createChatRoom(row.getInt("id"), row.getString("name")));
                        }

                    } catch (JSONException e) {

                    }

                } catch (IOException e) {

            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            }
        }.start(); // Start the thread

    }

    private static ChatRoom createChatRoom(int id, String name) {
        // Given an id and name, this initiates a new ChatRoom object and returns it
        return new ChatRoom(String.valueOf(id), name, makeDetails(id));
    }

    // This function creates details for the chatroom. As of now it adds a generic
    // line for details. This is something that can be improved on in the future
    // if the server returns back extra details about the Chatroom, it can be used
    // to add those details to the Chatroom object.
    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore detailed information here.");
        }
        return builder.toString();
    }

    public static class ChatRoom {
        public final String id;
        public final String content;
        public final String details;

        public ChatRoom(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
