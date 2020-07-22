package org.mfg.mdbplugin.ui.editors;

import static java.lang.System.out;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

@SuppressWarnings("boxing")
public class TestJavascript {

	public static void main(String[] args) throws ScriptException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		ScriptContext ctx = new SimpleScriptContext();
		
		Map<String, Object> rec = new HashMap<>();
		rec.put("price", 2);
		ctx.setAttribute("$pos$", 10, ScriptContext.ENGINE_SCOPE);
		ctx.setAttribute("$$", rec, ScriptContext.ENGINE_SCOPE);
		
		engine.setContext(ctx);
		out.println(engine.eval("new Date(098709870987).toString()"));
	}
}
