package com.ws.womansafety.model;

public class User {
    public String uName;
    public String uMobileNo;

    public User(String uName, String uMobileNo) {
        this.uName = uName;
        this.uMobileNo = uMobileNo;
    }
    public User(){}

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuMobileNo() {
        return uMobileNo;
    }

    public void setuMobileNo(String uMobileNo) {
        this.uMobileNo = uMobileNo;
    }
}
