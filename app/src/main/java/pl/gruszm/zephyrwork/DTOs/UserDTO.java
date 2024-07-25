package pl.gruszm.zephyrwork.DTOs;

public class UserDTO
{
    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private Integer supervisorId;
    private String roleName;
    private int locationRegistrationInterval;
    private int startingHour;
    private int startingMinute;
    private int endingHour;
    private int endingMinute;
    private boolean forceStartWorkSession;

    public int getId()
    {
        return id;
    }

    public UserDTO setId(int id)
    {
        this.id = id;

        return this;
    }

    public String getEmail()
    {
        return email;
    }

    public UserDTO setEmail(String email)
    {
        this.email = email;

        return this;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public UserDTO setFirstName(String firstName)
    {
        this.firstName = firstName;

        return this;
    }

    public String getLastName()
    {
        return lastName;
    }

    public UserDTO setLastName(String lastName)
    {
        this.lastName = lastName;

        return this;
    }

    public Integer getSupervisorId()
    {
        return supervisorId;
    }

    public UserDTO setSupervisorId(Integer supervisorId)
    {
        this.supervisorId = supervisorId;

        return this;
    }

    public String getRoleName()
    {
        return roleName;
    }

    public UserDTO setRoleName(String roleName)
    {
        this.roleName = roleName;

        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(getFirstName())
                .append(" ")
                .append(getLastName())
                .append(", ")
                .append(getRoleName())
                .append('\n')
                .append(getEmail());

        return sb.toString();
    }

    public int getLocationRegistrationInterval()
    {
        return locationRegistrationInterval;
    }

    public UserDTO setLocationRegistrationInterval(int locationRegistrationInterval)
    {
        this.locationRegistrationInterval = locationRegistrationInterval;

        return this;
    }

    public int getStartingHour()
    {
        return startingHour;
    }

    public UserDTO setStartingHour(int startingHour)
    {
        this.startingHour = startingHour;

        return this;
    }

    public int getStartingMinute()
    {
        return startingMinute;
    }

    public UserDTO setStartingMinute(int startingMinute)
    {
        this.startingMinute = startingMinute;

        return this;
    }

    public int getEndingHour()
    {
        return endingHour;
    }

    public UserDTO setEndingHour(int endingHour)
    {
        this.endingHour = endingHour;

        return this;
    }

    public int getEndingMinute()
    {
        return endingMinute;
    }

    public UserDTO setEndingMinute(int endingMinute)
    {
        this.endingMinute = endingMinute;

        return this;
    }

    public boolean isForceStartWorkSession()
    {
        return forceStartWorkSession;
    }

    public UserDTO setForceStartWorkSession(boolean forceStartWorkSession)
    {
        this.forceStartWorkSession = forceStartWorkSession;

        return this;
    }
}
