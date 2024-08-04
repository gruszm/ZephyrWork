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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.gruszm.zephyrwork.DTOs.UserDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.config.AppConfig;
import pl.gruszm.zephyrwork.enums.RoleType;
import pl.gruszm.zephyrwork.navigation.MyOnNavigationItemSelectedListener;
import pl.gruszm.zephyrwork.services.AutoStartService;
import pl.gruszm.zephyrwork.services.LocationSenderService;

public class WorkSessionActivity extends AppCompatActivity
{
    // Common
    private OkHttpClient okHttpClient;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    private boolean callLock;
    private boolean ceoAgreed;

    // Buttons
    private ImageButton startWorkSessionBtn, finishWorkSessionBtn, userProfileBtn, logoutBtn;

    // Layout
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

    // Navigation Header Views
    private TextView navFirstNameAndLastName, navEmail;

    // Other
    private String userRole;
    private UserDTO userDTO;

    @Override
    protected void onResume()
    {
        super.onResume();

        if (sharedPreferences.getString("Auth", "").isEmpty())
        {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_session);

        // Common
        okHttpClient = new OkHttpClient();
        gson = new Gson();
        sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        callLock = false;
        ceoAgreed = false;

        // Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        // Configure navigation
        setSupportActionBar(toolbar);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation Header Views
        navFirstNameAndLastName = navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
        navEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);

        // Buttons
        startWorkSessionBtn = findViewById(R.id.start_work_session_icon);
        finishWorkSessionBtn = findViewById(R.id.finish_work_session_icon);
        userProfileBtn = findViewById(R.id.user_profile_icon);
        logoutBtn = findViewById(R.id.logout_icon);

        // OnClickListeners
        startWorkSessionBtn.setOnClickListener(this::startWorkSessionOnClickListener);
        finishWorkSessionBtn.setOnClickListener(this::finishWorkSessionOnClickListener);
        userProfileBtn.setOnClickListener(this::userProfileOnClickListener);
        logoutBtn.setOnClickListener(this::logoutOnClickListener);

