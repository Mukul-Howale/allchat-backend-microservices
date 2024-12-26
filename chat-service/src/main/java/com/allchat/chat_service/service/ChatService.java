package com.allchat.chat_service.service;

import org.springframework.stereotype.Service;

@Service
public class ChatService {

    public boolean startChat(){
        try{
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    public boolean stopChat(){
        try{
            return true;
        }
        catch(Exception e){
            return false;
        }
    }
}
