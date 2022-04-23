package cvsu.clearance.app;

import java.util.List;
import java.util.Map;

public class CatchRequirementsDetails {
    private String RequirementsName, Location, Description, RequirementStatus, SigningStation;
    private Map<String, Object> IncompleteFileURI;


    public CatchRequirementsDetails() {
        //public no-arg
    }



    public CatchRequirementsDetails(String CatchRequirementsName, String CatchLocation, String CatchDescription, String CatchRequirementStatus, String CatchSigningStation, Map<String, Object> IncompleteFileURI){
        this.RequirementsName = CatchRequirementsName;
        this.Description = CatchDescription;
        this.Location = CatchLocation;
        this.RequirementStatus = CatchRequirementStatus;
        this.SigningStation = CatchSigningStation;
        this.IncompleteFileURI = IncompleteFileURI;
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

    public Map<String, Object> getIncompleteFileUri() {
        return IncompleteFileURI;
    }

}

