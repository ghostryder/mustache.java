package com.sampullara.mustache;

import java.util.Locale;

public interface AttributeRenderer {
	public String render(Object o, String format, Locale locale);
}
