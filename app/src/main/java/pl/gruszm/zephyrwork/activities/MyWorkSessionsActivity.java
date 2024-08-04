package pl.gruszm.zephyrwork.activities;

import static pl.gruszm.zephyrwork.enums.RoleType.EMPLOYEE;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;

import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.adapters.MyWorkSessionsAdapter;
import pl.gruszm.zephyrwork.enums.RoleType;
import pl.gruszm.zephyrwork.enums.WorkSessionState;
import pl.gruszm.zephyrwork.navigation.MyOnNavigationItemSelectedListener;

public class MyWorkSessionsActivity extends AppCompatActivity
{
    private MyWorkSessionsAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private RoleType role;

    // Layout
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private TextView filterAllTv, filterInProgressTv, filterUnderReviewTv, filterApprovedTv,
            filterReturnedTv, filterCancelledTv, startingDateTv, endingDateTv;
    private ImageButton clearDatesFilterBtn;

    // Navigation Header Views
    private TextView navFirstNameAndLastName, navEmail;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_work_sessions);

        // Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        // Filter TextViews/ImageButtons
        filterAllTv = findViewById(R.id.filter_all);
        filterInProgressTv = findViewById(R.id.filter_in_progress);
        filterUnderReviewTv = findViewById(R.id.filter_under_review);
        filterApprovedTv = findViewById(R.id.filter_approved);
        filterReturnedTv = findViewById(R.id.filter_returned);
        filterCancelledTv = findViewById(R.id.filter_cancelled);
        startingDateTv = findViewById(R.id.starting_date);
        endingDateTv = findViewById(R.id.ending_date);
        clearDatesFilterBtn = findViewById(R.id.clear_date_button);

        filterAllTv.setOnClickListener(new FilterOnClickListener(null));
        filterInProgressTv.setOnClickListener(new FilterOnClickListener(WorkSessionState.IN_PROGRESS));
        filterUnderReviewTv.setOnClickListener(new FilterOnClickListener(WorkSessionState.UNDER_REVIEW));
        filterApprovedTv.setOnClickListener(new FilterOnClickListener(WorkSessionState.APPROVED));
        filterReturnedTv.setOnClickListener(new FilterOnClickListener(WorkSessionState.RETURNED));
        filterCancelledTv.setOnClickListener(new FilterOnClickListener(WorkSessionState.CANCELLED));
        startingDateTv.setOnClickListener(this::startingDateOnClickListener);
        endingDateTv.setOnClickListener(this::endingDateOnClickListener);
        clearDatesFilterBtn.setOnClickListener(this::clearDatesFilterOnClickListener);

        // Configure navigation
        setSupportActionBar(toolbar);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation Header Views
        navFirstNameAndLastName = navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
        navEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        // Toolbar and navigation handling
        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            role = RoleType.valueOf(extras.getString("user_role", EMPLOYEE.name()));
            userRole = extras.getString("user_role");
            navFirstNameAndLastName.setText(extras.getString("nav_first_and_last_name", ""));
            navEmail.setText(extras.getString("email", ""));
        }

        MyOnNavigationItemSelectedListener itemSelectedListener = new MyOnNavigationItemSelectedListener(
                this,
                userRole,
                navFirstNameAndLastName.getText().toString(),
                navEmail.getText().toString(),
                drawerLayout
        );
        toolbar.setNavigationOnClickListener(this::navigationOnClickListener);
        navigationView.setNavigationItemSelectedListener(itemSelectedListener);

        adapter = new MyWorkSessionsAdapter(this, progressBar, role);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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

    private class FilterOnClickListener implements View.OnClickListener
    {
        private WorkSessionState workSessionState;

        public FilterOnClickListener(WorkSessionState workSessionState)
        {
            this.workSessionState = workSessionState;
        }

        @Override
        public void onClick(View v)
        {
            adapter.setWorkSessionFilter(workSessionState);
        }
    }

    private void startingDateOnClickListener(View v)
    {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) ->
                {
                    adapter.setStartingDateFilter(year, month, dayOfMonth);
                    clearDatesFilterBtn.setVisibility(View.VISIBLE);
                    startingDateTv.setText(String.format("Starting date\n%04d-%02d-%02d", year, month + 1, dayOfMonth));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void endingDateOnClickListener(View v)
    {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) ->
                {
                    adapter.setEndingDateFilter(year, month, dayOfMonth);
                    clearDatesFilterBtn.setVisibility(View.VISIBLE);
                    endingDateTv.setText(String.format("Ending date\n%04d-%02d-%02d", year, month + 1, dayOfMonth));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }


    private void clearDatesFilterOnClickListener(View view)
    {
        adapter.clearDatesFilter();
        clearDatesFilterBtn.setVisibility(View.INVISIBLE);
        startingDateTv.setText("Starting date");
        endingDateTv.setText("Ending date");
    }
}