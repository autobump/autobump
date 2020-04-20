import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class HelloWorldTest {

    @Test
    public void testToString() {
        assertEquals("Hello World", (new HelloWorld()).toString());
    }
}