package pl.gruszm.zephyrwork.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pl.gruszm.zephyrwork.DTOs.WorkSessionDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.callbacks.OnWorkSessionUpdateCallback;
import pl.gruszm.zephyrwork.enums.RoleType;
import pl.gruszm.zephyrwork.enums.WorkSessionState;
import pl.gruszm.zephyrwork.viewholders.WorkSessionViewHolder;

public class WorkSessionsUnderReviewAdapter extends RecyclerView.Adapter<WorkSessionViewHolder> implements OnWorkSessionUpdateCallback
{
    // Common
    private Activity activity;
    private DateTimeFormatter formatter;
    private RoleType role;

    // List of Work Sessions
    private List<WorkSessionDTO> workSessionDTOs;
    private List<WorkSessionDTO> filteredWorkSessionDTOs;
    private WorkSessionState workSessionFilter;
    private LocalDateTime startingDateFilter;
    private LocalDateTime endingDateFilter;

    // Work Session State Display
    private Map<WorkSessionState, String> workSessionNames;
    private Map<WorkSessionState, Integer> workSessionColors;

    public WorkSessionsUnderReviewAdapter(Activity activity, List<WorkSessionDTO> workSessionDTOs, RoleType role)
    {
        this.activity = activity;
        this.workSessionDTOs = workSessionDTOs;
        this.role = role;

        filteredWorkSessionDTOs = this.workSessionDTOs;

        // Work Session State Display
        workSessionNames = new HashMap<>();
        workSessionColors = new HashMap<>();

        workSessionNames.put(WorkSessionState.IN_PROGRESS, "IN PROGRESS");
        workSessionNames.put(WorkSessionState.UNDER_REVIEW, "UNDER REVIEW");
        workSessionNames.put(WorkSessionState.APPROVED, "APPROVED");
        workSessionNames.put(WorkSessionState.RETURNED, "RETURNED");
        workSessionNames.put(WorkSessionState.CANCELLED, "CANCELLED");

        workSessionColors.put(WorkSessionState.IN_PROGRESS, R.color.yellow); // Yellow
        workSessionColors.put(WorkSessionState.UNDER_REVIEW, R.color.blue); // Blue
        workSessionColors.put(WorkSessionState.APPROVED, R.color.green); // Green
        workSessionColors.put(WorkSessionState.RETURNED, R.color.magenta); // Magenta
        workSessionColors.put(WorkSessionState.CANCELLED, R.color.red); // Red

        // Common
        formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    }

