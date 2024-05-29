package pl.gruszm.zephyrwork.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.LocalDateTime;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.gruszm.zephyrwork.DTOs.LocationDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.activities.WorkSessionActivity;
import pl.gruszm.zephyrwork.config.AppConfig;

public class LocationSenderService extends Service implements LocationListener
{
    private static final int LOCATION_TRACKING_DELAY_MS = 5000;
    private static final String CHANNEL_ID = "ZephyrWorkLocationServiceChannel";
    private OkHttpClient okHttpClient;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public void onCreate()
    {
        super.onCreate();

        okHttpClient = new OkHttpClient();
        gson = new Gson();
        sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        createNotificationChannel();
        startForeground(1, getNotification());
    }

    // Suppressed, because permission is checked in the method, which start the service
    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        fusedLocationProviderClient.requestLocationUpdates(
                new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_TRACKING_DELAY_MS).build(),
                this,
                Looper.getMainLooper()
        );

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
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

    private Notification getNotification()
    {
        Intent notificationIntent = new Intent(this, WorkSessionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ZephyrWork")
                .setContentText("Sending Your locations for the active work session")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        LocationDTO locationDTO = new LocationDTO(LocalDateTime.now().toString(), location.getLatitude(), location.getLongitude());
        RequestBody requestBody = RequestBody.create(gson.toJson(locationDTO), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(AppConfig.BACKEND_BASE.concat("/locations/token"))
                .post(requestBody)
                .header("Auth", sharedPreferences.getString("Auth", ""))
                .build();

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                showToast(AppConfig.CONNECTION_ERROR_STANDARD_MSG);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    showToast("Your location has been saved for the active work session.");

                    response.close();
                }
                else if (response.code() == 400)
                {
                    Intent intent = new Intent(LocationSenderService.this, LocationSenderService.class);

                    showToast("You do not have an active work session. Locations tracking has been turned off.");
                    stopService(intent);
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    showToast("Authorization error. The location could be sent. Please log in to resume location sending.");
                }
            }
        });
    }

    private void showToast(String message)
    {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(LocationSenderService.this, message, Toast.LENGTH_SHORT).show());
    }
}
