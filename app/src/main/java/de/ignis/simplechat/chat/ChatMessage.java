package de.ignis.simplechat.chat;

import android.widget.RelativeLayout;
import android.widget.TextView;

import de.ignis.simplechat.R;

/**
 * Created by Janik on 11.10.2016.
 */

public class ChatMessage {

    public String name;
    public String message;
    private String key;
    private RelativeLayout uiLayout;

    public ChatMessage() {
    }

    public ChatMessage(String name, String message) {

        this.name = name;
        this.message = message;
    }

    public ChatMessage(String name, String message, RelativeLayout uiLayout, String key) {

        this.name = name;
        this.message = message;
        this.uiLayout = uiLayout;
        this.key = key;

        TextView message_text = (TextView) uiLayout.findViewById(R.id.message_text);
        TextView message_name = (TextView) uiLayout.findViewById(R.id.message_user_name);
        message_text.setText(message);
        message_name.setText(name);
    }

    public void change(String name, String message){
        TextView message_text = (TextView) uiLayout.findViewById(R.id.message_text);
        TextView message_name = (TextView) uiLayout.findViewById(R.id.message_user_name);
        message_text.setText(message);
        message_name.setText(name);
    }

    public void dispose(){
        name = null;
        message = null;
        key = null;
        uiLayout = null;
    }

    public String getKey(){
        return key;
    }

    public void setKey(String id){
        this.key = id;
    }

    public RelativeLayout getUiLayout() {
        return uiLayout;
    }

    public void setUiLayout(RelativeLayout uiLayout) {
        this.uiLayout = uiLayout;
    }
}
