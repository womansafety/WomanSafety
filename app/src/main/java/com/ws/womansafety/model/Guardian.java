package com.ws.womansafety.model;

public class Guardian {
  public  String gName;
  public  String gToken;
  public  String gMobileNo;

    public Guardian(String gToken, String gName, String gMobileNo) {
        this.gToken = gToken;
        this.gName = gName;
        this.gMobileNo = gMobileNo;
    }

    public Guardian() {
    }

    public String getgName() {
        return gName;
    }

    public void setgName(String gName) {
        this.gName = gName;
    }

    public String getgToken() {
        return gToken;
    }

    public void setgToken(String gToken) {
        this.gToken = gToken;
    }

    public String getgMobileNo() {
        return gMobileNo;
    }

    public void setgMobileNo(String gMobileNo) {
        this.gMobileNo = gMobileNo;
    }
}
