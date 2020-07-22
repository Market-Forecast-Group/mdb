package org.mfg.mdbplugin.xmdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Table;

@SuppressWarnings("boxing")
public class XQuery {
	private ScriptEngine engine;
	private String[] formulas;
	private XCursor _cursor;
	private SimpleScriptContext ctx;
	private String[] vars;
	private String _filter;
	private List<SelectColumn> _select;
	private int _limit;

	public static class SelectColumn {
		public String name;
		public String formula;
	}

	public XQuery(XCursor cursor, List<SelectColumn> select, String filter) {
		this._cursor = cursor;
		this._select = select;
		this._filter = filter;
		Table table = cursor.getTable();

		vars = new String[table.size()];
		for (int i = 0; i < vars.length; i++) {
			vars[i] = "__rec__" + table.get(i).getName();
		}

		this.formulas = new String[select.size()];
		for (int i = 0; i < select.size(); i++) {
			formulas[i] = select.get(i).formula;
		}

		for (int i = 0; i < this.formulas.length; i++) {
			String formula = this.formulas[i];
			for (Column col : table) {
				formula = formula.replace("$$." + col.getName(), "__rec__"
						+ col.getName());
			}
			this.formulas[i] = formula;
		}

		if (this._filter != null) {
			for (SelectColumn col : select) {
				this._filter = this._filter.replace("$$." + col.name, "__rec__"
						+ col.name);
			}
		}

		ScriptEngineManager manager = new ScriptEngineManager();
		engine = manager.getEngineByName("JavaScript");
		ctx = new SimpleScriptContext();
		engine.setContext(ctx);

		_limit = 0;

	}

	public int getLimit() {
		return _limit;
	}

	public void setLimit(int limit) {
		this._limit = limit;
	}

	public List<Object[]> selectAll() throws IOException, ScriptException {
		if (formulas.length == 0) {
			return Collections.emptyList();
		}

		List<Object[]> result = new ArrayList<>();

		while (_cursor.next()) {
			if (_limit > 0) {
				if (result.size() > _limit) {
					break;
				}
			}
			// set ctx attributes
			Object[] realRec = _cursor.getRecord();
			for (int j = 0; j < vars.length; j++) {
				ctx.setAttribute(vars[j], realRec[j],
						ScriptContext.ENGINE_SCOPE);
			}

			long position = _cursor.position();
			ctx.setAttribute("$pos$", position, ScriptContext.ENGINE_SCOPE);

			// eval record
			Object[] evalRec = new Object[formulas.length];
			for (int j = 0; j < evalRec.length; j++) {
				String formula = formulas[j];
				evalRec[j] = engine.eval(formula);
			}

			if (_filter == null) {
				result.add(evalRec);
			} else {
				int j = 0;
				for (SelectColumn col : _select) {
					ctx.setAttribute("__rec__" + col.name, evalRec[j],
							ScriptContext.ENGINE_SCOPE);
					j++;
				}

				Object test = engine.eval(_filter);

				if (Boolean.TRUE.equals(test)) {
					result.add(evalRec);
				}
			}

		}
		return result;
	}

	public void close() throws IOException {
		_cursor.close();
	}

}
