package cvsu.clearance.app;

public class ReadCSV {
    String studentNumber;

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    @Override
    public String toString() {
        return "ReadCSV{" +
                "studentNumber='" + studentNumber + '\'' +
                '}';
    }
}
