package pl.gruszm.zephyrwork.navigation;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.activities.MyWorkSessionsActivity;
import pl.gruszm.zephyrwork.activities.RegisterNewEmployeeActivity;
import pl.gruszm.zephyrwork.activities.WorkSessionsUnderReviewActivity;
import pl.gruszm.zephyrwork.enums.RoleType;

public class MyOnNavigationItemSelectedListener implements NavigationView.OnNavigationItemSelectedListener
{
    private Activity activity;
    private String userRole;
    private String firstNameAndLastName;
    private String email;
    private DrawerLayout drawerLayout;

    public MyOnNavigationItemSelectedListener(Activity activity, String userRole, String firstNameAndLastName, String email, DrawerLayout drawerLayout)
    {
        this.activity = activity;
        this.userRole = userRole;
        this.firstNameAndLastName = firstNameAndLastName;
        this.email = email;
        this.drawerLayout = drawerLayout;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
    {
        int id = menuItem.getItemId();

        if (id == R.id.my_work_sessions)
        {
            Intent intent = new Intent(activity, MyWorkSessionsActivity.class);
            intent.putExtra("role", userRole);
            intent.putExtra("nav_first_and_last_name", firstNameAndLastName);
            intent.putExtra("email", email);

            activity.startActivity(intent);
        }
        else if (id == R.id.employees_work_sessions)
        {
            if (userRole.equals(RoleType.EMPLOYEE.name()))
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                alertDialogBuilder.setTitle("ERROR");
                alertDialogBuilder.setMessage("This action is not available for regular employees.");
                alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
                alertDialogBuilder.create().show();
            }
            else
            {
                Intent intent = new Intent(activity, WorkSessionsUnderReviewActivity.class);
                intent.putExtra("role", userRole);
                intent.putExtra("nav_first_and_last_name", firstNameAndLastName);
                intent.putExtra("email", email);

                activity.startActivity(intent);
            }
        }
        else if (id == R.id.register_new_employee)
        {
            if (userRole.equals(RoleType.EMPLOYEE.name()))
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                alertDialogBuilder.setTitle("ERROR");
                alertDialogBuilder.setMessage("This action is not available for regular employees.");
                alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
                alertDialogBuilder.create().show();
            }
            else
            {
                Intent intent = new Intent(activity, RegisterNewEmployeeActivity.class);
                intent.putExtra("user_role", userRole);
                intent.putExtra("nav_first_and_last_name", firstNameAndLastName);
                intent.putExtra("email", email);

                activity.startActivity(intent);
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
}
