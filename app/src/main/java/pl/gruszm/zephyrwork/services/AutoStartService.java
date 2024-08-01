package pl.gruszm.zephyrwork.services;

import static pl.gruszm.zephyrwork.config.AppConfig.CONNECTION_ERROR_STANDARD_MSG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import java.io.IOException;
import java.time.LocalTime;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.gruszm.zephyrwork.DTOs.UserDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.activities.WorkSessionActivity;
import pl.gruszm.zephyrwork.config.AppConfig;

public class AutoStartService extends Service
{
    private Handler handler;
    private Runnable runnable;
    private static final int INTERVAL_MS = 5_000;
    private static final String CHANNEL_ID = "Auto Start Checker Service";
    private OkHttpClient okHttpClient;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    private boolean callLock = false;

    @Override
    public void onCreate()
    {
        super.onCreate();

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                checkCurrentTime();
                handler.postDelayed(this, INTERVAL_MS);
            }
        };

        okHttpClient = new OkHttpClient();
        gson = new Gson();
        sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
    }

    private void checkCurrentTime()
    {
        String jwt = sharedPreferences.getString("Auth", "");

        Request request = new Request.Builder()
                .url(AppConfig.BACKEND_BASE.concat("/users/token"))
                .header("Auth", jwt)
                .get()
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    UserDTO userDTO = gson.fromJson(response.body().string(), UserDTO.class);

                    LocalTime timeNow = LocalTime.now();
                    LocalTime startingTimeSet = LocalTime.of(userDTO.getStartingHour(), userDTO.getStartingMinute());
                    LocalTime endingTimeSet = LocalTime.of(userDTO.getEndingHour(), userDTO.getEndingMinute());

                    if (timeNow.isAfter(startingTimeSet) && timeNow.isBefore(endingTimeSet))
                    {
                        startWorkSession();
                    }

                    response.close();
                }
            }
        });
    }

    private void startWorkSession()
    {
        if (callLock)
        {
            return;
        }

        String jwt = sharedPreferences.getString("Auth", "");

        Request request = new Request.Builder()
                .url(AppConfig.BACKEND_BASE.concat("/worksessions/start"))
                .header("Auth", jwt)
                .post(RequestBody.create("", null))
                .build();

        Call call = okHttpClient.newCall(request);
        callLock = true;

        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(AutoStartService.this, CONNECTION_ERROR_STANDARD_MSG, Toast.LENGTH_SHORT).show());

                callLock = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    // Start the location tracking
                    Intent locationSenderService = new Intent(AutoStartService.this, LocationSenderService.class);
                    locationSenderService.putExtra("interval", Integer.parseInt(response.body().string()));

                    startService(locationSenderService);

                    response.close();
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    new Handler(Looper.getMainLooper()).post(
                            () -> Toast.makeText(AutoStartService.this, "Authorization error. Please log in and try again.", Toast.LENGTH_SHORT).show());
                }
                else if (response.code() == 400) // Bad Request, the user already has an active Work Session
                {
                    new Handler(Looper.getMainLooper()).post(
                            () -> Toast.makeText(AutoStartService.this, "You already have an active Work Session at the moment.", Toast.LENGTH_SHORT).show());
                }

                callLock = false;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, WorkSessionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("Auto Start Checker")
                .setContentText("Work session will be automatically started.")
                .setSmallIcon(R.drawable.round_work_24)
                .setContentIntent(pendingIntent)
                .build();

//        if (isRunning)
//        {
//            stopForeground(STOP_FOREGROUND_REMOVE);
//            startForeground(2, notification);
//            handler.removeCallbacks(runnable);
//            handler.post(runnable);
//        }
//        else
//        {
//            startForeground(2, notification);
//            handler.post(runnable);
//            isRunning = true;
//        }

        startForeground(2, notification);
        handler.post(runnable);

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        handler.removeCallbacks(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void createNotificationChannel()
    {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null)
        {
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
