package com.sampullara.mustache;

public interface ValueHandler {
	public Object handle(Object expression, Scope scope);
}