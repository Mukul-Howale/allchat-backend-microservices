package com.allchat.profile_service.exception;

public class NoSuchProfileException extends ExceptionHandler{

    public NoSuchProfileException(String message, Exception e){
        super(message, e);
    }

    public NoSuchProfileException(String message){
        super(message);
    }
}
