package com.project.moviebooth;

public class CardView { //POJO class to store data related to each item in the grid view of the main screen
    private int imageId;
    private String buttonText;
    private String className;

    public CardView(int imageId, String buttonText, String className) {
        this.imageId = imageId;
        this.buttonText = buttonText;
        this.className = className;
    }

    public int getImageId() { //return imageId
        return imageId;
    }

    public void setImageId(int imageId) { //set a value for imageId
        this.imageId = imageId;
    }

    public String getButtonText() { //return buttonText
        return buttonText;
    }

    public void setButtonText(String buttonText) { //set a value for buttonText
        this.buttonText = buttonText;
    }

    public String getClassName() { //return className
        return className;
    }

    public void setClassName(String className) { //set a value for className
        this.className = className;
    }
}
