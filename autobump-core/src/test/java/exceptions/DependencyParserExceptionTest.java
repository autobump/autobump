package exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyParserExceptionTest {

    @Test
    void testCreate() {
        assertEquals("test" , new DependencyParserException("test", new RuntimeException()).getMessage());
    }

}
