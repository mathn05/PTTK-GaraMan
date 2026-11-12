package garaman.entity;

public class Member6 {
    private Integer id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private Account6 acc;

    public Member6() {}
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Account6 getAcc() { return acc; }
    public void setAcc(Account6 acc) { this.acc = acc; }
}
