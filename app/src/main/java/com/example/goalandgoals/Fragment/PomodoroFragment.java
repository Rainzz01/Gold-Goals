package com.example.goalandgoals.Fragment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.PomodoroViewModel;
import com.example.goalandgoals.R;
import com.example.goalandgoals.TaskViewModel;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class PomodoroFragment extends Fragment {

    private static final String TAG = "PomodoroFragment";
    private Spinner spinnerTasks;
    private EditText etDuration;
    private Button btnConfirm;
    private TextView tvTitle, tvSubtitle, timerText;
    private MaterialButton btnStartPause, btnSkip;
    private PomodoroViewModel pomodoroViewModel;
    private TaskViewModel taskViewModel;
    private CountDownTimer countDownTimer;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private NotificationManager notificationManager;

    private TextView tvTaskSelectorTitle;

    private LinearLayout llWorkTime;
    private static final String CHANNEL_ID = "pomodoro_channel";

    private TextView tvQuote;
    private static final String API_KEY = "4nk8AESkqe/WDX6a8Iq0YQ==9HUzH55glGkzp8Vc";
    private static final String API_URL = "https://api.api-ninjas.com/v1/quotes?category=inspirational";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pomodoro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvQuote = view.findViewById(R.id.tv_quote);
        tvTaskSelectorTitle = view.findViewById(R.id.tv_task_selector_title);

        llWorkTime = view.findViewById(R.id.ll_work_time);

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Initialize services
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        // Initialize views
        spinnerTasks = view.findViewById(R.id.spinner_tasks);
        etDuration = view.findViewById(R.id.et_duration);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        tvTitle = view.findViewById(R.id.tv_title);
        tvSubtitle = view.findViewById(R.id.tv_subtitle);
        timerText = view.findViewById(R.id.timer_text);
        btnStartPause = view.findViewById(R.id.btn_start_pause);
        btnSkip = view.findViewById(R.id.btn_skip);

        // Initialize ViewModels
        pomodoroViewModel = new ViewModelProvider(requireActivity()).get(PomodoroViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        // Setup UI and observers
        setupTaskSelector();
        setupConfirmButton();
        setupTimerControls();
        observeViewModel();
        toggleTimerVisibility(pomodoroViewModel.getTimerRunning().getValue() != null && pomodoroViewModel.getTimerRunning().getValue());
        if (pomodoroViewModel.getTimerRunning().getValue() != null &&
                pomodoroViewModel.getTimerRunning().getValue()) {
            tvQuote.setVisibility(View.VISIBLE);
            fetchQuote();
        }
    }



    private void fetchQuote() {
        new Thread(() -> {
            try {
                // 修正API URL（移除category参数）
                URL url = new URL("https://api.api-ninjas.com/v1/quotes");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestProperty("X-Api-Key", API_KEY);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    InputStream response = connection.getInputStream();
                    String json = new Scanner(response).useDelimiter("\\A").next();

                    JSONArray jsonArray = new JSONArray(json);
                    if (jsonArray.length() > 0) {
                        JSONObject quoteObj = jsonArray.getJSONObject(0);
                        final String quote = quoteObj.getString("quote") + "\n- " + quoteObj.getString("author");

                        requireActivity().runOnUiThread(() -> {
                            tvQuote.setText(quote);
                            tvQuote.setVisibility(View.VISIBLE);
                        });
                    } else {

                        showDefaultQuote();
                    }
                } else {
                    showDefaultQuote();
                }
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {

                    tvQuote.setText("Stay focused and do your best!");
                    tvQuote.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private void showDefaultQuote() {
        requireActivity().runOnUiThread(() -> {
            tvQuote.setText("Stay focused and do your best!");
            tvQuote.setVisibility(View.VISIBLE);
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        // Resume timer if it was running
        Boolean timerRunning = pomodoroViewModel.getTimerRunning().getValue();
        if (timerRunning != null && timerRunning) {
            startTimer();
            Log.d(TAG, "onResume: Resuming timer with TimeLeft=" + pomodoroViewModel.getTimeLeftInMillis().getValue());
        }
    }

    private void setupTaskSelector() {
        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            List<ToDoModel> incompleteTasks = new ArrayList<>();
            if (tasks != null) {
                for (ToDoModel task : tasks) {
                    if (task.getStatus() == 0) {
                        incompleteTasks.add(task);
                    }
                }
                Log.d(TAG, "setupTaskSelector: Loaded " + incompleteTasks.size() + " incomplete tasks");
            } else {
                Log.w(TAG, "setupTaskSelector: Task list is null");
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
                        textView.setText(incompleteTasks.get(position).getTask());
                        return textView;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                        textView.setText(incompleteTasks.get(position).getTask());
                        return textView;
                    }
                };

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTasks.setAdapter(adapter);

                // Restore selected task if available
                ToDoModel selected = pomodoroViewModel.getSelectedTask().getValue();
                if (selected != null) {
                    for (int i = 0; i < incompleteTasks.size(); i++) {
                        if (incompleteTasks.get(i).getId() == selected.getId()) {
                            spinnerTasks.setSelection(i);
                            break;
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "No incomplete tasks found", Toast.LENGTH_LONG).show();
            }
        });

        spinnerTasks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ToDoModel task = (ToDoModel) parent.getItemAtPosition(position);
                pomodoroViewModel.setSelectedTask(task);
                Log.d(TAG, "Task selected: ID=" + task.getId() + ", Name=" + task.getTask());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                pomodoroViewModel.setSelectedTask(null);
            }
        });
    }

    private void setupConfirmButton() {
        btnConfirm.setOnClickListener(v -> {
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

                pomodoroViewModel.setCustomWorkDuration(minutes * 60 * 1000L);
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

    private void observeViewModel() {
        pomodoroViewModel.getTimeLeftInMillis().observe(getViewLifecycleOwner(), time -> updateTimerDisplay());
        pomodoroViewModel.getTimerRunning().observe(getViewLifecycleOwner(), running -> {
            if (running != null) {
                updateButtonStates();
            }
        });
        pomodoroViewModel.getCurrentPhase().observe(getViewLifecycleOwner(), phase -> {
            if (phase != null) {
                updatePhaseUI();
            }
        });
    }

    private void toggleTimer() {
        Boolean running = pomodoroViewModel.getTimerRunning().getValue();
        if (running != null && running) {
            pauseTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        Long time = pomodoroViewModel.getTimeLeftInMillis().getValue();
        if (time == null || time <= 0) {
            initializeTimer();
            time = pomodoroViewModel.getTimeLeftInMillis().getValue();
            if (time == null) return;
        }

        countDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                pomodoroViewModel.setTimeLeftInMillis(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                pomodoroViewModel.setTimerRunning(false);
                handleTimerCompletion();
            }
        }.start();

        pomodoroViewModel.setTimerRunning(true);
        Log.d(TAG, "Timer started: Phase=" + pomodoroViewModel.getCurrentPhase().getValue() +
                ", TimeLeft=" + pomodoroViewModel.getTimeLeftInMillis().getValue());
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        pomodoroViewModel.setTimerRunning(false);
        Log.d(TAG, "Timer paused: TimeLeft=" + pomodoroViewModel.getTimeLeftInMillis().getValue());
    }

    private void skipPhase() {
        pauseTimer();
        PomodoroViewModel.TimerPhase phase = pomodoroViewModel.getCurrentPhase().getValue();
        if (phase == PomodoroViewModel.TimerPhase.WORK) {
            handlePhaseEnd();
        } else {
            stopAlarm();
            startWorkPhase();
        }
    }

    private void initializeTimer() {
        PomodoroViewModel.TimerPhase phase = pomodoroViewModel.getCurrentPhase().getValue();
        if (phase == PomodoroViewModel.TimerPhase.WORK) {
            startWorkPhase();
        } else if (phase == PomodoroViewModel.TimerPhase.BREAK || phase == PomodoroViewModel.TimerPhase.LONG_BREAK) {
            startBreakPhase();
        }
    }

    private void startWorkPhase() {
        Long duration = pomodoroViewModel.getCustomWorkDuration().getValue();
        if (duration == null || duration <= 0) {
            Toast.makeText(requireContext(), "Work duration not set", Toast.LENGTH_SHORT).show();
            return;
        }
        pomodoroViewModel.setCurrentPhase(PomodoroViewModel.TimerPhase.WORK);
        pomodoroViewModel.setTimeLeftInMillis(duration);
        updateTitle("Focus Time", "Start working on " + getTaskName());
        timerText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue));
        btnStartPause.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_blue));}

    private void startBreakPhase() {
        Integer cycleCount = pomodoroViewModel.getPomodoroCycleCount().getValue();
        boolean isLongBreak = cycleCount != null && cycleCount % 4 == 0 && cycleCount > 0;
        Long duration = isLongBreak ? 15 * 60 * 1000L : pomodoroViewModel.getCustomBreakDuration().getValue();
        if (duration == null || duration <= 0) {
            duration = 5 * 60 * 1000L;
            pomodoroViewModel.setCustomBreakDuration(duration);
        }
        pomodoroViewModel.setCurrentPhase(isLongBreak ? PomodoroViewModel.TimerPhase.LONG_BREAK : PomodoroViewModel.TimerPhase.BREAK);
        pomodoroViewModel.setTimeLeftInMillis(duration);
        updateTitle(isLongBreak ? "Long Break" : "Break Time", "Relax~");
        timerText.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_grey));
        btnSkip.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondary_grey)));
    }

    private void updatePhaseUI() {
        PomodoroViewModel.TimerPhase phase = pomodoroViewModel.getCurrentPhase().getValue();
        if (phase == PomodoroViewModel.TimerPhase.WORK) {
            updateTitle("Focus Time", "Start working on " + getTaskName());
            timerText.setTextColor(ContextCompat.getColor(requireContext(), R.color.work_primary));
        } else {
            boolean isLongBreak = phase == PomodoroViewModel.TimerPhase.LONG_BREAK;
            updateTitle(isLongBreak ? "Long Break" : "Break Time", "Relax~");
            timerText.setTextColor(ContextCompat.getColor(requireContext(), R.color.break_primary));
        }
    }

    private String getTaskName() {
        ToDoModel task = pomodoroViewModel.getSelectedTask().getValue();
        return task != null ? task.getTask() : "Task";
    }

    private void playAlarm() {
        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer.create(requireContext(), android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
                if (mediaPlayer != null) {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                    Log.d(TAG, "playAlarm: MediaPlayer started");
                } else {
                    Log.e(TAG, "playAlarm: Failed to create MediaPlayer");
                }
            } catch (Exception e) {
                Log.e(TAG, "playAlarm: Error creating MediaPlayer", e);
            }
        }
        if (vibrator != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(1000);
                }
                Log.d(TAG, "playAlarm: Vibration triggered");
            } catch (Exception e) {
                Log.e(TAG, "playAlarm: Error triggering vibration", e);
            }
        }
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
                Log.d(TAG, "stopAlarm: MediaPlayer stopped and released");
            } catch (Exception e) {
                Log.e(TAG, "stopAlarm: Error stopping MediaPlayer", e);
            } finally {
                mediaPlayer = null;
            }
        }
        if (vibrator != null) {
            try {
                vibrator.cancel();
                Log.d(TAG, "stopAlarm: Vibration canceled");
            } catch (Exception e) {
                Log.e(TAG, "stopAlarm: Error canceling vibration", e);
            }
        }
    }

    private void handlePhaseEnd() {
        playAlarm();
        PomodoroViewModel.TimerPhase phase = pomodoroViewModel.getCurrentPhase().getValue();
        if (phase == PomodoroViewModel.TimerPhase.WORK) {
            pomodoroViewModel.incrementPomodoroCycle();
            showWorkCompletionDialog();
        } else {
            pomodoroViewModel.resetPomodoroCycle();
            showBreakCompletionDialog();
        }
    }

    private void showWorkCompletionDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Work Time Ended")
                .setMessage("Would you like to continue or mark the task as complete?")
                .setPositiveButton("Continue", (d, w) -> {
                    stopAlarm();
                    showTimeSettingsDialog(true);
                })
                .setNegativeButton("Complete", (d, w) -> {
                    stopAlarm();
                    completeTask();
                })
                .setOnCancelListener(dialog -> stopAlarm()) // Stop alarm if dialog is canceled
                .setCancelable(true)
                .show();
    }

    private void showBreakCompletionDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(pomodoroViewModel.getCurrentPhase().getValue() == PomodoroViewModel.TimerPhase.LONG_BREAK ? "Long Break Ended" : "Break Time Ended")
                .setMessage("Ready to start working?")
                .setPositiveButton("Continue", (d, w) -> {
                    stopAlarm();
                    startWorkPhase();
                })
                .setOnCancelListener(dialog -> stopAlarm()) // Stop alarm if dialog is canceled
                .setCancelable(true)
                .show();
    }

    private void completeTask() {
        pomodoroViewModel.completeTask();
        Toast.makeText(requireContext(), "Task completed!", Toast.LENGTH_SHORT).show();
        stopAlarm(); // Ensure alarm is stopped before navigation
        NavController navController = Navigation.findNavController(requireView());
        Log.d(TAG, "Navigating to TaskFragment");
        navController.navigate(R.id.action_pomodoroFragment_to_taskFragment);
    }

    private void updateTitle(String title, String subtitle) {
        tvTitle.setText(title);
        tvSubtitle.setText(subtitle);
    }

    private void updateTimerDisplay() {
        Long time = pomodoroViewModel.getTimeLeftInMillis().getValue();
        if (time != null) {
            int minutes = (int) (time / 1000) / 60;
            int seconds = (int) (time / 1000) % 60;
            timerText.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }

    private void updateButtonStates() {
        Boolean running = pomodoroViewModel.getTimerRunning().getValue();
        btnStartPause.setText(running != null && running ? "Pause" : "Start");
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
        PomodoroViewModel.TimerPhase phase = pomodoroViewModel.getCurrentPhase().getValue();
        String title = phase == PomodoroViewModel.TimerPhase.WORK ? "⏰ Work Time Ended" :
                (phase == PomodoroViewModel.TimerPhase.LONG_BREAK ? "🏖 Long Break Ended" : "☕ Break Time Ended");
        String content = phase == PomodoroViewModel.TimerPhase.WORK ? "Time to take a break!" : "Ready to continue working?";

        Notification notification = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1, notification);
        Log.d(TAG, "sendCompletionNotification: Notification sent for phase=" + phase);
    }

    private void handleTimerCompletion() {
        sendCompletionNotification();
        handlePhaseEnd();
    }

    private void showTimeSettingsDialog(boolean isMandatory) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_time_settings, null);
        EditText etWork = dialogView.findViewById(R.id.et_work_duration);
        EditText etBreak = dialogView.findViewById(R.id.et_break_duration);

        Long workDuration = pomodoroViewModel.getCustomWorkDuration().getValue();
        Long breakDuration = pomodoroViewModel.getCustomBreakDuration().getValue();
        etWork.setText(String.valueOf(workDuration != null ? workDuration / 60000 : 25));
        etBreak.setText(String.valueOf(breakDuration != null ? breakDuration / 60000 : 5));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Set Next Session")
                .setView(dialogView)
                .setPositiveButton("Continue", (d, w) -> {
                    try {
                        int newWork = Integer.parseInt(etWork.getText().toString());
                        int newBreak = Integer.parseInt(etBreak.getText().toString());

                        if (newWork <= 0 || newBreak <= 0) {
                            Toast.makeText(requireContext(), "Durations must be greater than 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        pomodoroViewModel.setCustomWorkDuration(newWork * 60000L);
                        pomodoroViewModel.setCustomBreakDuration(newBreak * 60000L);
                        startBreakPhase();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(isMandatory ? "Exit" : "Cancel", (d, w) -> {
                    stopAlarm();
                    if (isMandatory) {
                        NavController navController = Navigation.findNavController(requireView());
                        Log.d(TAG, "Navigating to TaskFragment from time settings dialog");
                        navController.navigate(R.id.action_pomodoroFragment_to_taskFragment);
                    }
                })
                .setOnCancelListener(dialog1 -> stopAlarm())
                .setCancelable(true)
                .create();

        dialog.show();
    }

    private void toggleSelectionVisibility(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        tvTaskSelectorTitle.setVisibility(visibility);
        spinnerTasks.setVisibility(visibility);
        llWorkTime.setVisibility(visibility);
        btnConfirm.setVisibility(visibility);
    }

    private void toggleTimerVisibility(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        tvTitle.setVisibility(visibility);
        tvSubtitle.setVisibility(visibility);
        timerText.setVisibility(visibility);
        btnStartPause.setVisibility(visibility);
        btnSkip.setVisibility(visibility);


        if (show) {
            tvQuote.setVisibility(View.VISIBLE);
            fetchQuote();
        } else {
            tvQuote.setVisibility(View.GONE);
            tvQuote.setText(""); // Clear previous text
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseTimer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAlarm();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}