package com.github.autobump.springboot.controllers;

import com.github.autobump.springboot.controllers.dtos.CommentDto;
import com.github.autobump.springboot.controllers.dtos.PushDto;
import com.github.autobump.springboot.controllers.dtos.RejectedDto;
import com.github.autobump.springboot.services.WebhookService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/webhooks")
public class WebhooksController {
    private final WebhookService webhookService;

    public WebhooksController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("comment_created")
    public void commentCreated(@RequestBody CommentDto commentDto) {
        webhookService.handleComment(commentDto.getPrTitle(), commentDto.getCommment(), commentDto.getRepoName());
    }

    @PostMapping("pr_rejected")
    public void prRejected(@RequestBody RejectedDto rejectedDto) {
        webhookService.handleReject(rejectedDto.getPrTitle(), rejectedDto.getRepoName());
    }

    @PostMapping("push")
    public void push(@RequestBody PushDto pushDto) {
        if (pushDto.getBranchName() != null){
            webhookService.handlePush(pushDto.getBranchName(), URI.create(pushDto.geturl()));
        }
    }
}
