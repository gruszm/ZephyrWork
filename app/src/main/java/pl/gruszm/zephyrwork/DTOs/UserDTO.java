package pl.gruszm.zephyrwork.DTOs;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        UserDTO userDTO = (UserDTO) o;
        return id == userDTO.id && locationRegistrationInterval == userDTO.locationRegistrationInterval && startingHour == userDTO.startingHour && startingMinute == userDTO.startingMinute && endingHour == userDTO.endingHour && endingMinute == userDTO.endingMinute && forceStartWorkSession == userDTO.forceStartWorkSession && Objects.equals(email, userDTO.email) && Objects.equals(firstName, userDTO.firstName) && Objects.equals(lastName, userDTO.lastName) && Objects.equals(supervisorId, userDTO.supervisorId) && Objects.equals(roleName, userDTO.roleName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, email, firstName, lastName, supervisorId, roleName, locationRegistrationInterval, startingHour, startingMinute, endingHour, endingMinute, forceStartWorkSession);
    }
}
