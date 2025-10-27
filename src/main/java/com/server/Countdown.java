package com.server;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Countdown {

    private final int startSeconds = 3;

    private int remainingSeconds;

    private Timeline timeline;

    public void startCountdown() {
        remainingSeconds = startSeconds;
        sendCurrentCountdownToClients();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            remainingSeconds--;
            sendCurrentCountdownToClients();
            if (remainingSeconds <= 0) {
                try {
                    sendEndOfCountdownToClients();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

        // se ejecuta startSeconds veces (por ejemplo: 3 -> se ejecuta 3 veces)
        timeline.setCycleCount(startSeconds);
        timeline.play();
    }

    public void sendCurrentCountdownToClients() {

    }

    public void sendEndOfCountdownToClients() {

    }
}
