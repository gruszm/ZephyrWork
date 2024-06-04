package pl.gruszm.zephyrwork.activities;

import static pl.gruszm.zephyrwork.config.AppConfig.CONNECTION_ERROR_STANDARD_MSG;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import pl.gruszm.zephyrwork.services.LocationSenderService;

public class WorkSessionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    // Common
    private OkHttpClient okHttpClient;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    private boolean callLock;

    // Buttons
    private ImageButton startWorkSessionBtn, finishWorkSessionBtn, userProfileBtn, logoutBtn;

    // Layout
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

    // Navigation Header Views
    private TextView firstNameAndLastName, email;

    // Other
    private String userRole;

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
        firstNameAndLastName = navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
        email = navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);

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

        // Toolbar and navigation handling
        toolbar.setNavigationOnClickListener(this::navigationOnClickListener);
        navigationView.setNavigationItemSelectedListener(this);

        // Enable button for managers and the CEO, update data in the navigation header
        checkUserDataAndUpdateView();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
    {
        int id = menuItem.getItemId();

        if (id == R.id.my_work_sessions)
        {
            Intent intent = new Intent(this, MyWorkSessionsActivity.class);
            intent.putExtra("role", userRole);

            startActivity(intent);
        }
        else if (id == R.id.employees_work_sessions)
        {
            if (userRole.equals(RoleType.EMPLOYEE.name()))
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("ERROR");
                alertDialogBuilder.setMessage("This action is not available for regular employees.");
                alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
                alertDialogBuilder.create().show();
            }
            else
            {
                Intent intent = new Intent(this, WorkSessionsUnderReviewActivity.class);
                intent.putExtra("role", userRole);

                startActivity(intent);
            }
        }
        else if (id == R.id.register_new_employee)
        {
            if (userRole.equals(RoleType.EMPLOYEE.name()))
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("ERROR");
                alertDialogBuilder.setMessage("This action is not available for regular employees.");
                alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
                alertDialogBuilder.create().show();
            }
            else
            {
                Intent intent = new Intent(this, RegisterNewEmployeeActivity.class);
                intent.putExtra("user_role", userRole);

                startActivity(intent);
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
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

    private void registerNewEmployeeListener(View view)
    {
        Intent intent = new Intent(this, RegisterNewEmployeeActivity.class);
        intent.putExtra("user_role", userRole);

        startActivity(intent);
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
                        firstNameAndLastName.setText(userDTO.getFirstName()
                                .concat(" ")
                                .concat(userDTO.getLastName()));

                        email.setText(userDTO.getEmail());
                    });

                    userRole = userDTO.getRoleName();

                    response.close();
                }
            }
        });
    }

    private void logoutOnClickListener(View view)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Intent intent = new Intent(this, LoginActivity.class);

        editor.remove("Auth");
        editor.apply();

        finish();
        startActivity(intent);
    }

    private void startWorkSessionOnClickListener(View view)
    {
        // Check permissions for location
        if ((checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED))
        {
            requestPermissions(Arrays.asList(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).toArray(new String[0]), AppConfig.LOCATION_CODE);

            return;
        }

        if (callLock == true)
        {
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

                    startService(locationSenderService);

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
                    runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, "You already have an active Work Session at the moment.", Toast.LENGTH_SHORT).show());
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

        startActivity(intent);
    }
}