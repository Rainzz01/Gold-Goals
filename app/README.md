# GoalAndGoals

> **GoalAndGoals** is an Android application designed to help users manage tasks and boost productivity using a gamified to-do list and Pomodoro timer. Built with Firebase for authentication and data synchronization, and Room for local storage, the app allows users to create, track, and complete tasks while earning experience points (XP) and coins. The app includes a dashboard to visualize task statistics and a Pomodoro timer to enhance focus.

## Features

- **User Authentication**: Secure login and registration using Firebase Authentication.
- **Task Management**:
  - Create, edit, and delete tasks with details like difficulty, deadlines, rewards, and penalties.
  - Mark tasks as complete to earn XP and coins.
  - Tasks are user-specific, ensuring data isolation across multiple users.
- **Pomodoro Timer**:
  - Customizable work and break durations.
  - Notifications and alarms for phase completion.
  - Integration with tasks to mark completion after work sessions.
- **User Progress**:
  - Track XP and coins earned from task completion.
  - Local storage with Room and real-time sync with Firebase Realtime Database.
- **Statistics Dashboard**:
  - Visualize task completion rates, punctuality, and personality traits based on task difficulty.
  - Pie charts for intuitive data representation using MPAndroidChart.
- **Data Persistence**:
  - Local storage with Room for offline access.
  - Firebase Realtime Database for cloud synchronization.
- **Multi-User Support**:
  - Each user's tasks and progress are isolated using Firebase UID.

## Tech Stack

- **Language**: Java
- **Framework**: Android SDK
- **Database**:
  - **Room**: Local SQLite database for offline storage.
  - **Firebase Realtime Database**: Cloud storage for task and progress synchronization.
- **Authentication**: Firebase Authentication (Email/Password)
- **UI**: Android XML layouts, Material Design components
- **Libraries**:
  - **LiveData & ViewModel**: For reactive UI updates.
  - **MPAndroidChart**: For statistical visualizations.
  - **Firebase SDK**: For authentication and database.
- **IDE**: Android Studio
- **Minimum SDK**: API 21 (Android 5.0 Lollipop)

## Project Structure

```
GoalAndGoals/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/goalandgoals/
│   │   │   │   ├── Activity/
│   │   │   │   │   ├── LoginActivity.java
│   │   │   │   │   ├── RegisterActivity.java
│   │   │   │   │   ├── MainActivity.java
│   │   │   │   │   ├── CreateTaskActivity.java
│   │   │   │   ├── Adapter/
│   │   │   │   │   ├── ToDoAdapter.java
│   │   │   │   │   ├── StatsAdapter.java
│   │   │   │   ├── Dao/
│   │   │   │   │   ├── ToDoDao.java
│   │   │   │   │   ├── UserProgressDao.java
│   │   │   │   ├── Fragment/
│   │   │   │   │   ├── TaskFragment.java
│   │   │   │   │   ├── StatusFragment.java
│   │   │   │   │   ├── SettingFragment.java
│   │   │   │   │   ├── PomodoroFragment.java
│   │   │   │   ├── Helper/
│   │   │   │   │   ├── RecyclerItemTouchHelper.java
│   │   │   │   ├── Model/
│   │   │   │   │   ├── TaskViewModel.java
│   │   │   │   │   ├── PomodoroViewModel.java
│   │   │   │   │   ├── ToDoModel.java
│   │   │   │   │   ├── UserProgress.java
│   │   │   │   │   ├── StatItem.java
│   │   │   │   ├── Utils/
│   │   │   │   │   ├── AppDatabase.java
│   │   │   │   │   ├── FirebaseSyncUtils.java
│   │   │   ├── res/
│   │   │   │   ├── AndroidManifest.xml
│   │   │   │   ├── layout/
│   │   │   │   ├── values/
│   │   │   │   ├── drawable/
│   ├── build.gradle
├── README.md
```

## Prerequisites

- **Android Studio**: Version 4.0 or higher
- **Java**: JDK 8 or higher
- **Firebase Project**: Set up a Firebase project with Authentication and Realtime Database enabled.
- **API Key**: For inspirational quotes in the Pomodoro timer (optional, using API Ninjas).

## Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/<your-username>/GoalAndGoals.git
   cd GoalAndGoals
   ```

2. **Open in Android Studio**:
    - Open Android Studio and select `Open an existing project`.
    - Navigate to the `GoalAndGoals` directory and click `OK`.

3. **Configure Firebase**:
    - Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/).
    - Enable **Email/Password** authentication in the Authentication section.
    - Enable **Realtime Database** and set the database URL to `https://rpgtodoapp-8e638-default-rtdb.asia-southeast1.firebasedatabase.app/`.
    - Download the `google-services.json` file and place it in the `app/` directory.
    - Add Firebase dependencies to `app/build.gradle`:
      ```gradle
      implementation 'com.google.firebase:firebase-auth:22.0.0'
      implementation 'com.google.firebase:firebase-database:20.1.0'
      ```
    - Apply the Google Services plugin in `app/build.gradle`:
      ```gradle
      apply plugin: 'com.google.gms.google-services'
      ```
    - Add the classpath in `build.gradle` (project level):
      ```gradle
      classpath 'com.google.gms:google-services:4.3.15'
      ```

4. **Add API Key for Quotes (Optional)**:
    - Obtain an API key from [API Ninjas](https://api-ninjas.com/) for the quotes feature.
    - Update the `API_KEY` constant in `PomodoroFragment.java`:
      ```java
      private static final String API_KEY = "your-api-key-here";
      ```

5. **Sync Project**:
    - Click `Sync Project with Gradle Files` in Android Studio.
    - Ensure all dependencies are resolved.

6. **Run the App**:
    - Connect an Android device or start an emulator.
    - Click `Run` in Android Studio to build and install the app.

## Usage

1. **Register/Login**:
    - Open the app and register with an email and password or log in with existing credentials.
    - The app uses Firebase Authentication to manage user sessions.

2. **Create Tasks**:
    - Navigate to the **Task** tab and click the "+" button to create a new task.
    - Set task details like name, description, difficulty, start time, deadline, and rewards.

3. **Use Pomodoro Timer**:
    - Go to the **Pomodoro** tab, select a task, and set a work duration.
    - Start the timer to focus on the task, with breaks scheduled automatically.
    - Mark tasks as complete to earn XP and coins.

4. **View Statistics**:
    - Visit the **Status** tab to see task completion rates, punctuality, and personality traits based on task difficulty.

5. **Track Progress**:
    - Check your XP and coins in the **Task** tab, updated as you complete tasks.

## Database Schema

- **Room (Local)**:
    - `todo` table:
        - `id` (Primary Key, Auto-generated)
        - `task`, `description`, `difficulty`, `start_time`, `deadline`, `reminder_time`, `task_type`, `firebaseKey`, `userId` (TEXT)
        - `status`, `exp_reward`, `coin_reward`, `exp_penalty`, `coin_penalty`, `repeat_count` (INTEGER)
    - `user_progress` table:
        - `userId` (Primary Key, TEXT)
        - `xp`, `coins` (INTEGER)
        - `name` (TEXT, optional)


- **Firebase Realtime Database**:
  ```
  users/
    <uid>/
      tasks/
        <task_key>/
          task, description, difficulty, start_time, deadline, ...
      progress/
        userId, xp, coins, name
  ```
  
## Contributing

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Make changes and commit (`git commit -m "Add your feature"`).
4. Push to your fork (`git push origin feature/your-feature`).
5. Create a pull request with a detailed description of changes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact

For questions or feedback, please contact [yeetianlow@gmail.com] or open an issue on GitHub.