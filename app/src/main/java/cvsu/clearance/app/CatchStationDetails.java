package cvsu.clearance.app;

public class CatchStationDetails {
    private String Signing_Station_Name, Location, isRequired;
    private Double StationNumber;


    public CatchStationDetails() {
        //public no-arg
    }

    public CatchStationDetails(String stationName,Double stationNumber,String stationLocation,String stationIsRequired){
        this.Signing_Station_Name = stationName;
        this.StationNumber = stationNumber;
        this.Location = stationLocation;
        this.isRequired = stationIsRequired;
    }

    public String getSigning_Station_Name() {
        return Signing_Station_Name;
    }

    public String getLocation() {
        return Location;
    }

    public String getIsRequired() {
        return isRequired;
    }

    public Double getStationNumber() {
        return StationNumber;
    }
}
