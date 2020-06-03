package com.github.autobump.springboot.controllers;

import com.github.autobump.springboot.controllers.dtos.CommentDto;
import com.github.autobump.springboot.controllers.dtos.PushDto;
import com.github.autobump.springboot.controllers.dtos.RejectedDto;
import com.github.autobump.springboot.services.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class WebhooksControllerTest {
    @Mock
    WebhookService webhookService;
    @InjectMocks
    WebhooksController webhooksController;

    @BeforeEach
    void setUp() {
        webhooksController = new WebhooksController(webhookService);
    }

    @Test
    void commentCreated() {
        assertThatCode(() ->
                webhooksController.commentCreated(new CommentDto("comment", "name", "title")))
                .doesNotThrowAnyException();
    }

    @Test
    void prRejected() {
        assertThatCode(() ->
                webhooksController.prRejected(new RejectedDto("title", "name")))
                .doesNotThrowAnyException();
    }

    @Test
    void push() {
        assertThatCode(() ->
                webhooksController.push(new PushDto(
                        new PushDto.Data.Push.Change.Event("branchname"),
                        "link")))
                .doesNotThrowAnyException();
    }

    @Test
    void pushWithoutBranchname() {
        assertThatCode(() -> webhooksController.push(new PushDto(null, "link"))).doesNotThrowAnyException();
    }
}
