package de.ignis.simplechat.chat;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.ignis.simplechat.R;
import de.ignis.simplechat.home.HomeActivity;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, ChildEventListener {

    public FirebaseDatabase database;
    public DatabaseReference chatRef;

    private LinearLayout messagesLayout;

    private FloatingActionButton sendButton;
    private MultiAutoCompleteTextView enteredText;
    private ScrollView scrollView;
    private RelativeLayout mainRl;

    private ArrayList<ChatMessage> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setTitle("TestChat");

        database = FirebaseDatabase.getInstance();
        chatRef = database.getReference("test_chat");
        chatRef.addChildEventListener(this);
        chatRef.keepSynced(true);

        sendButton = (FloatingActionButton) findViewById(R.id.sendButton);
        enteredText = (MultiAutoCompleteTextView) findViewById(R.id.enteredText);
        sendButton.setOnClickListener(this);

        messagesLayout = (LinearLayout) findViewById(R.id.linearLayoutMessages);
        messages = new ArrayList<>();

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        mainRl = (RelativeLayout) findViewById(R.id.activity_chat);
        mainRl.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view == sendButton){
            if(!enteredText.getText().toString().trim().equals(""))
                sendMessage();
        }
    }

    public void sendMessage(){
        chatRef.push().setValue(new ChatMessage(HomeActivity.user_name, enteredText.getText().toString().trim()));
        enteredText.setText("");
    }

    public void addMessage(String name, String message, String key){
        LayoutInflater inflater = LayoutInflater.from(this);
        RelativeLayout message_layout;
        if(name.equals(HomeActivity.user_name)){
            message_layout = (RelativeLayout) inflater.inflate(R.layout.message_view_from_user, null);
        }else{
            message_layout = (RelativeLayout) inflater.inflate(R.layout.message_view, null);
        }
        ChatMessage chatMessage = new ChatMessage(name, message, message_layout, key);
        messages.add(chatMessage);
        messagesLayout.addView(chatMessage.getUiLayout());
    }

    public void changeMessage(String name, String message, String key){
        for(Object chatMessage : messages.toArray()){
            if(((ChatMessage)chatMessage).getKey().equals(key)){
                ((ChatMessage)chatMessage).change(name, message);
            }
        }
    }

    public void removeMessage(String key){
        for(Object chatMessage : messages.toArray()){
            if(((ChatMessage)chatMessage).getKey().equals(key)){
                messagesLayout.removeView(((ChatMessage)chatMessage).getUiLayout());
                messages.remove(chatMessage);
                ((ChatMessage)chatMessage).dispose();
            }
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        String name = dataSnapshot.child("name").getValue(String.class);
        String text = dataSnapshot.child("message").getValue(String.class);
        addMessage(name, text, dataSnapshot.getKey());
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        String name = dataSnapshot.child("name").getValue(String.class);
        String text = dataSnapshot.child("message").getValue(String.class);
        changeMessage(name, text, dataSnapshot.getKey());
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        removeMessage(dataSnapshot.getKey());
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        removeMessage(dataSnapshot.getKey());
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {}
}
