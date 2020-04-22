package helloworldpackage;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HelloWorldTest {
    private HelloWorld helloWorld;

    @Before
    public void setUp() {
        helloWorld = new HelloWorld();
    }

    @Test
    public void testToString() {
        assertEquals("Hello world", helloWorld.toString());
    }
}