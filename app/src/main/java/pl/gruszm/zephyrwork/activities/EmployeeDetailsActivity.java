package pl.gruszm.zephyrwork.activities;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.gruszm.zephyrwork.DTOs.SubordinateEmpDataDTO;
import pl.gruszm.zephyrwork.DTOs.UserDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.config.AppConfig;
import pl.gruszm.zephyrwork.enums.RoleType;
import pl.gruszm.zephyrwork.navigation.MyOnNavigationItemSelectedListener;

public class EmployeeDetailsActivity extends AppCompatActivity
{
    public static final int MIN_INTERVAL = 5;

    private UserDTO userDTO;
    private TextView email, firstNameAndLastName, role, startingHourTv, endingHourTv;
    private EditText intervalEt;
    private ImageButton saveBtn;
    private SubordinateEmpDataDTO subordinateEmpDataDTO;
    private CheckBox forceStartWorkSessionCheckbox;

    // Layout
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

    // Navigation Header Views
    private TextView navFirstNameAndLastName, navEmail;
    private String supervisorRole;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_details);

        // Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        email = findViewById(R.id.employee_email);
        firstNameAndLastName = findViewById(R.id.employee_first_name_and_last_name);
        role = findViewById(R.id.employee_role);
        saveBtn = findViewById(R.id.save_interval_button);
        intervalEt = findViewById(R.id.employee_location_registration_interval_et);
        startingHourTv = findViewById(R.id.starting_hour);
        endingHourTv = findViewById(R.id.ending_hour);
        forceStartWorkSessionCheckbox = findViewById(R.id.force_start_work_session_checkbox);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();

            userDTO = new Gson().fromJson(extras.getString("UserDTO"), UserDTO.class);

            email.setText(userDTO.getEmail());
            firstNameAndLastName.setText(userDTO.getFirstName() + " " + userDTO.getLastName());
            role.setText(userDTO.getRoleName());
            intervalEt.setText(String.valueOf(userDTO.getLocationRegistrationInterval()));
            forceStartWorkSessionCheckbox.setChecked(userDTO.isForceStartWorkSession());

            subordinateEmpDataDTO = new SubordinateEmpDataDTO(
                    userDTO.getStartingHour(),
                    userDTO.getStartingMinute(),
                    userDTO.getEndingHour(),
                    userDTO.getEndingMinute(),
                    userDTO.getLocationRegistrationInterval(),
                    userDTO.isForceStartWorkSession()
            );

            startingHourTv.setText(String.format("%02d:%02d", subordinateEmpDataDTO.getStartingHour(), subordinateEmpDataDTO.getStartingMinute()));
            endingHourTv.setText(String.format("%02d:%02d", subordinateEmpDataDTO.getEndingHour(), subordinateEmpDataDTO.getEndingMinute()));
        }

        startingHourTv.setOnClickListener(this::startingHourOnClickListener);
        endingHourTv.setOnClickListener(this::endingHourOnClickListener);
        saveBtn.setOnClickListener(this::saveOnClickListener);
        saveBtn.setVisibility(View.GONE);

        forceStartWorkSessionCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            subordinateEmpDataDTO.setForceStartWorkSession(isChecked);
            saveBtn.setVisibility(View.VISIBLE);
        });

        intervalEt.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                int newInterval = (editable.toString().isEmpty()) ? 0 : Integer.parseInt(editable.toString());

                subordinateEmpDataDTO.setLocationRegistrationInterval(newInterval);
                saveBtn.setVisibility(View.VISIBLE);
            }
        });

        // Configure navigation
        setSupportActionBar(toolbar);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation Header Views
        navFirstNameAndLastName = navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
        navEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);

        // Toolbar and navigation handling
        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            supervisorRole = extras.getString("supervisor_role", RoleType.MANAGER.name());
            navFirstNameAndLastName.setText(extras.getString("supervisor_first_and_last_name", ""));
            navEmail.setText(extras.getString("supervisor_email", ""));
        }

        MyOnNavigationItemSelectedListener itemSelectedListener = new MyOnNavigationItemSelectedListener(
                this,
                supervisorRole,
                navFirstNameAndLastName.getText().toString(),
                navEmail.getText().toString(),
                drawerLayout
        );
        toolbar.setNavigationOnClickListener(this::navigationOnClickListener);
        navigationView.setNavigationItemSelectedListener(itemSelectedListener);
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

    private void startingHourOnClickListener(View view)
    {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view1, hourOfDay, minute) ->
                {
                    String[] endingHoursAndMinutes = endingHourTv.getText().toString().split(":");
                    int endingHour = Integer.parseInt(endingHoursAndMinutes[0]);
                    int endingMinute = Integer.parseInt(endingHoursAndMinutes[1]);

                    if ((hourOfDay > endingHour) || ((hourOfDay == endingHour) && (minute >= endingMinute)))
                    {
                        Toast.makeText(this, "Starting time must be before ending time.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        startingHourTv.setText(String.format("%02d:%02d", hourOfDay, minute));
                        saveBtn.setVisibility(View.VISIBLE);
                        subordinateEmpDataDTO.setStartingHour(hourOfDay);
                        subordinateEmpDataDTO.setStartingMinute(minute);
                    }
                },
                userDTO.getStartingHour(),
                userDTO.getStartingMinute(),
                true);

        timePickerDialog.show();
    }

    private void endingHourOnClickListener(View view)
    {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view1, hourOfDay, minute) ->
                {
                    String[] startingHoursAndMinutes = startingHourTv.getText().toString().split(":");
                    int startingHour = Integer.parseInt(startingHoursAndMinutes[0]);
                    int startingMinute = Integer.parseInt(startingHoursAndMinutes[1]);

                    if ((hourOfDay < startingHour) || ((hourOfDay == startingHour) && (minute <= startingMinute)))
                    {
                        Toast.makeText(this, "Ending time must be after starting time.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        endingHourTv.setText(String.format("%02d:%02d", hourOfDay, minute));
                        saveBtn.setVisibility(View.VISIBLE);
                        subordinateEmpDataDTO.setEndingHour(hourOfDay);
                        subordinateEmpDataDTO.setEndingMinute(minute);
                    }
                },
                userDTO.getEndingHour(),
                userDTO.getEndingMinute(),
                true);

        timePickerDialog.show();
    }

    private void saveOnClickListener(View view)
    {
        if (intervalEt.getText().toString().isEmpty() || (Integer.parseInt(intervalEt.getText().toString()) < MIN_INTERVAL))
        {
            Toast.makeText(this, "Interval cannot be lower than " + MIN_INTERVAL, Toast.LENGTH_SHORT).show();

            return;
        }

        Gson gson = new Gson();
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        RequestBody requestBody = RequestBody.create(gson.toJson(subordinateEmpDataDTO), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .put(requestBody)
                .url(AppConfig.BACKEND_BASE
                        .concat("/users/subordinates/update/")
                        .concat(String.valueOf(userDTO.getId())))
                .header("Auth", sharedPreferences.getString("Auth", ""))
                .build();
        Call call = new OkHttpClient().newCall(request);

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
                    runOnUiThread(() ->
                    {
                        saveBtn.setVisibility(View.GONE);
                        Toast.makeText(EmployeeDetailsActivity.this, "Subordinate employee updated.", Toast.LENGTH_SHORT).show();
                    });
                }
                else
                {
                    runOnUiThread(() -> Toast.makeText(EmployeeDetailsActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}