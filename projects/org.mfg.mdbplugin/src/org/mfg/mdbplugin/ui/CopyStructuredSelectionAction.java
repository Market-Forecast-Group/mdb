/*
 * (C) Copyright 2011 - MFG <http://www.marketforecastgroup.com/>
 * All rights reserved. This program and the accompanying materials
 * are proprietary to Giulio Rugarli.
 * 
 * @author <a href="mailto:boniatillo@gmail.com">Arian Fornaris</a>, MFG
 * 
 * @version $Revision$: $Date$:
 * $Id$:
 */
package org.mfg.mdbplugin.ui;

import java.lang.reflect.Array;

import javax.swing.text.TableView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * @author arian
 * 
 */
public class CopyStructuredSelectionAction extends Action {

	private static final String CSV_SEPARATOR = ",";

	@Override
	public void run() {
		IWorkbenchPart part = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActivePart();
		if (part != null) {
			StringBuilder html = new StringBuilder();
			StringBuilder text = new StringBuilder();

			Table table = null;
			Tree tree = null;

			ISelectionProvider selProvider = part.getSite()
					.getSelectionProvider();

			if (selProvider instanceof TableViewer) {
				table = ((TableViewer) selProvider).getTable();
			}

			if (selProvider != null) {
				if (selProvider instanceof TableView) {
					table = ((TableViewer) selProvider).getTable();
				}

				if (selProvider instanceof TreeViewer) {
					tree = ((TreeViewer) selProvider).getTree();
				}
			}

			if (table != null) {
				copyFromTable(html, text, table);
			} else if (tree != null) {
				copyFromTree(html, text, tree);
			} else {
				if (selProvider != null) {
					copyFromSelectionProvider(html, text, selProvider);
				}
			}
			if (html.length() > 0) {
				Clipboard clipboard = new Clipboard(Display.getDefault());
				HTMLTransfer htmlTransfer = HTMLTransfer.getInstance();
				TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(
						new Object[] { html.toString(), text.toString() },
						new Transfer[] { htmlTransfer, textTransfer });
			}
		}
	}

	/**
	 * @param html
	 * @param text
	 * @param selProvider
	 */
	public static void copyFromSelectionProvider(StringBuilder html,
			StringBuilder text, ISelectionProvider selProvider) {
		ISelection sel = selProvider.getSelection();
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection structSel = (IStructuredSelection) sel;
			Object[] selRows = structSel.toArray();

			html.append("<table>");

			for (Object row : selRows) {
				Object array = null;
				if (row.getClass().isArray()) {
					array = row;
				} 
				if (array != null) {
					html.append("<tr>");
					int len = Array.getLength(array);
					for (int i = 0; i < len; i++) {
						Object cell = Array.get(array, i);
						String str = cell == null ? "" : cell.toString();
						html.append("<td>" + str + "</td>");
						if (i > 0) {
							text.append(CSV_SEPARATOR);
						}
						text.append(str);
					}
					html.append("</tr>");
					text.append("\n");
				}
			}

			html.append("</table>");
		}
	}

	/**
	 * @param html
	 * @param text
	 * @param tree
	 */
	public static void copyFromTree(StringBuilder html, StringBuilder text, Tree tree) {
		html.append("<table>");

		html.append("<tr>");
		for (int i = 0; i < tree.getColumnCount(); i++) {
			TreeColumn col = tree.getColumn(i);
			String cell = col.getText();
			String str = cell == null ? "" : cell.toString();
			html.append("<td>" + str + "</td>");
			if (i > 0) {
				text.append(CSV_SEPARATOR);
			}
			text.append(str);
		}
		html.append("</tr>");
		text.append("\n");

		for (TreeItem item : tree.getSelection()) {
			html.append("<tr>");
			for (int i = 0; i < tree.getColumnCount(); i++) {
				String cell = item.getText(i);
				String str = cell == null ? "" : cell.toString();
				html.append("<td>" + str + "</td>");
				if (i > 0) {
					text.append(CSV_SEPARATOR);
				}
				text.append(str);
			}
			html.append("</tr>");
			text.append("\n");
		}
		html.append("</table>");
	}

	/**
	 * @param html
	 * @param text
	 * @param table
	 */
	public static void copyFromTable(StringBuilder html, StringBuilder text,
			Table table) {
		html.append("<table>");

		html.append("<tr>");
		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn col = table.getColumn(i);
			String cell = col.getText();
			String str = cell == null ? "" : cell.toString();
			html.append("<td>" + str + "</td>");
			if (i > 0) {
				text.append(CSV_SEPARATOR);
			}
			text.append(str);
		}
		html.append("</tr>");
		text.append("\n");

		for (TableItem item : table.getSelection()) {
			html.append("<tr>");
			for (int i = 0; i < table.getColumnCount(); i++) {
				String cell = item.getText(i);
				String str = cell == null ? "" : cell.toString();
				html.append("<td>" + str + "</td>");
				if (i > 0) {
					text.append(CSV_SEPARATOR);
				}
				text.append(str);
			}
			html.append("</tr>");
			text.append("\n");
		}
		html.append("</table>");
	}
}
