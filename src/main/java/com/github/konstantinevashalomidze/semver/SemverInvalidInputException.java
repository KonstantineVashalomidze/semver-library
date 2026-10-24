package com.github.konstantinevashalomidze.semver;


/**
 * @author <a href="https://portfolio.kosta-server.org/">Konstantine Vashalomidze</a>
 */
public class SemverInvalidInputException extends IllegalArgumentException {
    public SemverInvalidInputException(String message) {
        super(message);
    }
}