    @NonNull
    @Override
    public WorkSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.work_session_item, parent, false);

        return new WorkSessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkSessionViewHolder holder, int position)
    {
        WorkSessionDTO workSessionDTO = filteredWorkSessionDTOs.get(position);

        String startTime = "Start: " + LocalDateTime.parse(workSessionDTO.getStartTime()).format(formatter).toString();
        String endTime = "";

        if (workSessionDTO.getEndTime() != null)
        {
            endTime = "End: " + LocalDateTime.parse(workSessionDTO.getEndTime()).format(formatter).toString();
        }

        holder.setWorkSessionState(workSessionDTO.getWorkSessionState());
        holder.setUnderReviewActivity(true);
        holder.setOnWorkSessionUpdateCallback(this);
        holder.setActivityAndSharedPreferences(activity);
        holder.setWorkSessionId(workSessionDTO.getId());
        holder.firstNameAndLastNameTv.setText(workSessionDTO.getEmployeeName());
        holder.startingDateTv.setText(startTime);
        holder.endingDateTv.setText(endTime);
        holder.setNotesFromSupervisor(workSessionDTO.getNotesFromSupervisor());
        holder.setNotesFromEmployee(workSessionDTO.getNotesFromEmployee());
        holder.state.setText(workSessionNames.get(workSessionDTO.getWorkSessionState()));
        holder.state.setTextColor(ContextCompat.getColor(activity, workSessionColors.get(workSessionDTO.getWorkSessionState())));
    }

    @Override
    public int getItemCount()
    {
        return filteredWorkSessionDTOs.size();
    }

    @Override
    public void removeWorkSession(int workSessionId)
    {
        List<WorkSessionDTO> workSessionToRemove = filteredWorkSessionDTOs.stream().filter(ws -> (ws.getId() == workSessionId)).collect(Collectors.toList());

        if (workSessionToRemove.size() == 0)
        {
            return;
        }

        int positionToRemove = filteredWorkSessionDTOs.indexOf(workSessionToRemove.get(0));

        WorkSessionDTO workSessionRemoved = filteredWorkSessionDTOs.remove(positionToRemove);
        workSessionDTOs.remove(workSessionRemoved);
        activity.runOnUiThread(() -> notifyItemRemoved(positionToRemove));
    }

    @Override
    public void updateWorkSessionState(int workSessionId, WorkSessionState workSessionState)
    {
        List<WorkSessionDTO> workSessionToUpdate = filteredWorkSessionDTOs.stream().filter(ws -> (ws.getId() == workSessionId)).collect(Collectors.toList());

        if (workSessionToUpdate.size() == 0)
        {
            return;
        }

        int positionToUpdate = filteredWorkSessionDTOs.indexOf(workSessionToUpdate.get(0));

        workSessionToUpdate.get(0).setWorkSessionState(workSessionState);
        activity.runOnUiThread(() -> notifyItemChanged(positionToUpdate));

        applyFilters();
    }

    @Override
    public void updateNotesFromEmployee(int workSessionId, String notesFromEmployee)
    {
        List<WorkSessionDTO> workSessionToUpdate = filteredWorkSessionDTOs.stream().filter(ws -> (ws.getId() == workSessionId)).collect(Collectors.toList());

        if (workSessionToUpdate.size() == 0)
        {
            return;
        }

        int positionToUpdate = filteredWorkSessionDTOs.indexOf(workSessionToUpdate.get(0));

        workSessionToUpdate.get(0).setNotesFromEmployee(notesFromEmployee);
        activity.runOnUiThread(() -> notifyItemChanged(positionToUpdate));
    }

    @Override
    public void updateNotesFromSupervisor(int workSessionId, String notesFromSupervisor)
    {
        List<WorkSessionDTO> workSessionToUpdate = filteredWorkSessionDTOs.stream().filter(ws -> (ws.getId() == workSessionId)).collect(Collectors.toList());

        if (workSessionToUpdate.size() == 0)
        {
            return;
        }

        int positionToUpdate = filteredWorkSessionDTOs.indexOf(workSessionToUpdate.get(0));

        workSessionToUpdate.get(0).setNotesFromSupervisor(notesFromSupervisor);
        workSessionToUpdate.get(0).setNotesFromEmployee(null);
        activity.runOnUiThread(() -> notifyItemChanged(positionToUpdate));
    }

    public void setWorkSessionFilter(WorkSessionState workSessionFilter)
    {
        this.workSessionFilter = workSessionFilter;

        applyFilters();
    }

    public void setStartingDateFilter(int year, int month, int dayOfMonth)
    {
        LocalDateTime startingDateFilter = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0);
        this.startingDateFilter = startingDateFilter;

        applyFilters();
    }

    public void setEndingDateFilter(int year, int month, int dayOfMonth)
    {
        LocalDateTime endingDateFilter = LocalDateTime.of(year, month + 1, dayOfMonth, 23, 59);
        this.endingDateFilter = endingDateFilter;

        applyFilters();
    }

    private void applyFilters()
    {
        Stream<WorkSessionDTO> stream = workSessionDTOs.stream();

        stream = (this.workSessionFilter != null) ? (stream.filter(ws -> ws.getWorkSessionState().equals(this.workSessionFilter))) : stream;
        stream = (this.startingDateFilter != null) ? (stream.filter(ws -> LocalDateTime.parse(ws.getStartTime()).isAfter(this.startingDateFilter))) : stream;
        stream = (this.endingDateFilter != null) ? (stream.filter(ws -> LocalDateTime.parse(ws.getStartTime()).isBefore(this.endingDateFilter))) : stream;

        filteredWorkSessionDTOs = stream.collect(Collectors.toList());

        activity.runOnUiThread(() -> notifyDataSetChanged());
    }

    public void clearDatesFilter()
    {
        startingDateFilter = null;
        endingDateFilter = null;

        applyFilters();
    }
}
