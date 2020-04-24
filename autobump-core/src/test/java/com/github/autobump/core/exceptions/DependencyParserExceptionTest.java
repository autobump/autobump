package com.github.autobump.core.exceptions;

import com.github.autobump.core.exceptions.DependencyParserException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DependencyParserExceptionTest {

    @Test
    void testCreate() {
        assertEquals("test" , new DependencyParserException("test", new RuntimeException()).getMessage());
    }

}
