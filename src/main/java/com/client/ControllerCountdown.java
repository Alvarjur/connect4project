package com.client;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class ControllerCountdown {

    @FXML private Label labelCountdown, labelPlayer1, labelPlayer2;
    
    private final int startSeconds = 3;

    private int remainingSeconds;

    private Timeline timeline;

    public void startCountdown() {
        stopCountdownIfRunning();
        remainingSeconds = startSeconds;
        updateLabelCountdown(remainingSeconds);

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            remainingSeconds--;
            updateLabelCountdown(remainingSeconds);
            if (remainingSeconds <= 0) {
                stopCountdownIfRunning();
                try {
                    Main.setViewGame();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

        // se ejecuta startSeconds veces (por ejemplo: 3 -> se ejecuta 3 veces)
        timeline.setCycleCount(startSeconds);
        timeline.play();
    }

    public void stopCountdownIfRunning() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    public void resetState() {
        stopCountdownIfRunning();
        remainingSeconds = startSeconds;
        updateLabelCountdown(startSeconds);
    }

    private void updateLabelCountdown(int value) {
        if (labelCountdown != null) {
            labelCountdown.setText(String.valueOf(value));
        }
    }

    public void setPlayerLabels(String player_1, String player_2) {
        labelPlayer1.setText(player_1);
        labelPlayer2.setText(player_2);
    }
}
