package cvsu.clearance.app;

public class CatchStaffDetails {
    private String Name,Email,Role,Station,Verified;
    private double VerifyCount;

    public CatchStaffDetails() {
        //public no-arg
    }

    public CatchStaffDetails (String CatchName,String CatchEmail,String CatchRole,String CatchStation,String CatchVerified, double CatchVerifyCount){
        this.Name = CatchName;
        this.Email = CatchEmail;
        this.Role = CatchRole;
        this.Station = CatchStation;
        this.Verified = CatchVerified;
        this.VerifyCount = CatchVerifyCount;
    }

    public String getName(){
        return Name;
    }

    public String getEmail() {
        return Email;
    }

    public String getRole() {
        return Role;
    }

    public String getStation() {
        return Station;
    }

    public String getVerified() {
        return Verified;
    }

    public Double getVerifyCount() { return VerifyCount; }
}
