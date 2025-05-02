package com.example.goalandgoals.Fragment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.R;
import com.example.goalandgoals.Utils.AppDatabase;
import com.example.goalandgoals.TaskViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PomodoroFragment extends Fragment {

    private Spinner spinnerTasks;
    private EditText etDuration;
    private Button btnConfirm;
    private TextView tvTitle, tvSubtitle, timerText;
    private MaterialButton btnStartPause, btnSkip;

    private EditText etWorkDurationDialog, etBreakDurationDialog;
    private ToDoModel selectedTask;
    private TaskViewModel taskViewModel;
    private AppDatabase db;
    private CountDownTimer countDownTimer;
    private boolean timerRunning = false;
    private long timeLeftInMillis;
    private long customWorkDuration;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "pomodoro_channel";
    private enum TimerPhase { WORK, BREAK }
    private TimerPhase currentPhase = TimerPhase.WORK;
    private long customBreakDuration = 5 * 60 * 1000L;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pomodoro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etDuration = view.findViewById(R.id.et_duration);
        super.onViewCreated(view, savedInstanceState);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        db = AppDatabase.getInstance(requireContext());
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        spinnerTasks = view.findViewById(R.id.spinner_tasks);
        etDuration = view.findViewById(R.id.et_duration);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        tvTitle = view.findViewById(R.id.tv_title);
        tvSubtitle = view.findViewById(R.id.tv_subtitle);
        timerText = view.findViewById(R.id.timer_text);
        btnStartPause = view.findViewById(R.id.btn_start_pause);
        btnSkip = view.findViewById(R.id.btn_skip);

        setupTaskSelector();
        setupConfirmButton();
        setupTimerControls();
        toggleTimerVisibility(false);
    }

    private void setupTaskSelector() {
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            List<ToDoModel> incompleteTasks = new ArrayList<>();
            if (tasks != null) {
                for (ToDoModel task : tasks) {
                    if (task.getStatus() == 0) {
                        incompleteTasks.add(task);
                    }
                }
            }

            if (!incompleteTasks.isEmpty()) {

                ArrayAdapter<ToDoModel> adapter = new ArrayAdapter<ToDoModel>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        incompleteTasks
                ) {
                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView textView = (TextView) super.getView(position, convertView, parent);
                        textView.setText(incompleteTasks.get(position).getTask()); // 设置显示的文本
                        return textView;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                        textView.setText(incompleteTasks.get(position).getTask()); // 下拉项同样设置
                        return textView;
                    }
                };

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTasks.setAdapter(adapter);
            } else {
                Toast.makeText(requireContext(), "No incomplete tasks found", Toast.LENGTH_LONG).show();
            }
        });

        spinnerTasks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTask = (ToDoModel) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTask = null;
            }
        });
    }

    private void setupConfirmButton() {
        btnConfirm.setOnClickListener(v -> {
            // 只验证初始工作时间
            String durationInput = etDuration.getText().toString();
            if (durationInput.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter work duration", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int minutes = Integer.parseInt(durationInput);
                if (minutes <= 0) {
                    Toast.makeText(requireContext(), "Duration must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                customWorkDuration = minutes * 60 * 1000L;
                // 保持默认休息时间
                if (customBreakDuration <= 0) {
                    customBreakDuration = 5 * 60 * 1000L;
                }

                toggleSelectionVisibility(false);
                toggleTimerVisibility(true);
                initializeTimer();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid duration", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupTimerControls() {
        btnStartPause.setOnClickListener(v -> toggleTimer());
        btnSkip.setOnClickListener(v -> skipPhase());
    }

    private void toggleTimer() {
        if (timerRunning) pauseTimer();
        else startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                handleTimerCompletion();
            }
        }.start();

        timerRunning = true;
        updateButtonStates();
    }

    private void pauseTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        timerRunning = false;
        updateButtonStates();
    }

    private void skipPhase() {
        pauseTimer();
        if (currentPhase == TimerPhase.WORK) handlePhaseEnd();
        else {
            stopAlarm();
            startWorkPhase();
        }
    }

    private void initializeTimer() {
        if (currentPhase == TimerPhase.WORK) startWorkPhase();
        else startBreakPhase();
    }

    private void playAlarm() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(requireContext(), android.provider.Settings.System.DEFAULT_RINGTONE_URI);
            mediaPlayer.setLooping(true);
        }
        mediaPlayer.start();
    }

    private void handlePhaseEnd() {
        playAlarm();
        if (currentPhase == TimerPhase.WORK) showWorkCompletionDialog();
        else showBreakCompletionDialog();
    }

    private void showBreakCompletionDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Break Time Ended")
                .setMessage("Ready to start working?")
                .setPositiveButton("Continue", (d, w) -> {
                    stopAlarm();
                    startWorkPhase();
                })
                .setCancelable(false)
                .show();
    }

    private void showWorkCompletionDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Work Time Ended")
                .setMessage("Continue?")
                .setPositiveButton("Continue", (d, w) -> {
                    stopAlarm();
                    showTimeSettingsDialog(true);
                })
                .setNegativeButton("Complete", (d, w) -> completeTask())
                .setCancelable(false)
                .show();
    }

    private void startWorkPhase() {
        if (customWorkDuration <= 0) {
            Toast.makeText(requireContext(), "Work duration not set", Toast.LENGTH_SHORT).show();
            return;
        }
        currentPhase = TimerPhase.WORK;
        timeLeftInMillis = customWorkDuration;
        updateTitle("Focus Time", "Start working!");
        timerText.setTextColor(ContextCompat.getColor(requireContext(), R.color.work_primary));
        startTimer();
    }


    private void startBreakPhase() {
        if (customBreakDuration <= 0) {
            Toast.makeText(requireContext(), "Break duration not set", Toast.LENGTH_SHORT).show();
            return;
        }
        currentPhase = TimerPhase.BREAK;
        timeLeftInMillis = customBreakDuration;
        updateTitle("Break Time", "Relax~");
        timerText.setTextColor(ContextCompat.getColor(requireContext(), R.color.break_primary));
        startTimer();
    }

    private void updateTitle(String title, String subtitle) {
        tvTitle.setText(title);
        tvSubtitle.setText(subtitle);
    }

    private void updateTimerDisplay() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        timerText.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateButtonStates() {
        btnStartPause.setText(timerRunning ? "Pause" : "Start");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pomodoro Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alerts for pomodoro completion");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendCompletionNotification() {
        String title = currentPhase == TimerPhase.WORK ? "⏰ Work Time Ended" : "☕ Break Time Ended";
        String content = currentPhase == TimerPhase.WORK ? "Time to take a break!" : "Ready to continue working?";

        Notification notification = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1, notification);
    }

    private void handleTimerCompletion() {
        sendCompletionNotification();
        handlePhaseEnd();
    }

    private void showTimeSettingsDialog(boolean isMandatory) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_time_settings, null);

        // 绑定对话框控件
        EditText etWork = dialogView.findViewById(R.id.et_work_duration);
        EditText etBreak = dialogView.findViewById(R.id.et_break_duration);

        // 设置默认值
        etWork.setText(String.valueOf(customWorkDuration / 60000));
        etBreak.setText(String.valueOf(customBreakDuration / 60000));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Set Next Session")
                .setView(dialogView)
                .setPositiveButton("Continue", (d, w) -> {
                    try {
                        int newWork = Integer.parseInt(etWork.getText().toString());
                        int newBreak = Integer.parseInt(etBreak.getText().toString());

                        if (newWork <= 0 || newBreak <= 0) {
                            Toast.makeText(requireContext(),
                                    "Durations must be greater than 0",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        customWorkDuration = newWork * 60000L;
                        customBreakDuration = newBreak * 60000L;
                        startBreakPhase();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(),
                                "Invalid number format",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(isMandatory ? "Exit" : "Cancel", null)
                .setCancelable(false)
                .create();

        dialog.show();
    }

    private void completeTask() {
        if (selectedTask != null) {
            new Thread(() -> {
                db.toDoDao().updateStatus(selectedTask.getId(), 1);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Task completed!", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                });
            }).start();
        }
    }

    private void toggleSelectionVisibility(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        spinnerTasks.setVisibility(visibility);
        etDuration.setVisibility(visibility);
        btnConfirm.setVisibility(visibility);
    }

    private void toggleTimerVisibility(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        tvTitle.setVisibility(visibility);
        tvSubtitle.setVisibility(visibility);
        timerText.setVisibility(visibility);
        btnStartPause.setVisibility(visibility);
        btnSkip.setVisibility(visibility);
    }

    private void stopAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}