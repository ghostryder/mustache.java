package com.sampullara.mustache;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({ "rawtypes" })
public class DefaultValueHandler implements ValueHandler {
	private static Map<Class, AttributeRenderer> renderers = new HashMap<Class, AttributeRenderer>();

	public static void register(Class klass, AttributeRenderer renderer) {
		renderers.put(klass, renderer);
	}

	public Object handle(Object expression, Scope scope) {
		if (!(expression instanceof String))
			return scope.get(expression, scope);

		String[] options = expression.toString().split(";", 2);
		Object value = scope.get(options[0].trim(), scope);

		if (options.length > 1 && value != null)
			return format(value, options[1]);

		return value;
	}

	private Object format(Object value, String format) {
		AttributeRenderer r = renderers.get(value.getClass());
		if (r == null)
			return value;
		return r.render(value, format, Locale.getDefault());
	}
}
