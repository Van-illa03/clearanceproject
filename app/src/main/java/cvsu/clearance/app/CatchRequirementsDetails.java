package cvsu.clearance.app;

public class CatchRequirementsDetails {
    private String RequirementsName, Location, Description, RequirementStatus, SigningStation;


    public CatchRequirementsDetails() {
        //public no-arg
    }

    public CatchRequirementsDetails(String CatchRequirementsName, String CatchLocation, String CatchDescription, String CatchRequirementStatus, String CatchSigningStation){
        this.RequirementsName = CatchRequirementsName;
        this.Description = CatchDescription;
        this.Location = CatchLocation;
        this.RequirementStatus = CatchRequirementStatus;
        this.SigningStation = CatchSigningStation;
    }

    public String getLocation() {
        return Location;
    }

    public String getDescription() {
        return Description;
    }

    public String getRequirementsName() {
        return RequirementsName;
    }

    public String getRequirementStatus() {
        return RequirementStatus;
    }

    public String getSigningStation() {
        return SigningStation;
    }
}

