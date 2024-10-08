package pl.gruszm.zephyrwork.viewholders;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.activities.LoginActivity;
import pl.gruszm.zephyrwork.activities.WorkSessionRouteActivity;
import pl.gruszm.zephyrwork.callbacks.OnWorkSessionUpdateCallback;
import pl.gruszm.zephyrwork.config.AppConfig;
import pl.gruszm.zephyrwork.enums.WorkSessionState;
import pl.gruszm.zephyrwork.services.LocationSenderService;

public class WorkSessionViewHolder extends RecyclerView.ViewHolder
{
    private OkHttpClient okHttpClient;
    private SharedPreferences sharedPreferences;
    private Activity activity;
    private int workSessionId;
    public TextView firstNameAndLastNameTv, startingDateTv, endingDateTv, state;
    private ImageButton detailsBtn;
    private OnWorkSessionUpdateCallback onWorkSessionUpdateCallback;
    private boolean isUnderReviewActivity = false;
    private String notesFromSupervisor, notesFromEmployee;
    private WorkSessionState workSessionState;

    public WorkSessionViewHolder(@NonNull View itemView)
    {
        super(itemView);

        okHttpClient = new OkHttpClient();

        firstNameAndLastNameTv = itemView.findViewById(R.id.first_name_and_last_name);
        startingDateTv = itemView.findViewById(R.id.starting_date);
        endingDateTv = itemView.findViewById(R.id.ending_date);
        detailsBtn = itemView.findViewById(R.id.details_icon);
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

        if (notesFromSupervisor != null)
        {
            stringBuilder
                    .append("\n\n")
                    .append("Notes from supervisor:")
                    .append("\n\n")
                    .append(notesFromSupervisor);
        }

        if (notesFromEmployee != null)
        {
            stringBuilder
                    .append("\n\n")
                    .append("Notes from employee:")
                    .append("\n\n")
                    .append(notesFromEmployee);
        }

        alertDialogBuilder.setTitle("Work Session Details");
        alertDialogBuilder.setMessage(stringBuilder.toString());

        alertDialogBuilder.setNeutralButton("VERIFY ON MAP", (DialogInterface var1, int var2) ->
        {
            Intent intent = new Intent(activity, WorkSessionRouteActivity.class);
            intent.putExtra("workSessionId", workSessionId);

            activity.startActivity(intent);
        });

        if (isUnderReviewActivity && workSessionState.equals(WorkSessionState.UNDER_REVIEW))
        {
            alertDialogBuilder.setNegativeButton("RETURN", (DialogInterface dialog, int var2) ->
            {
                dialog.dismiss();
                showReturnReasonDialog();
            });
            alertDialogBuilder.setPositiveButton("APPROVE", (DialogInterface dialog, int var2) ->
            {
                approveWorkSession(dialog);
            });
        }
        else if (!isUnderReviewActivity && workSessionState.equals(WorkSessionState.RETURNED))
        {
            alertDialogBuilder.setPositiveButton("RE-SEND", (dialogInterface, i) ->
            {
                dialogInterface.dismiss();
                showResendDialog();
            });
            alertDialogBuilder.setNegativeButton("CANCEL", (dialogInterface, i) ->
            {
                dialogInterface.dismiss();
                cancelWorkSession();
            });
        }
        else if (!isUnderReviewActivity && (workSessionState.equals(WorkSessionState.IN_PROGRESS) || workSessionState.equals(WorkSessionState.UNDER_REVIEW)))
        {
            alertDialogBuilder.setPositiveButton("CANCEL", (dialogInterface, i) ->
            {
                dialogInterface.dismiss();
                cancelWorkSession();
            });
        }

        alertDialogBuilder.create().show();
    }

