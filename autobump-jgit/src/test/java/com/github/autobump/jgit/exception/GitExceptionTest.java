package com.github.autobump.jgit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitExceptionTest {

    @Test
    void testCreate() {
        assertEquals("test", new GitException("test", new RuntimeException()).getMessage());
    }

}
