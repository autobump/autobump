package com.github.autobump.core.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DependencyParserExceptionTest {

    @Test
    void testCreateOneParam() {
        assertEquals("test" , new DependencyParserException("test").getMessage());
    }

    @Test
    void testCreateTwoParam() {
        assertEquals("test" , new DependencyParserException("test", new RuntimeException()).getMessage());
    }
}
