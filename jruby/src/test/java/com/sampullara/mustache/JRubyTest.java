package com.sampullara.mustache;

import java.lang.reflect.Method;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jruby.RubyObject;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test using ruby as backing for mustache
 */
public class JRubyTest {
  @Test
  public void testJRubyRuntime() throws ScriptException {
    assertEquals("Hello, jruby!", engine().eval("return 'Hello, ' + $engine + '!'"));
  }

  @Test
  public void testCall() throws ScriptException {
    long start = System.currentTimeMillis();
    ScriptEngine engine = engine();
    RubyObject eval = (RubyObject) engine.eval("class BackingCode\n  def callback\n    return 'Hello'\n  end\nend\nBackingCode.new()");
    ThreadContext threadContext = ThreadContext.newContext(eval.getRuntime());
    IRubyObject result = eval.callMethod(threadContext, "callback");
    assertEquals("Hello", result.asJavaString());
    System.out.println(System.currentTimeMillis() - start);
  }

  private ScriptEngine engine() {

    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine jRubyEngine = manager.getEngineByName("jruby");
    assertNotNull(jRubyEngine);

    return jRubyEngine;
  }


}
