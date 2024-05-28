package in.sixconbao.merge.game2048.GameCode;

import android.content.SharedPreferences;


public final class Scores {


    private static final String TOP_SCORE = "TopScore";

    private long score, prevScore;
    private Long TopScore, prevTopScore;
    private final SharedPreferences prefs;
    private final String rowsString, colsString, gameModeString;
    private boolean newHighScore;

    public Scores(Long score, SharedPreferences prefs, int gameMode, int rows, int cols) {

        this.score = score;
        this.prefs = prefs;
        this.gameModeString = Integer.toString(gameMode);
        this.rowsString = Integer.toString(rows);
        this.colsString = Integer.toString(cols);
        this.TopScore = prefs.getLong(TOP_SCORE + gameModeString + rowsString + colsString, 0);
        this.prevTopScore = TopScore;
    }

    //Kiểm tra điểm cao nhất hiện tại để hiện thị lên khu vực chơi
    public void checkTopScore() {
        if (!newHighScore) {
            TopScore = prefs.getLong(TOP_SCORE + gameModeString + rowsString + colsString, 0);
            if (score > TopScore) {
                newHighScore = true;
                prevTopScore = TopScore;
                TopScore = score;
            }
        }
    }
    //Cập nhật bảng điểm (hàm được gọi mỗi khi có điểm cao mới)
    public void updateScoreBoard() {

        prefs.edit().putLong(TOP_SCORE + gameModeString + rowsString + colsString, TopScore).apply();
    }

    //Sau mỗi thao tác thực hiện trên khu vực bảng chơi sẽ cập nhật điểm số
    public void updateScore(Long value) {
        prevScore = score;
        score = value;
        if (!newHighScore)
            checkTopScore();
        else {
            prevTopScore = TopScore;
            TopScore = score;
        }

    }
    //Cập nhật trạng thái điểm hiện tại của màn chơi
    public void refreshScoreBoard() {
        TopScore = prefs.getLong(TOP_SCORE + gameModeString + rowsString + colsString, 0);
    }
    //Khi người dùng undo thao tác trên bảng chơi thì cập nhật lại điểm
    public void undoScore() {
        score = prevScore;
        TopScore = prevTopScore;
        if (score < TopScore) {
            newHighScore = false;
            updateScoreBoard();
        }
    }

    public Long getTopScore() {
        return TopScore;
    }

    public Long getScore() {
        return score;
    }

    public boolean isNewHighScore() {
        return newHighScore;
    }

    public void resetGame() {
        score = 0;
        refreshScoreBoard();
        newHighScore = false;
    }


}

