package com.example.androideatitserver.Common;

import com.example.androideatitserver.Model.Request;
import com.example.androideatitserver.Model.User;

public class Common {
    public static User currentUser;

    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";

    public static final int PICK_IMAGE_REQUEST = 71;
    public static Request currentRequest = null;

    public static String convertCodeToStatus(String code) {
        if(code.equals("0"))
            return "Placed";
        else if(code.equals("1"))
            return "On my way";
        else
            return "Shipped";
    }
}
