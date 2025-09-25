public class Student {
    private String ph906;
    private String fullName;
    private String address;
    private String type;
    private String deadline;
    private String status;

    public Student(String ph906, String fullName, String address, String type, String deadline, String status) {
        this.ph906 = ph906;
        this.fullName = fullName;
        this.address = address;
        this.type = type;
        this.deadline = deadline;
        this.status = status;
    }

    public String getPh906() { return ph906; }
    public String getFullName() { return fullName; }
    public String getAddress() { return address; }
    public String getType() { return type; }
    public String getDeadline() { return deadline; }
    public String getStatus() { return status; }
}
