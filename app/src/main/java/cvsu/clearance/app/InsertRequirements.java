package cvsu.clearance.app;

public class InsertRequirements {
    String Requirements_Name, Description, Location, Status;

    public InsertRequirements(){

    }

    public InsertRequirements(String Requirements_Name, String Description, String Location, String Status){

        this.Requirements_Name = Requirements_Name;
        this.Description = Description;
        this.Location = Location;
        this.Status = Status;



    }

    public String getRequirements_Name() {
        return Requirements_Name;
    }

    public void setRequirements_Name(String requirements_Name) {
        Requirements_Name = requirements_Name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
