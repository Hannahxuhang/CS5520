package edu.neu.madcourse.hangxu.model;

import java.io.Serializable;

public class Game implements Serializable {

    private String userName;
    private int score;
    private int topScore;

    public Game() {

    }

    public Game(String userName, int score, int topScore) {
        this.userName = userName;
        this.score = score;
        this.topScore = topScore;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setTopScore(int topScore) {
        this.topScore = topScore;
    }

    public int getTopScore() {
        return topScore;
    }

    public void updateTopScore(int score) {
        if (score > topScore) {
            topScore = score;
        }
    }
}
