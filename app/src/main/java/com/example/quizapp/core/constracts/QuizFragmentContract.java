package com.example.quizapp.core.constracts;

import java.util.List;

public interface QuizFragmentContract {

    interface ViewListener{
        void setQuestions(String question, List<String> answers);
    }

    interface PresenterListener{
        void setViewListener(ViewListener viewListener);

        void getNextQuestion();


    }
}
