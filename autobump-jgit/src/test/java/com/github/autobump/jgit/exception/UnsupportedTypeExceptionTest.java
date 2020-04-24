package com.github.autobump.jgit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnsupportedTypeExceptionTest {

    @Test
    void testCreate() {
        assertEquals("test", new UnsupportedTypeException("test").getMessage());
    }
}
