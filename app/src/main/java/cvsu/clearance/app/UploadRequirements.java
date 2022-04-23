package cvsu.clearance.app;

import java.util.List;

public class UploadRequirements {
    private String mName;
    private String mFileUrl;
    private List<ReadCSV> mfileData;

    public UploadRequirements(){

    }

    public UploadRequirements(String name, String fileUrl, List<ReadCSV> fileData) {
        // if (name.trim().equals("")) {
        //    name = "Unassigned";
        // }

        mName = name;
        mFileUrl = fileUrl;
        mfileData = fileData;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getFileUrl() {
        return mFileUrl;
    }

    public void setFileUrl(String fileUrl) {
        mFileUrl = fileUrl;
    }

    public List<ReadCSV> getFileData() {
        return mfileData;
    }

    public void setFileData(List<ReadCSV> fileData) {
        this.mfileData = fileData;
    }
}
