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

    public void setId(int id)
    {
        this.id = id;
    }

    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }

    public void setEmployeeName(String employeeName)
    {
        this.employeeName = employeeName;
    }

    public void setWorkSessionState(WorkSessionState workSessionState)
    {
        this.workSessionState = workSessionState;
    }

    public void setNotesFromSupervisor(String notesFromSupervisor)
    {
        this.notesFromSupervisor = notesFromSupervisor;
    }

    public void setNotesFromEmployee(String notesFromEmployee)
    {
        this.notesFromEmployee = notesFromEmployee;
    }
}
