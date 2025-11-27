package com.polyu.cmms.model;

import java.util.Date;

public class Staff {
    private int staffId;
    private String staffNumber;
    private String firstName;
    private String lastName;
    private int age;
    private String gender;
    private String role;
    private String email;
    private String phone;
    private Date hireDate;
    private boolean activeFlag;
    private String responsibility;
    
    // function,getter && setter methods
    public Staff() {}
    
    public Staff(int staffId, String staffNumber, String firstName, String lastName, 
                 int age, String gender, String role, String email, String phone, 
                 Date hireDate, boolean activeFlag, String responsibility) {
        this.staffId = staffId;
        this.staffNumber = staffNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.gender = gender;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.hireDate = hireDate;
        this.activeFlag = activeFlag;
        this.responsibility = responsibility;
    }
    
    // getter and setter methods
    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }
    public String getStaffNumber() { return staffNumber; }
    public void setStaffNumber(String staffNumber) { this.staffNumber = staffNumber; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Date getHireDate() { return hireDate; }
    public void setHireDate(Date hireDate) { this.hireDate = hireDate; }
    public boolean isActiveFlag() { return activeFlag; }
    public void setActiveFlag(boolean activeFlag) { this.activeFlag = activeFlag; }
    public String getResponsibility() { return responsibility; }
    public void setResponsibility(String responsibility) { this.responsibility = responsibility; }
    
    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + role + ")";
    }
}