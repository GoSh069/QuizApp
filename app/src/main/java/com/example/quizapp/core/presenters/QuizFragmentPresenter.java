package com.example.quizapp.core.presenters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.example.quizapp.api.Api;
import com.example.quizapp.core.constracts.QuizFragmentContract;
import com.example.quizapp.model.Question;
import com.example.quizapp.model.Quiz;
import com.example.quizapp.model.database.Score;
import com.example.quizapp.model.database.ScoreDbService;
import com.example.quizapp.utilities.ScoresUtility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

public class QuizFragmentPresenter implements QuizFragmentContract.PresenterListener {

    private List<Question> questionList = new ArrayList<>();
    private QuizFragmentContract.ViewListener viewListener;
    private int index = 0;
    private int counter = 0;
    private int currentQuizCategory;
    private int quizCategory = 12;
    private int checkedId;

    private ScoreDbService service;


    @Override
    public void setViewListener(QuizFragmentContract.ViewListener viewListener, Context context) {
        this.viewListener = viewListener;
    }

    public int getCheckedId() {
        return checkedId;
    }

    @Override
    public void setCategory(int category) {
        this.quizCategory = category;
    }

    @Override
    public void setScoreDbService(ScoreDbService service) {
        this.service = service;
    }


    public void getQuiz(int category, Context context) {
        Api.getInstance().getQuiz(category, new Api.ApiListener() {
            @Override
            public void onQuizReceived(Quiz quiz) {
                questionList = quiz.getQuestions();
                setQuestionsInView(context);
            }

            @Override
            public void onFailure() {
                Toast.makeText(context, "Couldn't retrieve data from Internet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void getNextQuestion(Context context) {
        if (index == 10 || index == 0 || currentQuizCategory != quizCategory) {
            index = 0;
            getQuiz(quizCategory, context);
            currentQuizCategory = quizCategory;
        } else
            setQuestionsInView(context);

    }

    @Override
    public void submitAnswer(String submittedAnswer) {
        if (submittedAnswer.equals(questionList.get(index - 1).getCorrectAnswer())) {
            counter++;
        } else {
            if (counter != 0) {
                saveScore();
            }
            counter = 0;
        }
        viewListener.setCorrectAnswerStreakCounterAndShowRightAnswer(counter, checkedId);
    }

    @Override
    public void radioGroupChecked(int checkedId) {
        this.checkedId = checkedId;
        viewListener.unableRadioGroup();
    }

    private void setQuestionsInView(Context context) {
        if (questionList.isEmpty()) {
            Toast.makeText(context, "Category currently unavailable, please choose another.", Toast.LENGTH_LONG).show();
        } else {
            Question question = questionList.get(index);
            index++;
            List<String> allAnswers = question.getIncorrectAnswers();
            Random random = new Random();
            int pos = random.nextInt(allAnswers.size() + 1);
            allAnswers.add(pos, question.getCorrectAnswer());
            viewListener.setQuestions(question.getQuestion(), allAnswers, question.getCorrectAnswer());
        }
    }

    public void saveScore() {
        Score score = new Score(getFormattedDate(), counter);
        service.saveScore(score);
    }


    private String getFormattedDate() {
        Date date = Calendar.getInstance().getTime();
        return ScoresUtility.getSimpleDataFormatInstance().format(date);
    }
}
