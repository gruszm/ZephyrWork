package pl.gruszm.zephyrwork.DTOs;

import pl.gruszm.zephyrwork.enums.RoleType;

public class RegistrationDTO
{
    private String email;

    private String firstName;

    private String lastName;

    private String password;

    private String repeatPassword;

    private Integer supervisorId;

    private RoleType role;

    public String getEmail()
    {
        return email;
    }

    public RegistrationDTO setEmail(String email)
    {
        this.email = email;

        return this;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public RegistrationDTO setFirstName(String firstName)
    {
        this.firstName = firstName;

        return this;
    }

    public String getLastName()
    {
        return lastName;
    }

    public RegistrationDTO setLastName(String lastName)
    {
        this.lastName = lastName;

        return this;
    }

    public String getPassword()
    {
        return password;
    }

    public RegistrationDTO setPassword(String password)
    {
        this.password = password;

        return this;
    }

    public String getRepeatPassword()
    {
        return repeatPassword;
    }

    public RegistrationDTO setRepeatPassword(String repeatPassword)
    {
        this.repeatPassword = repeatPassword;

        return this;
    }

    public Integer getSupervisorId()
    {
        return supervisorId;
    }

    public RegistrationDTO setSupervisorId(Integer supervisorId)
    {
        this.supervisorId = supervisorId;

        return this;
    }

    public RoleType getRole()
    {
        return role;
    }

    public RegistrationDTO setRole(RoleType role)
    {
        this.role = role;

        return this;
    }

    public boolean arePasswordFieldsDifferent()
    {
        return !password.equals(repeatPassword);
    }
}
