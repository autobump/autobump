package com.github.autobump.maven.model.testClasses;

import org.apache.maven.model.InputSource;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;

public class MavenXpp3ReaderExTester extends MavenXpp3ReaderEx {

    @Override
    public Model read(Reader reader, boolean strict, InputSource source) throws IOException, XmlPullParserException {
        throw new XmlPullParserException("test");
    }
}
