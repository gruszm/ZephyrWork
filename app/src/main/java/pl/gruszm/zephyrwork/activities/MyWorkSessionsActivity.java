package pl.gruszm.zephyrwork.activities;

import static pl.gruszm.zephyrwork.enums.RoleType.EMPLOYEE;

import android.os.Bundle;
import android.view.View;
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

import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.adapters.MyWorkSessionsAdapter;
import pl.gruszm.zephyrwork.enums.RoleType;
import pl.gruszm.zephyrwork.navigation.MyOnNavigationItemSelectedListener;

public class MyWorkSessionsActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private RoleType role;

    // Layout
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyWorkSessionsAdapter(this, progressBar, role));
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
}