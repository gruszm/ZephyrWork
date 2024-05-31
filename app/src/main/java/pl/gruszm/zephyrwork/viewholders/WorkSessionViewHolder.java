package pl.gruszm.zephyrwork.viewholders;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.activities.LoginActivity;
import pl.gruszm.zephyrwork.activities.RegisterNewEmployeeActivity;
import pl.gruszm.zephyrwork.activities.WorkSessionRouteActivity;
import pl.gruszm.zephyrwork.callbacks.OnWorkSessionUpdateCallback;
import pl.gruszm.zephyrwork.config.AppConfig;
import pl.gruszm.zephyrwork.enums.RoleType;

public class WorkSessionViewHolder extends RecyclerView.ViewHolder
{
    private OkHttpClient okHttpClient;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    private Activity activity;
    private RoleType userRole;
    private int workSessionId;
    public TextView firstNameAndLastNameTv, startingDateTv, endingDateTv, state;
    private Button detailsBtn;
    private OnWorkSessionUpdateCallback onWorkSessionUpdateCallback;

    public WorkSessionViewHolder(@NonNull View itemView)
    {
        super(itemView);

        okHttpClient = new OkHttpClient();
        gson = new Gson();

        firstNameAndLastNameTv = itemView.findViewById(R.id.first_name_and_last_name);
        startingDateTv = itemView.findViewById(R.id.starting_date);
        endingDateTv = itemView.findViewById(R.id.ending_date);
        detailsBtn = itemView.findViewById(R.id.details);
        state = itemView.findViewById(R.id.state);

        detailsBtn.setOnClickListener(this::detailsOnClickListener);
    }

    private void detailsOnClickListener(View view)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append(firstNameAndLastNameTv.getText())
                .append("\n")
                .append(startingDateTv.getText())
                .append("\n")
                .append(endingDateTv.getText());

        alertDialogBuilder.setTitle("Work Session Details");
        alertDialogBuilder.setMessage(stringBuilder.toString());

        alertDialogBuilder.setNeutralButton("VERIFY ON MAP", (DialogInterface var1, int var2) ->
        {
            Intent intent = new Intent(activity, WorkSessionRouteActivity.class);
            intent.putExtra("workSessionId", workSessionId);

            activity.startActivity(intent);
        });

        if (!userRole.equals(RoleType.EMPLOYEE))
        {
            alertDialogBuilder.setNegativeButton("RETURN", (DialogInterface var1, int var2) ->
            {
                Toast.makeText(activity, "RETURN", Toast.LENGTH_SHORT).show();
            });
            alertDialogBuilder.setPositiveButton("APPROVE", (DialogInterface var1, int var2) ->
            {
                approveWorkSession(var1);
            });
        }

        alertDialogBuilder.create().show();
    }

    private void approveWorkSession(DialogInterface dialogInterface)
    {
        RequestBody emptyRequestBody = RequestBody.create(new byte[0]);

        StringBuilder urlBuilder = new StringBuilder()
                .append(AppConfig.BACKEND_BASE)
                .append("/worksessions/approve/")
                .append(workSessionId);

        Request request = new Request.Builder()
                .post(emptyRequestBody)
                .url(urlBuilder.toString())
                .header("Auth", sharedPreferences.getString("Auth", ""))
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                activity.runOnUiThread(() -> Toast.makeText(activity, AppConfig.CONNECTION_ERROR_STANDARD_MSG, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Work session approved successfully.", Toast.LENGTH_SHORT).show());
                    dialogInterface.dismiss();
                    onWorkSessionUpdateCallback.updateWorkSession(workSessionId);
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    // Show error message and redirect to Login activity
                    activity.runOnUiThread(() ->
                    {
                        dialogInterface.dismiss();

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Authorization error. Please log in and try again.");
                        alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) ->
                        {
                            Intent intent = new Intent(activity, LoginActivity.class);

                            dialogInterface.dismiss();
                            activity.finish();
                            activity.startActivity(intent);
                        });
                        alertDialogBuilder.create().show();
                    });
                }
                else
                {
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Unknown error occurred. Please try again.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    public void setUserRole(RoleType userRole)
    {
        this.userRole = userRole;
    }

    public void setActivityAndSharedPreferences(Activity activity)
    {
        this.activity = activity;

        sharedPreferences = activity.getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
    }

    public void setWorkSessionId(int workSessionId)
    {
        this.workSessionId = workSessionId;
    }

    public void setOnWorkSessionUpdateCallback(OnWorkSessionUpdateCallback onWorkSessionUpdateCallback)
    {
        this.onWorkSessionUpdateCallback = onWorkSessionUpdateCallback;
    }
}
