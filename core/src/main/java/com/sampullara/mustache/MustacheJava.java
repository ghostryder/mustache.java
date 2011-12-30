package com.sampullara.mustache;

/**
 * The interface for working with Compiler and Builder
 * <p/>
 * User: sam Date: 5/15/11 Time: 1:30 PM
 */
public interface MustacheJava {
	Mustache parse(String partial, String path) throws MustacheException;

	Mustache parseFile(String path) throws MustacheException;
}
