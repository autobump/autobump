package com.github.autobump.springboot.controllers;

import com.github.autobump.springboot.controllers.dtos.PushDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks")
public class WebhooksController {

    @PostMapping("comment_created")
    public Boolean commentCreated(){
        return true;
    }

    @PostMapping("pr_rejected")
    public Boolean prRejected(){
        return true;
    }

    @PostMapping("push")
    public void push(@RequestBody PushDto pushDto){
        if (pushDto.getBranchName().equalsIgnoreCase("master")){
            // TODO: add code that does things
        }
    }
}