    private void cancelWorkSession()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder.setMessage("Are You sure You want to cancel this work session? This cannot be undone.");
        alertDialogBuilder.setNegativeButton("NO", (dialogInterface, i) ->
        {
            dialogInterface.dismiss();
        });
        alertDialogBuilder.setPositiveButton("YES", (dialogInterface, i) ->
        {
            dialogInterface.dismiss();

            RequestBody emptyRequestBody = RequestBody.create(new byte[0]);

            Request request = new Request.Builder()
                    .url(AppConfig.BACKEND_BASE.concat("/worksessions/cancel/").concat(String.valueOf(workSessionId)))
                    .post(emptyRequestBody)
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
                        activity.runOnUiThread(() -> Toast.makeText(activity, "Work session cancelled.", Toast.LENGTH_SHORT).show());
                        onWorkSessionUpdateCallback.updateWorkSessionState(workSessionId, WorkSessionState.CANCELLED);

                        // Shut down the service
                        if (workSessionState.equals(WorkSessionState.IN_PROGRESS))
                        {
                            Intent intent = new Intent(activity, LocationSenderService.class);

                            activity.stopService(intent);
                        }
                    }
                    else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                    {
                        // Show error message and redirect to Login activity
                        activity.runOnUiThread(() ->
                        {
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
        });

        alertDialogBuilder.create().show();
    }

    private void showResendDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder.setTitle("Your Answer");

        // Create EditText
        EditText editText = new EditText(activity);
        editText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setMinLines(3);
        editText.setSingleLine(false);
        editText.setHorizontallyScrolling(false);
        editText.setPadding(40, editText.getPaddingTop(), 40, editText.getPaddingBottom());
        alertDialogBuilder.setView(editText);

        alertDialogBuilder.setPositiveButton("OK", (dialog, which) ->
        {
            dialog.dismiss();

            resendWorkSession(editText.getText().toString());
        });

        alertDialogBuilder.setNeutralButton("CANCEL", (dialog, which) ->
        {
            dialog.dismiss();
        });

        alertDialogBuilder.create().show();
    }

    private void resendWorkSession(String answer)
    {
        RequestBody requestBody = RequestBody.create(
                answer,
                MediaType.get("text/plain; charset=utf-8")
        );

        StringBuilder urlBuilder = new StringBuilder()
                .append(AppConfig.BACKEND_BASE)
                .append("/worksessions/resend/")
                .append(workSessionId);

        Request request = new Request.Builder()
                .post(requestBody)
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
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Work session re-sent for approval.", Toast.LENGTH_SHORT).show());
                    onWorkSessionUpdateCallback.updateWorkSessionState(workSessionId, WorkSessionState.UNDER_REVIEW);
                    onWorkSessionUpdateCallback.updateNotesFromEmployee(workSessionId, answer);
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    // Show error message and redirect to Login activity
                    activity.runOnUiThread(() ->
                    {
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

    private void showReturnReasonDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder.setTitle("Reason");

        // Create EditText
        EditText editText = new EditText(activity);
        editText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setMinLines(3);
        editText.setSingleLine(false);
        editText.setHorizontallyScrolling(false);
        editText.setPadding(40, editText.getPaddingTop(), 40, editText.getPaddingBottom());
        alertDialogBuilder.setView(editText);

        alertDialogBuilder.setPositiveButton("OK", (dialog, which) ->
        {
            dialog.dismiss();

            returnWorkSession(editText.getText().toString());
        });

        alertDialogBuilder.setNeutralButton("CANCEL", (dialog, which) ->
        {
            dialog.dismiss();
        });

        alertDialogBuilder.create().show();
    }

    private void returnWorkSession(String reason)
    {
        RequestBody requestBody = RequestBody.create(
                reason,
                MediaType.get("text/plain; charset=utf-8")
        );

        StringBuilder urlBuilder = new StringBuilder()
                .append(AppConfig.BACKEND_BASE)
                .append("/worksessions/return/")
                .append(workSessionId);

        Request request = new Request.Builder()
                .post(requestBody)
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
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Work session returned.", Toast.LENGTH_SHORT).show());
                    onWorkSessionUpdateCallback.updateWorkSessionState(workSessionId, WorkSessionState.RETURNED);
                    onWorkSessionUpdateCallback.updateNotesFromSupervisor(workSessionId, reason);
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    // Show error message and redirect to Login activity
                    activity.runOnUiThread(() ->
                    {
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
                    onWorkSessionUpdateCallback.updateWorkSessionState(workSessionId, WorkSessionState.APPROVED);
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

    public void setUnderReviewActivity(boolean underReviewActivity)
    {
        isUnderReviewActivity = underReviewActivity;
    }

    public String getNotesFromSupervisor()
    {
        return notesFromSupervisor;
    }

    public void setNotesFromSupervisor(String notesFromSupervisor)
    {
        this.notesFromSupervisor = notesFromSupervisor;
    }

    public String getNotesFromEmployee()
    {
        return notesFromEmployee;
    }

    public void setNotesFromEmployee(String notesFromEmployee)
    {
        this.notesFromEmployee = notesFromEmployee;
    }

    public void setWorkSessionState(WorkSessionState workSessionState)
    {
        this.workSessionState = workSessionState;
    }
}
