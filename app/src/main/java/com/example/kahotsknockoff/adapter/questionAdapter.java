package com.example.kahotsknockoff.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kahotsknockoff.GSON.Question;
import com.example.kahotsknockoff.MainActivity;
import com.example.kahotsknockoff.R;

import java.util.ArrayList;

public class questionAdapter extends RecyclerView.Adapter<questionViewHolder> {
    private ArrayList<Question> questions;
    private Context mContext;

    public questionAdapter(ArrayList<Question> questions, Context mContext) {
        this.questions = questions;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public questionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_question_list,viewGroup,false);
        questionViewHolder holder = new questionViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull questionViewHolder questionViewHolder, int i) {
        questionViewHolder.questionTxt.setText(this.questions.get(i).getId()+". "+this.questions.get(i).getQuestionStr());
        questionViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MainActivity.class);
                Question q = questions.get(i);
                intent.putExtra("question",q);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.questions.size();
    }
}
