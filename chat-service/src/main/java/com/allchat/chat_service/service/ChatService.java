package com.allchat.chat_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

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
