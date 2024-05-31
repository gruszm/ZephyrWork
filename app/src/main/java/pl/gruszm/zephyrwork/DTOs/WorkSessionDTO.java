package pl.gruszm.zephyrwork.DTOs;

import pl.gruszm.zephyrwork.enums.WorkSessionState;

public class WorkSessionDTO
{
    private int id;
    private String startTime;
    private String endTime;
    private String employeeName;
    private WorkSessionState workSessionState;
    private String notesFromSupervisor;
    private String notesFromEmployee;

    public int getId()
    {
        return id;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public String getEmployeeName()
    {
        return employeeName;
    }

    public WorkSessionState getWorkSessionState()
    {
        return workSessionState;
    }

    public String getNotesFromSupervisor()
    {
        return notesFromSupervisor;
    }

    public String getNotesFromEmployee()
    {
        return notesFromEmployee;
    }
}
