package com.github.konstantinevashalomidze.semver;


/**
 * @author Konstantine Vashalomidze
 */
public class SemverInvalidInputException extends IllegalArgumentException {
    public SemverInvalidInputException(String message) {
        super(message);
    }
}