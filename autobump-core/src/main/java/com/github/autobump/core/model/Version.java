package com.github.autobump.core.model;

/**
 * @author Griet Vermeesch
 * @version 1.0 27/04/2020 14:36
 */
public interface Version extends Comparable<Version> {

    String getVersionNumber();
}