        // Enable button for managers and the CEO, update data in the navigation header
        checkUserDataAndUpdateView();
    }

    @Override
    public void onBackPressed()
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    private void navigationOnClickListener(View view)
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void checkUserDataAndUpdateView()
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
                runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, CONNECTION_ERROR_STANDARD_MSG, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    UserDTO userDTO = gson.fromJson(response.body().string(), UserDTO.class);

                    runOnUiThread(() ->
                    {
                        navFirstNameAndLastName.setText(userDTO.getFirstName()
                                .concat(" ")
                                .concat(userDTO.getLastName()));

                        navEmail.setText(userDTO.getEmail());

                        userRole = userDTO.getRoleName();

                        // Toolbar and navigation handling
                        MyOnNavigationItemSelectedListener itemSelectedListener = new MyOnNavigationItemSelectedListener(
                                WorkSessionActivity.this,
                                userRole,
                                navFirstNameAndLastName.getText().toString(),
                                navEmail.getText().toString(),
                                drawerLayout
                        );
                        toolbar.setNavigationOnClickListener(WorkSessionActivity.this::navigationOnClickListener);
                        navigationView.setNavigationItemSelectedListener(itemSelectedListener);
                    });

                    if (userDTO.isForceStartWorkSession() && !AutoStartService.isRunning() && !LocationSenderService.isRunning())
                    {
                        startForegroundService(new Intent(WorkSessionActivity.this, AutoStartService.class));
                        AutoStartService.setRunning(true);
                    }

                    WorkSessionActivity.this.userDTO = userDTO;

                    response.close();
                }
            }
        });
    }

    private void logoutOnClickListener(View view)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        stopService(new Intent(this, AutoStartService.class));
        stopService(new Intent(this, LocationSenderService.class));
        AutoStartService.setRunning(false);

        editor.remove("Auth");
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

    private boolean ensureGpsIsActive()
    {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                    .setTitle("GPS provider is disabled")
                    .setMessage("This application requires an active GPS provider. Please enable GPS in settings.")
                    .setPositiveButton("Settings", (dialog, which) ->
                    {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            alertDialogBuilder.create().show();

            return false;
        }

        return true;
    }

    private boolean ensureNotificationsAreEnabled()
    {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) && (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED))
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                    .setTitle("Notifications are disabled")
                    .setMessage("This application requires sending notifications. Please enable them in settings.")
                    .setPositiveButton("Settings", (dialog, which) ->
                    {
                        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            alertDialogBuilder.create().show();

            return false;
        }

        return true;
    }

    private void startWorkSessionOnClickListener(View view)
    {
        if (userDTO.isForceStartWorkSession())
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                    .setTitle("You have required working hours")
                    .setMessage("You cannot start the work session manually, because You have required working hours. Please contact Your supervisor for more information.\n" +
                            "In case You cannot see the notification for auto-starting, restart the application.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

            alertDialogBuilder.create().show();

            return;
        }

        // Check permissions for location
        if ((checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            {
                if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_DENIED)
                {
                    requestPermissions(Arrays.asList(Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.FOREGROUND_SERVICE_LOCATION).toArray(new String[0]), AppConfig.LOCATION_CODE);
                }
            }
            else
            {
                requestPermissions(Arrays.asList(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION).toArray(new String[0]), AppConfig.LOCATION_CODE);
            }

            return;
        }

        // Check permissions for notifications for Android 13 and newer
        if (!ensureNotificationsAreEnabled())
        {
            return;
        }

        if ((!ensureGpsIsActive()) || callLock)
        {
            return;
        }

        if (userRole.equals(RoleType.CEO.name()) && !ceoAgreed)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setTitle("You are the CEO!");
            alertDialogBuilder.setMessage("Starting a work session will have no effect, since You are the CEO. Do you wish to start it anyway?");
            alertDialogBuilder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
            alertDialogBuilder.setPositiveButton("YES", ((dialog, which) ->
            {
                ceoAgreed = true;

                dialog.dismiss();
                startWorkSessionOnClickListener(view);
            }));

            alertDialogBuilder.show();

            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WorkSessionActivity.this);
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
                runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, CONNECTION_ERROR_STANDARD_MSG, Toast.LENGTH_SHORT).show());

                callLock = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    // Start the location tracking
                    Intent locationSenderService = new Intent(WorkSessionActivity.this, LocationSenderService.class);
                    locationSenderService.putExtra("interval", Integer.parseInt(response.body().string()));

                    startForegroundService(locationSenderService);

                    runOnUiThread(() ->
                    {
                        alertDialogBuilder.setTitle("Info");
                        alertDialogBuilder.setMessage("The work session has been started.");
                        alertDialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                        alertDialogBuilder.create().show();
                    });

                    response.close();
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    runOnUiThread(() ->
                    {
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Authorization error. Please log in and try again.");
                        alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) ->
                        {
                            Intent intent = new Intent(WorkSessionActivity.this, LoginActivity.class);

                            dialogInterface.dismiss();
                            finish();
                            startActivity(intent);
                        });
                        alertDialogBuilder.create().show();
                    });
                }
                else if (response.code() == 400) // Bad Request, the user already has an active Work Session
                {
                    runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, "You already have an active Work Session at the moment.\n" +
                            "Re-launching the location sender service...", Toast.LENGTH_SHORT).show());

                    Intent locationSenderService = new Intent(WorkSessionActivity.this, LocationSenderService.class);
                    locationSenderService.putExtra("interval", Integer.parseInt(response.body().string()));

                    startForegroundService(locationSenderService);
                }

                callLock = false;
            }
        });
    }

    private void finishWorkSessionOnClickListener(View view)
    {
        if (callLock == true)
        {
            return;
        }

        if (userDTO.isForceStartWorkSession())
        {
            LocalTime startingHours = LocalTime.of(userDTO.getStartingHour(), userDTO.getStartingMinute());
            LocalTime endingHours = LocalTime.of(userDTO.getEndingHour(), userDTO.getEndingMinute());
            LocalTime now = LocalTime.now();

            if (now.isAfter(startingHours) && now.isBefore(endingHours))
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                alertDialogBuilder
                        .setTitle("You have required working hours")
                        .setMessage("You cannot end the work session before the ending hour.\n" +
                                "Please contact Your supervisor for more information.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

                alertDialogBuilder.create().show();

                return;
            }
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WorkSessionActivity.this);
        String jwt = sharedPreferences.getString("Auth", "");

        Request request = new Request.Builder()
                .url(AppConfig.BACKEND_BASE.concat("/worksessions/stop"))
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
                runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, CONNECTION_ERROR_STANDARD_MSG, Toast.LENGTH_SHORT).show());

                callLock = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    // Stop the location tracking
                    Intent locationSenderService = new Intent(WorkSessionActivity.this, LocationSenderService.class);

                    stopService(locationSenderService);
                    startForegroundService(new Intent(WorkSessionActivity.this, AutoStartService.class));
                    AutoStartService.setRunning(true);

                    runOnUiThread(() ->
                    {
                        alertDialogBuilder.setTitle("Info");
                        alertDialogBuilder.setMessage("The work session has been stopped.");
                        alertDialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                        alertDialogBuilder.create().show();
                    });

                    response.close();
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    runOnUiThread(() ->
                    {
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Authorization error. Please log in and try again.");
                        alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) ->
                        {
                            Intent intent = new Intent(WorkSessionActivity.this, LoginActivity.class);

                            dialogInterface.dismiss();
                            finish();
                            startActivity(intent);
                        });
                        alertDialogBuilder.create().show();
                    });
                }
                else if (response.code() == 400) // Bad Request, the user does not have an active Work Session
                {
                    runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, "You do not have an active Work Session at the moment.", Toast.LENGTH_SHORT).show());
                }

                callLock = false;
            }
        });
    }

    private void userProfileOnClickListener(View view)
    {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("user_role", userRole);
        intent.putExtra("nav_first_and_last_name", navFirstNameAndLastName.getText().toString());
        intent.putExtra("email", navEmail.getText().toString());

        startActivity(intent);
    }
}