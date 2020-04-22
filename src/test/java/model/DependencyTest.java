package model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

    @Test
    public void testEquals() {
        Dependency dependency = new Dependency("test", "test", "test");
        Dependency dependency1 = new Dependency(null, "test", "test");
        Dependency dependency2 = new Dependency("test", null, "test");
        Dependency dependency3 = new Dependency("test", "test", null);
        assertEquals(dependency, dependency);
        assertNotEquals(dependency, dependency1);
        assertNotEquals(dependency, dependency2);
        assertNotEquals(dependency, dependency3);

    }

    @Test
    public void testHashCode() {
    }

    @Test
    public void testToString1() {
    }
}