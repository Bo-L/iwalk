package com.example.boge.laonianbao.PersonInfo;

/**
 * Created by Boge on 2017/5/27.
 */

public class PersonInfo {
    private String account;
    private static PersonInfo personInfo=null;
    private String walkNumber;

    public String getTel_number_contact1() {
        return tel_number_contact1;
    }

    public void setTel_number_contact1(String tel_number_contact1) {
        this.tel_number_contact1 = tel_number_contact1;
    }

    private String tel_number_contact1="17713583744";
    private PersonInfo(){

    }
    public  static PersonInfo getPersonInfo(){
        if(personInfo == null){
            personInfo=new PersonInfo();
        }
        return personInfo;
    }
    public void setAccount(String account){
        this.account=account;
    }
    public String getAccount(){
        return this.account;
    }
    public void setWalkNumber(String walkNumber){
        this.walkNumber=walkNumber;
    }
    public String getWalkNumber(){
        return walkNumber;
    }
}
