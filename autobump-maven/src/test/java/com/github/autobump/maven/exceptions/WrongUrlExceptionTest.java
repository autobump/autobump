package com.github.autobump.maven.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WrongUrlExceptionTest {

    @Test
    void canCreate(){
        assertEquals("test" , new WrongUrlException("test", new RuntimeException()).getMessage());
    }

}
