package pl.gruszm.zephyrwork.DTOs;

public class WorkSessionDTO
{
    private int id;
    private String startTime;
    private String endTime;
    private String employeeName;

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
}
