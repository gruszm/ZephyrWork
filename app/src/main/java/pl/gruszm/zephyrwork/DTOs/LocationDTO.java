package pl.gruszm.zephyrwork.DTOs;

public class LocationDTO
{
    private String locationTimeAsString;
    private double latitude;
    private double longitude;

    public LocationDTO(String locationTimeAsString, double latitude, double longitude)
    {
        this.locationTimeAsString = locationTimeAsString;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocationTimeAsString()
    {
        return locationTimeAsString;
    }

    public void setLocationTimeAsString(String locationTimeAsString)
    {
        this.locationTimeAsString = locationTimeAsString;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }
}
