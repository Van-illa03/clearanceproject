package cvsu.clearance.app;

public class CatchStudentDetails {
    String Course, Name, Email, Role, StdNo;


    public CatchStudentDetails () {
    }

    public CatchStudentDetails (String Course, String Name, String Email, String Role, String StdNo) {
        this.Course = Course;
        this.Name = Name;
        this.Email = Email;
        this.Role = Role;
        this.StdNo = StdNo;

    }

    public String getCourse() {
        return Course;
    }

    public void setCourse(String course) {
        Course = course;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public String getStdNo() {
        return StdNo;
    }

    public void setStdNo(String stdNo) {
        StdNo = stdNo;
    }
}
