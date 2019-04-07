package com.example.kahotsknockoff.GSON;

import java.util.ArrayList;

public class PostAnswer {

    private Long questionNo;
    private char choiceOption;
    private String comment;

    public Long getQuestionNo() {
        return questionNo;
    }

    public void setQuestionNo(Long questionNo) {
        this.questionNo = questionNo;
    }

    public char getChoiceOption() {
        return choiceOption;
    }

    public void setChoiceOption(char choiceOption) {
        this.choiceOption = choiceOption;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
