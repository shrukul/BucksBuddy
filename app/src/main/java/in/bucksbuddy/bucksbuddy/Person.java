package in.bucksbuddy.bucksbuddy;

/**
 * Created by shrukul on 20/1/16.
 */
class Person {
    String name;
    String phone;
    String amount;
    int photoId;
    int type;

    Person(String name, String phone, int photoId, String amount, int type) {
        this.name = name;
        this.phone = phone;
        this.amount = amount;
        this.photoId = photoId;
        this.type = type;
    }

    Person(String name, String phone, int photoId) {
        this.name = name;
        this.phone = phone;
        this.photoId = photoId;
    }

    Person(String name, String phone, String amount) {
        this.name = name;
        this.phone = phone;
        this.amount = amount;
    }

    Person(){

    }


    // getting ID
    public String getPhone(){
        return this.phone;
    }

    // setting id
    public void setPhone(String age){
        this.phone = age;
    }

    // getting name
    public String getName(){
        return this.name;
    }

    // setting name
    public void setName(String name){
        this.name = name;
    }

    // getting phone number
    public int getPhotoId(){
        return this.photoId;
    }

    // setting phone number
    public void setPhotoId(int photoId){
        this.photoId = photoId;
    }

    // getting phone number
    public String getAmount(){
        return this.amount;
    }

    // setting phone number
    public void setAmount(String amount){
        this.amount = amount;
    }

    // getting phone number
    public int getType(){
        return this.type;
    }

    // setting phone number
    public void setType(int type){
        this.type= type;
    }
}
