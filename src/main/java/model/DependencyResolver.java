package model;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

public interface DependencyResolver {
    Set<Dependency> resolve(Workspace workspace) throws IOException, XmlPullParserException;
}
