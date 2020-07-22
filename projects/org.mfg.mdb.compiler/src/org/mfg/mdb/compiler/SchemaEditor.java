package org.mfg.mdb.compiler;

import static java.lang.System.out;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * A simple graphical editor for the MDB schemas. Run this class if you prefer
 * to use a graphical tool to edit and compile MDB schemas. This is an
 * alternative to the {@link Compiler} class.
 * 
 * @author arian
 * 
 */
public class SchemaEditor extends JPanel {

	private static final long serialVersionUID = 1L;
	Schema _schema;
	private JTable _tableTable;
	private AbstractTableModel _tableTableModel;
	private JFileChooser _fileChooser;
	protected File _lastSavedFile;
	JTextField _srcTextField;
	private JTextField _schemaNameTextField;
	private JTextField _packageNameTextField;
	private JFormattedTextField _bufferSizeTextField;
	JTextField _rootDirTextField;
	final Preferences _prefsNode;

	/**
	 * The constructor.
	 * 
	 * @param schema
	 *            The schema to edit.
	 */
	public SchemaEditor(final Schema schema) {
		super(new BorderLayout());

		_prefsNode = Preferences.userRoot().node("org.mfg.mdb");

		final JComponent designPanel = buildDesignPanel(schema);
		final JPanel controlPanel = buildControlPanel(schema);
		final JPanel settingsPanel = buildSettingsPanel();

		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				settingsPanel, designPanel);
		split.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				split.removeComponentListener(this);
				split.setDividerLocation(0.4f);
			}
		});
		split.setOneTouchExpandable(true);
		split.setDividerSize(10);

		add(controlPanel, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);
	}

	/**
	 * The constructor.
	 */
	public SchemaEditor() {
		this(new Schema("Schema1"));
	}

	private JPanel buildControlPanel(
			@SuppressWarnings("unused") final Schema schema) {

		_fileChooser = new JFileChooser();
		_fileChooser.setFileFilter(new FileNameExtensionFilter("MDB Schema",
				"schema-json"));

		final JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

		final JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		toolPanel.add(new JButton(new AbstractAction("Open schema") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				open();
			}
		}));

		toolPanel.add(new JButton(new AbstractAction("Save") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				save();
			}
		}));

		toolPanel.add(new JButton(new AbstractAction("Compile") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				compile();
			}
		}));

		controlPanel.add(toolPanel);

		return controlPanel;
	}

	private JPanel buildSettingsPanel() {
		final JPanel settingPanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component add(final Component comp) {
				final JLabel label = new JLabel("");
				label.setPreferredSize(new Dimension(5, 5));
				super.add(label);
				final JComponent c = (JComponent) comp;
				c.setAlignmentX(0);
				return super.add(comp);
			}
		};
		final BoxLayout layout = new BoxLayout(settingPanel, BoxLayout.Y_AXIS);
		settingPanel.setLayout(layout);

		settingPanel.add(new JLabel("Root dir: "));
		JPanel rowPanel = new JPanel(new BorderLayout(5, 0));
		_rootDirTextField = new JTextField(10);
		rowPanel.add(_rootDirTextField, BorderLayout.CENTER);
		rowPanel.add(new JButton(new AbstractAction("...") {

			private static final long serialVersionUID = 1L;
			private JFileChooser _dirChooser;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (_dirChooser == null) {
					_dirChooser = new JFileChooser();
					_dirChooser
							.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				}
				if (_dirChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					_rootDirTextField.setText(_dirChooser.getSelectedFile()
							.getAbsolutePath());
					_prefsNode.put("rootDir", _rootDirTextField.getText());
				}
			}
		}), BorderLayout.EAST);
		settingPanel.add(rowPanel);
		_rootDirTextField.setText(_prefsNode.get("rootDir",
				System.getenv("user.home")));

		settingPanel.add(new JLabel("Source dir: "));
		rowPanel = new JPanel(new BorderLayout(5, 0));
		_srcTextField = new JTextField(10);
		rowPanel.add(_srcTextField, BorderLayout.CENTER);
		rowPanel.add(new JButton(new AbstractAction("...") {

			private static final long serialVersionUID = 1L;
			private JFileChooser _dirChooser;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (_dirChooser == null) {
					_dirChooser = new JFileChooser();
					_dirChooser
							.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				}

				final File rootFile = new File(_rootDirTextField.getText());
				_dirChooser.setSelectedFile(rootFile);

				if (_dirChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					final File file = _dirChooser.getSelectedFile();
					if (file.getAbsolutePath().contains(
							rootFile.getAbsolutePath())) {
						_srcTextField.setText(file.getAbsolutePath().substring(
								rootFile.getAbsolutePath().length() + 1));
					} else {
						JOptionPane.showMessageDialog(null,
								"Inlavid Source dir. It should be a sub folder of the Root dir '"
										+ rootFile.getAbsolutePath() + "'");
					}
				}
			}
		}), BorderLayout.EAST);
		settingPanel.add(rowPanel);

		settingPanel.add(new JLabel("Schema Name: "));
		_schemaNameTextField = new JTextField(10);
		_schemaNameTextField.setText(_schema.getName());
		settingPanel.add(_schemaNameTextField);

		settingPanel.add(new JLabel("Package: "));
		_packageNameTextField = new JTextField(10);
		settingPanel.add(_packageNameTextField);

		settingPanel.add(new JLabel("Buffer Size: "));
		_bufferSizeTextField = new JFormattedTextField(new DecimalFormat());
		_bufferSizeTextField.setValue(Integer.valueOf(100));
		_bufferSizeTextField.setColumns(10);
		settingPanel.add(_bufferSizeTextField);

		final JPanel tempPanel = new JPanel(new BorderLayout());
		tempPanel.add(settingPanel, BorderLayout.NORTH);
		tempPanel.add(new JPanel(), BorderLayout.CENTER);
		tempPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return tempPanel;
	}

	void addSchemaTables() {
		final Schema schema = openSchemaFromFile();
		if (schema != null) {
			appendSchemaTables(schema);
		}
	}

	private Schema openSchemaFromFile() {
		final String lastOpenPath = _prefsNode.get("openPath", null);
		if (lastOpenPath != null) {
			_fileChooser.setCurrentDirectory(new File(lastOpenPath));
		}
		if (_fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			try {
				final File file = _fileChooser.getSelectedFile();
				_prefsNode.put("openPath", file.getAbsolutePath());

				try (FileReader fileReader = new FileReader(file)) {
					JSONObject jsonOBJ = new JSONObject(new JSONTokener(
							fileReader));
					final Schema schema = new Schema(jsonOBJ);
					return schema;
				}
			} catch (final Exception e1) {
				JOptionPane.showMessageDialog(null, "Error:" + e1.getMessage());
				e1.printStackTrace();
			}
		}
		return null;
	}

	private void appendSchemaTables(final Schema schema) {
		_schema.addAll(schema);
		_tableTableModel.fireTableDataChanged();
	}

	protected void setSchema(final Schema schema, final boolean merge) {
		if (merge) {
			for (final Table table : schema) {
				_schema.add(table);
			}
		} else {
			_schema = schema;
		}
		_tableTableModel.fireTableDataChanged();
		_tableTable.getSelectionModel().setSelectionInterval(0, 0);
	}

	private JComponent buildDesignPanel(final Schema schema) {
		_schema = schema;
		// table list

		_tableTableModel = new AbstractTableModel() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getColumnName(final int column) {
				return "Name";
			}

			@Override
			public Class<?> getColumnClass(final int columnIndex) {
				switch (columnIndex) {
				case 0:
					return String.class;
				default:
					return Boolean.class;
				}
			}

			@Override
			public boolean isCellEditable(final int rowIndex,
					final int columnIndex) {
				return true;
			}

			@Override
			public void setValueAt(final Object aValue, final int rowIndex,
					final int columnIndex) {
				final Table t = _schema.get(rowIndex);
				switch (columnIndex) {
				case 0:
					t.setName((String) aValue);
					break;
				}
			}

			@Override
			public Object getValueAt(final int rowIndex, final int columnIndex) {
				final Table table = _schema.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return table.getName();
				}
				return null;
			}

			@Override
			public int getRowCount() {
				return _schema.size();
			}

			@Override
			public int getColumnCount() {
				return 1;
			}
		};
		_tableTable = new JTable(_tableTableModel);
		_tableTable.setRowHeight(30);
		final JPanel tableButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tableButtons.add(new JButton(new AbstractAction("Add") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				addNewTable();
			}
		}));

		tableButtons.add(new JButton(new AbstractAction("Add schema") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				addSchemaTables();
			}
		}));

		tableButtons.add(new JButton(new AbstractAction("Remove") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				removeSelectedTable();
			}
		}));

		final JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tablePanel.add(tableButtons, BorderLayout.NORTH);
		tablePanel.add(new JScrollPane(_tableTable), BorderLayout.CENTER);

		// column list

		final JPanel colPanel = new JPanel(new BorderLayout());
		colPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		final AbstractTableModel colModel = createColumnsModel();

		// update columns of selected table
		_tableTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(final ListSelectionEvent e) {
						colModel.fireTableDataChanged();
					}
				});

		final JTable colTable = new JTable(colModel);
		colTable.setRowHeight(30);
		colTable.setDefaultEditor(Type.class, new DefaultCellEditor(
				new JComboBox<>(Type.values())));
		final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setForeground(Color.cyan.darker());
		colTable.setDefaultRenderer(Type.class, renderer);
		colTable.setDefaultEditor(Order.class, new DefaultCellEditor(
				new JComboBox<>(Order.values())));
		colTable.setDefaultRenderer(String.class,
				new DefaultTableCellRenderer() {

					private static final long serialVersionUID = 1L;

					@Override
					public Component getTableCellRendererComponent(
							final JTable table, final Object value,
							final boolean isSelected, final boolean hasFocus,
							final int row, final int column) {
						final Component label = super
								.getTableCellRendererComponent(table, value,
										isSelected, hasFocus, row, column);
						label.setFont(label.getFont().deriveFont(Font.BOLD));
						return label;
					}
				});
		colTable.setDefaultRenderer(Order.class,
				new DefaultTableCellRenderer() {

					private static final long serialVersionUID = 1L;

					@Override
					public Component getTableCellRendererComponent(
							final JTable table, final Object value,
							final boolean isSelected, final boolean hasFocus,
							final int row, final int column) {
						final JLabel label = (JLabel) super
								.getTableCellRendererComponent(table, value,
										isSelected, hasFocus, row, column);
						if (value == Order.NONE) {
							label.setText("");
						}
						return label;
					}
				});

		final JPanel colButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		colButtons.add(new JButton(new AbstractAction("Add new column") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final Table selectedTable = getSelectedTable();
				if (selectedTable != null) {
					final String name = JOptionPane.showInputDialog(
							"Enter the new column name",
							"Col" + (selectedTable.size() + 1));
					if (name != null) {
						selectedTable.add(new Column(name, Type.DOUBLE,
								Order.NONE, false, "", 0, UUID.randomUUID()));
						colModel.fireTableDataChanged();
					}
				}
			}
		}));

		colButtons.add(new JButton(new AbstractAction("Remove column") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final int[] rows = colTable.getSelectedRows();

				final ArrayList<Object> colsToDelete = new ArrayList<>();

				final Table selectedTable = getSelectedTable();

				for (int row : rows) {
					row = colTable.convertRowIndexToModel(row);
					colsToDelete.add(selectedTable.get(row));
				}

				for (final Object col : colsToDelete) {
					selectedTable.remove(col);
				}
				colModel.fireTableDataChanged();
			}
		}));

		colPanel.add(colButtons, BorderLayout.NORTH);
		colPanel.add(new JScrollPane(colTable), BorderLayout.CENTER);

		final JSplitPane designPanel = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT, tablePanel, colPanel);
		designPanel.setOneTouchExpandable(true);
		designPanel.setDividerSize(10);
		designPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				designPanel.setDividerLocation(0.5f);
				designPanel.removeComponentListener(this);
			}
		});

		designPanel.setRightComponent(colPanel);

		return designPanel;
	}

	private AbstractTableModel createColumnsModel() {
		return new AbstractTableModel() {

			private static final long serialVersionUID = 1L;
			private static final int COL_NAME = 0;
			private static final int COL_TYPE = 1;
			private static final int COL_ORDER = 2;
			private static final int COL_VIRTUAL = 3;
			private static final int COL_FORMULA = 4;

			@Override
			public int getRowCount() {
				final Table selectedTable = getSelectedTable();
				return selectedTable == null ? 0 : selectedTable.size();
			}

			@Override
			public int getColumnCount() {
				return 5;
			}

			@Override
			public boolean isCellEditable(final int rowIndex,
					final int columnIndex) {
				if (columnIndex == COL_FORMULA) {
					return ((Boolean) getValueAt(rowIndex, COL_VIRTUAL))
							.booleanValue();
				}
				return true;
			}

			@Override
			public void setValueAt(final Object aValue, final int rowIndex,
					final int columnIndex) {
				final Column col = getSelectedTable().get(rowIndex);
				switch (columnIndex) {
				case COL_NAME:
					col.setName((String) aValue);
					break;
				case COL_TYPE:
					col.setType((Type) aValue);
					break;
				case COL_ORDER:
					col.setOrder((Order) aValue);
					break;
				case COL_VIRTUAL:
					col.setVirtual(((Boolean) aValue).booleanValue());
					break;
				case COL_FORMULA:
					col.setFormula((String) aValue);
				}
			}

			@Override
			public String getColumnName(final int column) {
				switch (column) {
				case COL_NAME:
					return "Name";
				case COL_TYPE:
					return "Type";
				case COL_ORDER:
					return "Order";
				case COL_VIRTUAL:
					return "Virtual";
				case COL_FORMULA:
					return "Formula";
				}
				return null;
			}

			@Override
			public Class<?> getColumnClass(final int columnIndex) {
				switch (columnIndex) {
				case COL_NAME:
					return String.class;
				case COL_TYPE:
					return Type.class;
				case COL_ORDER:
					return Order.class;
				case COL_VIRTUAL:
					return Boolean.class;
				case COL_FORMULA:
					return String.class;
				}
				return null;
			}

			@Override
			public Object getValueAt(final int rowIndex, final int columnIndex) {
				final Column col = getSelectedTable().get(rowIndex);
				switch (columnIndex) {
				case COL_NAME:
					return col.getName();
				case COL_TYPE:
					return col.getType();
				case COL_ORDER:
					return col.getOrder();
				case COL_VIRTUAL:
					return Boolean.valueOf(col.isVirtual());
				case COL_FORMULA:
					return col.getFormula();
				}
				return null;
			}
		};
	}

	protected Table getSelectedTable() {
		int row = _tableTable.getSelectedRow();
		if (row != -1) {
			row = _tableTable.convertRowIndexToModel(row);
			return _schema.get(row);
		}
		return null;
	}

	void save() {
		if (_lastSavedFile != null) {
			_fileChooser.setSelectedFile(_lastSavedFile);
		}
		if (_fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = _fileChooser.getSelectedFile();
			try {

				// String extension = ".mdbs";
				final String extension = ".schema-json";

				if (!file.getName().endsWith(extension)) {
					file = new File(file.getAbsolutePath() + extension);
				}
				file.createNewFile();
				_lastSavedFile = file;
				out.println("Save to " + file.getAbsolutePath());

				updateSchemaFromUI();
				try (final FileWriter w = new FileWriter(file)) {
					final String str = _schema.toJSONString();
					w.write(new JSONObject(str).toString(4));
				} catch (final JSONException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null,
							"Error:" + e.getMessage());
				}
			} catch (final IOException e1) {
				JOptionPane.showMessageDialog(null, "Error:" + e1.getMessage());
				e1.printStackTrace();
			}

		}

		try {
			_prefsNode.flush();
		} catch (final BackingStoreException e) {
			e.printStackTrace();
		}
	}

	private void updateSchemaFromUI() {
		_schema.setBufferSize(((Number) _bufferSizeTextField.getValue())
				.intValue());
		_schema.setSource(_srcTextField.getText());
		_schema.setPkgName(_packageNameTextField.getText());
		_schema.setName(_schemaNameTextField.getText());
	}

	private void updateUIFromSchema(final Schema schema, final boolean merge) {
		if (!merge || _srcTextField.getText().trim().length() == 0) {
			_srcTextField.setText(schema.getSource());
			_bufferSizeTextField.setValue(Integer.valueOf(schema
					.getBufferSize()));
			_packageNameTextField.setText(schema.getPkgName());
			_schemaNameTextField.setText(schema.getName());
		}
		setSchema(schema, merge);
	}

	void open() {
		final Schema info = openSchemaFromFile();
		if (info != null) {

			final boolean merge = _schema == null
					|| _schema.size() == 0
					|| JOptionPane.showConfirmDialog(null,
							"Do you want to merge with the current schema?",
							"Merge", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
			updateUIFromSchema(info, merge);
		}
	}

	void compile() {
		final Compiler compiler = buildCompiler();
		try {
			final StringBuilder b = new StringBuilder();
			compiler.compile(new ICompilerListener() {

				@Override
				public void compiledFile(final File file,
						final boolean replacing) {
					b.append((replacing ? "Replacing " : "Creating ")
							+ file.getAbsolutePath() + "<br>");
				}
			});

			JOptionPane.showMessageDialog(null, "<html>" + b + "</html>",
					"Compiler messages", JOptionPane.INFORMATION_MESSAGE);

		} catch (final IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
		}
	}

	private Compiler buildCompiler() {
		final Compiler compiler = new Compiler(_schemaNameTextField.getText(),
				new File(_rootDirTextField.getText(), _srcTextField.getText())
						.getAbsolutePath(), _packageNameTextField.getText(),
				Integer.parseInt(_bufferSizeTextField.getText()));

		for (final Table t : _schema) {
			compiler.table(t.getName(), t.getUUID());
			for (final Column col : t) {
				compiler.column(col.getName(), col.getType(), col.getOrder(),
						col.isVirtual(), col.getFormula(), col.getUUID());
			}
		}
		return compiler;
	}

	JFrame showFrame() {
		final JFrame f = new JFrame("Schema Editor");
		f.setSize(800, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		f.getContentPane().add(this);
		f.setVisible(true);

		return f;
	}

	void addNewTable() {
		final String name = JOptionPane.showInputDialog("Enter the table name",
				"Table" + (_schema.size() + 1));
		if (name != null) {
			_schema.add(new Table(name, UUID.randomUUID()));
			_tableTableModel.fireTableDataChanged();
		}
	}

	void removeSelectedTable() {
		final int row = _tableTable.getSelectedRow();
		if (row != -1) {
			_schema.remove(_tableTable.convertRowIndexToModel(row));
			_tableTableModel.fireTableDataChanged();
		}
	}

	/**
	 * Run the schema editor.
	 * 
	 * @param args
	 *            Program arguments. They do not have any purpose for this
	 *            class.
	 */
	public static void main(final String[] args) {
		try {
			String laf = UIManager.getSystemLookAndFeelClassName();
			if (laf.contains("Metal") || laf.contains("GTK")) {
				for (final LookAndFeelInfo info : UIManager
						.getInstalledLookAndFeels()) {
					if (info.getClassName().contains("Nimbus")) {
						laf = info.getClassName();
						break;
					}
				}
			}
			UIManager.setLookAndFeel(laf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		new SchemaEditor(new Schema()).showFrame();
	}
}
