package exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoDependencyFileFoundExceptionTest {

    @Test
    void testCreate() {
        assertEquals("test", new NoDependencyFileFoundException("test", new RuntimeException()).getMessage());
    }

}
