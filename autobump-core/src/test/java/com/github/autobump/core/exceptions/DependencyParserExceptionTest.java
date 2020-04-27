package com.github.autobump.core.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DependencyParserExceptionTest {

    private static final transient String TEST = "test";

    @Test
    void testCreateOneParam() {
        assertEquals("test" , new DependencyParserException(TEST).getMessage());
    }

    @Test
    void testCreateTwoParam() {
        assertEquals("test" , new DependencyParserException(TEST, new RuntimeException()).getMessage());
    }
}
