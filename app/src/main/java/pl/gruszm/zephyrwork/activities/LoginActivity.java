package pl.gruszm.zephyrwork.activities;

import static pl.gruszm.zephyrwork.config.AppConfig.CONNECTION_ERROR_STANDARD_MSG;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.api.LoginRequest;
import pl.gruszm.zephyrwork.config.AppConfig;

public class LoginActivity extends AppCompatActivity
{
    private boolean callLock;
    private EditText email, password;
    private ImageButton submitButton;
    private boolean initiated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        callLock = false;

        email = findViewById(R.id.email_edit_text);
        password = findViewById(R.id.password_edit_text);
        submitButton = findViewById(R.id.login_submit_btn);

        submitButton.setOnClickListener(this::submitOnClickListener);

        ensureLocationPermissionIsGranted();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        ensureLocationPermissionIsGranted();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length < 2)
        {
            return;
        }

        if (requestCode == AppConfig.LOCATION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                ensureNotificationsAreEnabled();
            }
            else
            {
                finish();
            }
        }
    }

    private void ensureLocationPermissionIsGranted()
    {
        if ((checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED))
        {
            requestPermissions(Arrays.asList(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).toArray(new String[0]), AppConfig.LOCATION_CODE);
        }
        else
        {
            ensureNotificationsAreEnabled();
        }
    }

    private void ensureNotificationsAreEnabled()
    {
        if (!initiated)
        {
            initiated = true;

            return;
        }

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) && (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED))
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                    .setTitle("Notifications are disabled")
                    .setMessage("This application requires sending notifications. Please enable them in settings.")
                    .setPositiveButton("Settings", (dialog, which) ->
                    {
                        dialog.dismiss();

                        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
                        {
                            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                            startActivity(intent);
                        }
                        else
                        {
                            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                            {
                                validateTokenAndSwitchActivity();
                            }
                            else
                            {
                                ensureGpsIsActive();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> finish());

            alertDialogBuilder.create().show();
        }
        else
        {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                validateTokenAndSwitchActivity();
            }
            else
            {
                ensureGpsIsActive();
            }
        }
    }

    private void ensureGpsIsActive()
    {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                    .setTitle("GPS provider is disabled")
                    .setMessage("This application requires an active GPS provider. Please enable GPS in settings.")
                    .setPositiveButton("Proceed", (dialog, which) ->
                    {
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        {
                            dialog.dismiss();
                            validateTokenAndSwitchActivity();
                        }
                        else
                        {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> finish());

            alertDialogBuilder.create().show();
        }
    }

    private void validateTokenAndSwitchActivity()
    {
        OkHttpClient okHttpClient = new OkHttpClient();
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        String jwt = sharedPreferences.getString("Auth", "");

        Request request = new Request.Builder()
                .url(AppConfig.BACKEND_BASE.concat("/auth/validate"))
                .get()
                .header("Auth", jwt)
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, CONNECTION_ERROR_STANDARD_MSG, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    runOnUiThread(() ->
                    {
                        Intent intent = new Intent(LoginActivity.this, WorkSessionActivity.class);

                        finish();
                        startActivity(intent);
                    });

                    response.close();
                }
            }
        });
    }

    private void submitOnClickListener(View view)
    {
        if (callLock == true)
        {
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        Gson gson = new Gson();
        MediaType mediaTypeJson = MediaType.get("application/json");
        LoginRequest loginRequest = new LoginRequest(email.getText().toString(), password.getText().toString());
        String loginRequestJson = gson.toJson(loginRequest);
        RequestBody requestBody = RequestBody.create(loginRequestJson, mediaTypeJson);

        Request request = new Request.Builder()
                .url(AppConfig.BACKEND_BASE.concat("/auth/login"))
                .post(requestBody)
                .build();

        Call call = okHttpClient.newCall(request);
        callLock = true;

        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, CONNECTION_ERROR_STANDARD_MSG, Toast.LENGTH_SHORT).show());

                callLock = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    String jwt = response.body().string();
                    SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("Auth", jwt);
                    editor.apply();

                    runOnUiThread(() ->
                    {
                        Intent intent = new Intent(LoginActivity.this, WorkSessionActivity.class);

                        finish();
                        startActivity(intent);
                    });
                }
                else
                {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Invalid email or password. Please try again.", Toast.LENGTH_SHORT).show());
                }

                callLock = false;

                response.close();
            }
        });
    }
}