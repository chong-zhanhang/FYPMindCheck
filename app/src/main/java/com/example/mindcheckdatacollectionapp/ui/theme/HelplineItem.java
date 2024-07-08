package com.example.mindcheckdatacollectionapp.ui.theme;

public class HelplineItem {
    private String name;
    private String number;
    private String website;

    public HelplineItem(String name, String number, String website) {
        this.name = name;
        this.number = number;
        this.website = website;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
