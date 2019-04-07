package com.example.kahotsknockoff.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.kahotsknockoff.R;

import org.w3c.dom.Text;

public class questionViewHolder extends RecyclerView.ViewHolder {
    TextView questionTxt;

    public questionViewHolder(@NonNull View itemView) {
        super(itemView);
        questionTxt = (TextView) itemView.findViewById(R.id.question_string);
    }
}
