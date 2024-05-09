package pl.gruszm.zephyrwork.DTOs;

public class UserDTO
{
    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private Integer supervisorId;
    private String roleName;

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
}
