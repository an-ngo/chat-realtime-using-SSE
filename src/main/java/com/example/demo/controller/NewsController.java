package com.example.demo.controller;

import com.example.demo.model.Message;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class NewsController {

//    public List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
public Map<String, SseEmitter> emitters = new HashMap<>();


    private final Long TIMEOUT = Long.MAX_VALUE;
    //method for client subscription
    @CrossOrigin("*")
    @RequestMapping(value = "/subscribe" , consumes = MediaType.ALL_VALUE)
    public SseEmitter subscribe(@RequestParam String userID){
        SseEmitter sseEmitter = new SseEmitter();
        sendInitEvent(sseEmitter);
        emitters.put(userID,sseEmitter);

        sseEmitter.onCompletion(() -> emitters.remove(sseEmitter));
        sseEmitter.onTimeout(()-> emitters.remove(sseEmitter));
        sseEmitter.onError((e)-> emitters.remove(sseEmitter));


        return sseEmitter;
    }

    private void sendInitEvent(SseEmitter sseEmitter){
        try {
            sseEmitter.send(SseEmitter.event().name("INIT"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //method for dispatching events to all clients
//    @PostMapping(value = "/dispatchEvent")
//    public void dispatchEventToClients(@RequestParam String title, @RequestParam String text){
//        String eventFormatted = new JSONObject()
//                .put("title",title)
//                .put("text",text).toString();
//
//
//        for( SseEmitter emitter: emitters){
//            try {
//                emitter.send(SseEmitter.event().name("latest").data(eventFormatted));
//            } catch (IOException e) {
//                emitters.remove(emitter);
//            }
//        }
//    }

    @PostMapping("/dispatchEventToID")
    public void dispatchEventToId(@RequestBody Message message){
        String eventFormatted = new JSONObject()
                .put("title",message.getTitle())
                .put("text",message.getText())
                .toString();

        SseEmitter sseEmitter = emitters.get(message.getUserId());
        if(sseEmitter!=null){
            try {
                sseEmitter.send(SseEmitter.event().name("latest").data(eventFormatted));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
