package cvsu.clearance.app;

public class Note {
    private String Signing_Station_Name;

    public Note() {
        //public no-arg
    }

    public Note (String CatchStationName){
        this.Signing_Station_Name = CatchStationName;
    }

    public String getSigning_Station_Name(){
        return Signing_Station_Name;
    }
}
