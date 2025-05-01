package com.example.goalandgoals;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class PomodoroActivity extends AppCompatActivity {

    // UI Components
    private TextView tvTitle, tvSubtitle, timerText;
    private Button btnStartPause;

    private com.google.android.material.button.MaterialButton btnSkip;

    // Timer Logic
    private CountDownTimer countDownTimer;
    private boolean timerRunning = false;
    private long timeLeftInMillis;
    private boolean isWorkPhase = true;

    // Time Constants
    private static final long WORK_DURATION = 25 * 60 * 1000; // 25 minutes
    private static final long BREAK_DURATION = 5 * 60 * 1000; // 5 minutes

    // Color Resources
    private final int colorWorkPrimary = R.color.work_primary;
    private final int colorWorkSecondary = R.color.work_secondary;
    private final int colorBreakPrimary = R.color.break_primary;
    private final int colorBreakSecondary = R.color.break_secondary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);
        setupUIComponents();
        initializeTimer();
    }

    private void setupUIComponents() {
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        timerText = findViewById(R.id.timer_text);
        btnStartPause = findViewById(R.id.btn_start_pause);
        btnSkip = findViewById(R.id.btn_skip);

        btnStartPause.setOnClickListener(v -> toggleTimer());
        btnSkip.setOnClickListener(v -> skipPhase());
    }

    private void initializeTimer() {
        isWorkPhase = true;
        timeLeftInMillis = WORK_DURATION;
        updateFullInterface();
    }

    private void toggleTimer() {
        if (timerRunning) {
            pauseTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                switchPhase();
            }
        }.start();

        timerRunning = true;
        updateButtonStates();
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerRunning = false;
        updateButtonStates();
    }

    private void skipPhase() {
        pauseTimer();
        switchPhase();
    }

    private void switchPhase() {
        isWorkPhase = !isWorkPhase;
        timeLeftInMillis = isWorkPhase ? WORK_DURATION : BREAK_DURATION;
        updateFullInterface();
    }

    private void updateFullInterface() {
        updateTitles();
        updateColorScheme();
        updateTimerDisplay();
        updateButtonStates();
    }

    private void updateTitles() {
        if (isWorkPhase) {
            tvTitle.setText(R.string.title_work);
            tvSubtitle.setText(R.string.subtitle_work);
        } else {
            tvTitle.setText(R.string.title_break);
            tvSubtitle.setText(R.string.subtitle_break);
        }
    }

    private void updateColorScheme() {
        int primaryColor = ContextCompat.getColor(this,
                isWorkPhase ? colorWorkPrimary : colorBreakPrimary);
        int secondaryColor = ContextCompat.getColor(this,
                isWorkPhase ? colorWorkSecondary : colorBreakSecondary);

        // Update timer text color
        timerText.setTextColor(primaryColor);

        // Update primary button
        btnStartPause.setBackgroundTintList(ColorStateList.valueOf(primaryColor));

        // Update secondary button
        btnSkip.setStrokeColor(ColorStateList.valueOf(primaryColor));
        btnSkip.setBackgroundTintList(ColorStateList.valueOf(secondaryColor));
    }

    private void updateTimerDisplay() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        timerText.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateButtonStates() {
        btnStartPause.setText(timerRunning ? R.string.btn_pause : R.string.btn_start);
        btnSkip.setVisibility(timerRunning ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}