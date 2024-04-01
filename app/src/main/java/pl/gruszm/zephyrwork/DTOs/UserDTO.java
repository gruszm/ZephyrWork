package pl.gruszm.zephyrwork.DTOs;

import java.util.List;

public class UserDTO
{
    private String email;
    private String firstName;
    private String lastName;
    private Integer supervisorId;
    private List<String> roles;

    public String getEmail()
    {
        return email;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }


    public Integer getSupervisorId()
    {
        return supervisorId;
    }

    public List<String> getRoles()
    {
        return roles;
    }
}
