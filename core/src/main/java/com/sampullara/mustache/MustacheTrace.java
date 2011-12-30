package com.sampullara.mustache;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trace all calls to underlying mustache backing code for performance and
 * concurrency analysis.
 * <p/>
 * User: sam Date: 5/14/11 Time: 3:40 PM
 */
public class MustacheTrace {
	private static ThreadLocal<MustacheTrace> traceThreadLocal = new ThreadLocal<MustacheTrace>();
	private static Map<Long, MustacheTrace> traces = new ConcurrentHashMap<Long, MustacheTrace>();
	private long unique;

	public static class Event {
		public long start = System.currentTimeMillis();
		public long end;
		public String thread;
		public String name;
		public String parameter;

		public Event(String name, String parameter, String thread) {
			this.name = name;
			this.parameter = parameter;
			this.thread = thread;
		}

		public String toString() {
			return start + ",\"" + end + ",\"" + name.replace("\"", "\\\"") + "\",\"" + parameter.replace("\"", "\\\"")
			    + "\"";
		}

		public void end() {
			end = System.currentTimeMillis();
		}
	}

	private List<Event> events = new ArrayList<Event>();

	public synchronized static Event addEvent(String name, String parameter) {
		MustacheTrace trace = traceThreadLocal.get();
		String threadName = Thread.currentThread().getName();
		Event event = new Event(name, parameter, threadName);
		if (trace == null) {
			Mustache.logger.info("Current trace not set in thread " + threadName);
		} else {
			trace.events.add(event);
		}
		return event;
	}

	public static void toASCII(Writer w, long uniqueid, int range) throws IOException {
		MustacheTrace trace = traces.get(uniqueid);
		if (trace == null)
			return;
		// Find min and max time
		long min = Long.MAX_VALUE;
		long max = 0;
		for (Event event : trace.events) {
			if (event.end > max)
				max = event.end;
			if (event.start < min)
				min = event.start;
		}
		double scale = range / ((double) max - min);
		Collections.sort(trace.events, new Comparator<Event>() {
			@Override
			public int compare(Event event, Event event1) {
				return (int) (event.start - event1.start);
			}
		});
		for (Event event : trace.events) {
			long interval = event.end - event.start;
			// Ignore times at or below the accuracy
			if (interval <= 1)
				continue;
			int during = (int) Math.round(interval * scale);
			int before = (int) Math.round((event.start - min) * scale);
			int after = (int) Math.round((max - event.end) * scale);
			int extra = 0;
			if (event.end == 0) {
				during = 0;
				after = 0;
				extra = range - before;
			}
			int total = before + during + after;
			if (total < range) {
				during += range - total;
			}
			if (total > range) {
				during -= total - range;
			}
			if (during == 0)
				continue;
			for (int i = 0; i < before; i++) {
				w.write("-");
			}
			for (int i = 0; i < during; i++) {
				w.write("*");
			}
			for (int i = 0; i < after; i++) {
				w.write("-");
			}
			for (int i = 0; i < extra; i++) {
				w.write("x");
			}
			w.write(" ");
			w.write(event.name);
			w.write(", ");
			w.write(String.valueOf(interval));
			w.write("ms\n");
		}
		w.write("Time: " + (max - min) + "ms Operations: " + trace.events.size() + "\n");
	}

	public synchronized static void setUniqueId(long unique) {
		MustacheTrace trace = traces.get(unique);
		if (trace == null) {
			trace = new MustacheTrace();
			traces.put(unique, trace);
		}
		trace.unique = unique;
		traceThreadLocal.set(trace);
	}

	public synchronized static long getUniqueId() {
		return traceThreadLocal.get().unique;
	}
}
