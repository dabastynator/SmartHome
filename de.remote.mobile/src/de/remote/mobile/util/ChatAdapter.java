package de.remote.mobile.util;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.remote.mobile.R;
import de.remote.mobile.activies.ChatActivity.Message;

public class ChatAdapter extends ArrayAdapter<Message>{

	private Message[] messages;

	public ChatAdapter(Context context, List<Message> messages) {
		super(context, R.layout.chat_row, messages.toArray(new Message[]{}));
		this.messages = messages.toArray(new Message[]{});
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	     View row = convertView;
	     ChatHolder holder = null;
	        
	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
	            row = inflater.inflate(R.layout.chat_row, parent, false);
	            
	            holder = new ChatHolder();
	            holder.lbl_chat_name = (TextView)row.findViewById(R.id.lbl_chat_name);
	            holder.lbl_chat_text = (TextView)row.findViewById(R.id.lbl_chat_text);
	            
	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (ChatHolder)row.getTag();
	        }
	        
	        Message msg = messages[position];
	        holder.lbl_chat_name.setText(msg.author);
	        holder.lbl_chat_text.setText(msg.message);
	        
	        return row;
	}
	
    static class ChatHolder
    {
    	TextView lbl_chat_name;
        TextView lbl_chat_text;
    }
	
}
