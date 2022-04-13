package cvsu.clearance.app;

public class CatchStationDetails {
    private String Signing_Station_Name, Requirements, Location, isRequired;


    public CatchStationDetails() {
        //public no-arg
    }

    public CatchStationDetails(String stationName,String stationRequirements,String stationLocation,String stationIsRequired){
        this.Signing_Station_Name = stationName;
        this.Requirements = stationRequirements;
        this.Location = stationLocation;
        this.isRequired = stationIsRequired;
    }

    public String getSigning_Station_Name() {
        return Signing_Station_Name;
    }

    public String getRequirements() {
        return Requirements;
    }

    public String getLocation() {
        return Location;
    }

    public String getIsRequired() {
        return isRequired;
    }
}
