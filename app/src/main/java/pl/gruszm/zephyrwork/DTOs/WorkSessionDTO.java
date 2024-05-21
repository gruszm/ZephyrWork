package pl.gruszm.zephyrwork.DTOs;

import pl.gruszm.zephyrwork.enums.WorkSessionState;

public class WorkSessionDTO
{
    private int id;
    private String startTime;
    private String endTime;
    private String employeeName;
    private WorkSessionState workSessionState;

    public int getId()
    {
        return id;
    }

    public WorkSessionDTO setId(int id)
    {
        this.id = id;

        return this;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public WorkSessionDTO setStartTime(String startTime)
    {
        this.startTime = startTime;

        return this;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public WorkSessionDTO setEndTime(String endTime)
    {
        this.endTime = endTime;

        return this;
    }

    public String getEmployeeName()
    {
        return employeeName;
    }

    public WorkSessionDTO setEmployeeName(String employeeName)
    {
        this.employeeName = employeeName;

        return this;
    }

    public WorkSessionState getWorkSessionState()
    {
        return workSessionState;
    }

    public WorkSessionDTO setWorkSessionState(WorkSessionState workSessionState)
    {
        this.workSessionState = workSessionState;

        return this;
    }
}
