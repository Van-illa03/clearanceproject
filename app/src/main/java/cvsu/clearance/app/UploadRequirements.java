package cvsu.clearance.app;

public class UploadRequirements {
    private String mName;
    private String mFileUrl;

    public UploadRequirements() {
        //empty constructor needed
    }

    public UploadRequirements(String name, String fileUrl) {
        // if (name.trim().equals("")) {
        //    name = "Unassigned";
        // }

        mName = name;
        mFileUrl = fileUrl;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mFileUrl;
    }

    public void setImageUrl(String fileUrl) {
        mFileUrl = fileUrl;
    }
}
