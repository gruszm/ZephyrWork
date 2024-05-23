package pl.gruszm.zephyrwork.activities;

import static pl.gruszm.zephyrwork.config.AppConfig.CONNECTION_ERROR_STANDARD_MSG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.gruszm.zephyrwork.DTOs.LocationDTO;
import pl.gruszm.zephyrwork.DTOs.UserDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.config.AppConfig;
import pl.gruszm.zephyrwork.enums.RoleType;

public class WorkSessionActivity extends AppCompatActivity implements LocationListener, NavigationView.OnNavigationItemSelectedListener
{
    // Constants
    private static final int LOCATION_TRACKING_DELAY_MS = 5000;

    // Common
    private OkHttpClient okHttpClient;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean callLock;

    // Buttons
    private Button startWorkSessionBtn, finishWorkSessionBtn, userProfileBtn, logoutBtn, registerNewEmployeeBtn;

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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
        startWorkSessionBtn = findViewById(R.id.start_work_session_btn);
        finishWorkSessionBtn = findViewById(R.id.finish_work_session_btn);
        userProfileBtn = findViewById(R.id.user_profile_btn);
        logoutBtn = findViewById(R.id.logout_btn);
        registerNewEmployeeBtn = findViewById(R.id.register_new_employee_btn);

        // OnClickListeners
        startWorkSessionBtn.setOnClickListener(this::startWorkSessionOnClickListener);
        finishWorkSessionBtn.setOnClickListener(this::finishWorkSessionOnClickListener);
        userProfileBtn.setOnClickListener(this::userProfileOnClickListener);
        logoutBtn.setOnClickListener(this::logoutOnClickListener);
        registerNewEmployeeBtn.setOnClickListener(this::registerNewEmployeeListener);

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

            startActivity(intent);
        }
        else if (id == R.id.employees_work_sessions)
        {
            Intent intent = new Intent(this, EmployeesWorkSessionsActivity.class);
            intent.putExtra("role", userRole);

            startActivity(intent);
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

                    // Regular employees cannot register new employees
                    if (!userDTO.getRoleName().equals(RoleType.EMPLOYEE.name()))
                    {
                        runOnUiThread(() -> registerNewEmployeeBtn.setVisibility(View.VISIBLE));
                    }

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

            // Suppressed, because permission is checked at the beginning of the function
            @SuppressLint("MissingPermission")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    // Start the location tracking
                    WorkSessionActivity.this.fusedLocationProviderClient.requestLocationUpdates(
                            new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_TRACKING_DELAY_MS).build(),
                            WorkSessionActivity.this,
                            Looper.getMainLooper()
                    );

                    response.close();
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    runOnUiThread(() ->
                    {
                        Intent intent = new Intent(WorkSessionActivity.this, LoginActivity.class);

                        finish();
                        startActivity(intent);
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
                    fusedLocationProviderClient.removeLocationUpdates(WorkSessionActivity.this);

                    response.close();
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    runOnUiThread(() ->
                    {
                        Intent intent = new Intent(WorkSessionActivity.this, LoginActivity.class);

                        finish();
                        startActivity(intent);
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

    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        String jwt = sharedPreferences.getString("Auth", "");
        LocationDTO locationDTO = new LocationDTO(LocalDateTime.now().toString(), location.getLatitude(), location.getLongitude());
        RequestBody requestBody = RequestBody.create(gson.toJson(locationDTO), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(AppConfig.BACKEND_BASE.concat("/locations/token"))
                .post(requestBody)
                .header("Auth", jwt)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, "Connection error. The locations are still being saved in offline mode and will be synchronized when possible.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    runOnUiThread(() ->
                    {
                        Toast.makeText(WorkSessionActivity.this, "Your location has been saved for the active work session.", Toast.LENGTH_SHORT).show();
                    });

                    response.close();
                }
                else if (response.code() == 400)
                {
                    runOnUiThread(() ->
                    {
                        Toast.makeText(WorkSessionActivity.this, "You do not have an active work session." +
                                " Locations tracking has been turned off.", Toast.LENGTH_SHORT).show();
                    });

                    fusedLocationProviderClient.removeLocationUpdates(WorkSessionActivity.this);
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    runOnUiThread(() ->
                    {
                        Intent intent = new Intent(WorkSessionActivity.this, LoginActivity.class);

                        // Show error message and redirect to Login activity
                        Toast.makeText(WorkSessionActivity.this, "Authorization error. The locations are still being saved in offline mode and will be synchronized once you log in.", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(intent);

                        // TODO: Add the location to a list for a later synchronization.
                    });
                }
            }
        });
    }

    private void userProfileOnClickListener(View view)
    {
        Intent intent = new Intent(this, UserProfileActivity.class);

        startActivity(intent);
    }
}