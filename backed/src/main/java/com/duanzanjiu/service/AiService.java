package com.duanzanjiu.service;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;
    public AiService(ChatClient.Builder chatClientBulider){
        this.chatClient = chatClientBulider.build();

    }
    public String chat(String systemPrompt,String userPrompt){
       return chatClient.prompt()
                 .system(systemPrompt)
                 .user(userPrompt)
                 .call()
                 .content();

    }

    public String chatForJson(String systemPrompt,String userPrompt){
        String raw = chat(systemPrompt,userPrompt);
        raw = raw.trim();
        if(raw.startsWith("```json")){
            raw = raw.substring(7);

        }else if(raw.startsWith("```")){
            raw = raw.substring(3);

        }
        if(raw.endsWith("```")){
            raw = raw.substring(0,raw.length()-3);
        }
        return raw.trim();

    }
}
