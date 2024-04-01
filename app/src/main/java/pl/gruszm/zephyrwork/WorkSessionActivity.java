package pl.gruszm.zephyrwork;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.gruszm.zephyrwork.config.AppConfig;

public class WorkSessionActivity extends AppCompatActivity
{
    private boolean callLock;
    private Button startWorkSessionBtn, finishWorkSessionBtn, readCurrentLocationBtn, userProfileBtn;
    private TextView workSessionResponse, currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_session);

        callLock = false;

        startWorkSessionBtn = findViewById(R.id.start_work_session_btn);
        finishWorkSessionBtn = findViewById(R.id.finish_work_session_btn);
        readCurrentLocationBtn = findViewById(R.id.read_current_location_btn);
        userProfileBtn = findViewById(R.id.user_profile_btn);
        workSessionResponse = findViewById(R.id.work_session_response);
        currentLocation = findViewById(R.id.current_location);

        startWorkSessionBtn.setOnClickListener(this::startWorkSessionOnClickListener);
        finishWorkSessionBtn.setOnClickListener(this::finishWorkSessionOnClickListener);
        readCurrentLocationBtn.setOnClickListener(this::readCurrentLocationOnClickListener);
        userProfileBtn.setOnClickListener(this::userProfileOnClickListener);
    }

    private void startWorkSessionOnClickListener(View view)
    {
        if (callLock == true)
        {
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        String jwt = sharedPreferences.getString("Auth", "");

        Request request = new Request.Builder()
                .url("http://192.168.0.100:8080/api/worksessions/start")
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
                runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, "Connection error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show());

                callLock = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                System.out.println("RESPONSE: " + response.code());

                if (response.isSuccessful())
                {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> workSessionResponse.setText(responseBody));
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    runOnUiThread(() ->
                    {
                        Intent intent = new Intent(WorkSessionActivity.this, LoginActivity.class);

                        startActivity(intent);
                        finish();
                    });
                }
                else if (response.code() == 400) // Bad Request, the user already has an active Work Session
                {
                    runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, "You already have an active Work Session at the moment.", Toast.LENGTH_SHORT).show());
                }

                callLock = false;

                response.close();
            }
        });
    }

    private void finishWorkSessionOnClickListener(View view)
    {
        if (callLock == true)
        {
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        String jwt = sharedPreferences.getString("Auth", "");

        Request request = new Request.Builder()
                .url("http://192.168.0.100:8080/api/worksessions/stop")
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
                runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, "Connection error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show());

                callLock = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                System.out.println("RESPONSE: " + response.code());

                if (response.isSuccessful())
                {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> workSessionResponse.setText(responseBody));
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    runOnUiThread(() ->
                    {
                        Intent intent = new Intent(WorkSessionActivity.this, LoginActivity.class);

                        startActivity(intent);
                        finish();
                    });
                }
                else if (response.code() == 400) // Bad Request, the user does not have an active Work Session
                {
                    runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, "You do not have an active Work Session at the moment.", Toast.LENGTH_SHORT).show());
                }

                callLock = false;

                response.close();
            }
        });
    }

    private void readCurrentLocationOnClickListener(View view)
    {
        // Check permissions for location
        if ((checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED))
        {
            requestPermissions(Arrays.asList(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).toArray(new String[0]), AppConfig.LOCATION_CODE);

            return;
        }

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationProviderClient.requestLocationUpdates(
                new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0).setMaxUpdates(1).build(),
                location ->
                {
                    currentLocation.setText(String.format(Locale.ENGLISH, "Latitude: %f\nLongitude: %f", location.getLatitude(), location.getLongitude()));
                },
                Looper.getMainLooper()
        );
    }

    private void userProfileOnClickListener(View view)
    {
        Intent intent = new Intent(this, UserProfileActivity.class);

        startActivity(intent);
    }
}