package model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DependencyTest {
    private Dependency dependency;

    @Before
    public void setup(){
        dependency = Dependency.builder().group("test").name("test").version("test").build();
    }

    @Test
    public void getGroup() {
        assertEquals("test", dependency.getGroup());
    }

    @Test
    public void getName() {
        assertEquals("test", dependency.getName());
    }

    @Test
    public void getVersion() {
        assertEquals("test", dependency.getVersion());
    }

    @Test
    public void testToString() {
        assertEquals("Dependency(group=test, name=test, version=test)", dependency.toString());
    }
}