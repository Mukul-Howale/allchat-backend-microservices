package com.allchat.profile_service.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class ExceptionHandler extends Exception{

    private String message;
    private Exception e;

    public ExceptionHandler(String message){
        this.message = message;
    }

    
}
