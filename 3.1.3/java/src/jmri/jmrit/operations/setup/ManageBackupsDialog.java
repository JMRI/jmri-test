package jmri.jmrit.operations.setup;

import java.awt.BorderLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JList;
import java.awt.Insets;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;

public class ManageBackupsDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JLabel selectBackupSetsLabel;
	private JButton selectAllButton;
	private JButton clearAllButton;
	private JScrollPane scrollPane;
	private JList setList;

	private JButton deleteButton;
	private JButton helpButton;

	private DefaultListModel model;

	private BackupBase backup;
	private Component horizontalGlue;
	private Component horizontalStrut;
	private Component horizontalStrut_1;
	private Component horizontalStrut_2;

	/**
	 * Create the dialog.
	 */
	public ManageBackupsDialog() {
		// For now we only support Autobackups, but this can be updated later if
		// needed.
		backup = new AutoBackup();

		initComponents();
	}

	private void initComponents() {
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setModal(true);
		setTitle("Manage Backup Sets");
		setBounds(100, 100, 461, 431);
		BorderLayout borderLayout = new BorderLayout();
		borderLayout.setVgap(5);
		borderLayout.setHgap(5);
		getContentPane().setLayout(borderLayout);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			selectBackupSetsLabel = new JLabel(
					"Select Backup Sets to delete. Use Ctrl-Click to select multiple sets.");
			GridBagConstraints gbc_selectBackupSetsLabel = new GridBagConstraints();
			gbc_selectBackupSetsLabel.anchor = GridBagConstraints.WEST;
			gbc_selectBackupSetsLabel.insets = new Insets(0, 0, 5, 0);
			gbc_selectBackupSetsLabel.gridx = 0;
			gbc_selectBackupSetsLabel.gridy = 0;
			contentPanel.add(selectBackupSetsLabel, gbc_selectBackupSetsLabel);
		}
		{
			scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 1;
			contentPanel.add(scrollPane, gbc_scrollPane);
			{
				setList = new JList();
				setList.setVisibleRowCount(20);

				model = new DefaultListModel();

				// Load up the list control with the available BackupSets
				for (BackupSet bs : backup.getBackupSets()) {
					model.addElement(bs);
				}

				setList.setModel(model);

				// Update button states based on if anything is selected in the
				// list.
				setList.addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						updateButtonStates();
					}
				});
				scrollPane.setViewportView(setList);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				selectAllButton = new JButton("Select all");
				selectAllButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						do_selectAllButton_actionPerformed(e);
					}
				});
				buttonPane.add(selectAllButton);
			}
			{
				horizontalStrut = Box.createHorizontalStrut(10);
				buttonPane.add(horizontalStrut);
			}
			{
				clearAllButton = new JButton("Clear all");
				clearAllButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						do_clearAllButton_actionPerformed(e);
					}
				});
				buttonPane.add(clearAllButton);
			}
			{
				horizontalGlue = Box.createHorizontalGlue();
				buttonPane.add(horizontalGlue);
			}
			{
				deleteButton = new JButton("Delete");
				deleteButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						do_deleteButton_actionPerformed(e);
					}
				});
				deleteButton.setActionCommand("");
				buttonPane.add(deleteButton);
			}
			{
				horizontalStrut_1 = Box.createHorizontalStrut(10);
				buttonPane.add(horizontalStrut_1);
			}
			{
				JButton closeButton = new JButton("Close");
				closeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						do_cancelButton_actionPerformed(e);
					}
				});
				closeButton.setActionCommand("Cancel");
				getRootPane().setDefaultButton(closeButton);
				buttonPane.add(closeButton);
			}
			{
				horizontalStrut_2 = Box.createHorizontalStrut(10);
				buttonPane.add(horizontalStrut_2);
			}
			{
				helpButton = new JButton("Help");
				helpButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						do_helpButton_actionPerformed(e);
					}
				});
				helpButton.setEnabled(false);
				buttonPane.add(helpButton);
			}
		}

		updateButtonStates();
	}

	protected void do_cancelButton_actionPerformed(ActionEvent e) {
		dispose();
	}

	protected void do_clearAllButton_actionPerformed(ActionEvent e) {
		setList.clearSelection();
	}

	protected void do_deleteButton_actionPerformed(ActionEvent e) {
		// Here we get the selected items from the list
		Object[] objs = setList.getSelectedValues();

		int count = objs.length;
		if (count > 0) {
			// Make sure OK to delete backups
			String msg = String
					.format("You are about to delete %d Backup Sets. OK to delete them?",
							count);
			int result = JOptionPane.showConfirmDialog(this, msg,
					"Deleting Backup Sets", JOptionPane.OK_CANCEL_OPTION);

			if (result == JOptionPane.OK_OPTION) {
				for (Object obj : objs) {
					BackupSet set = (BackupSet) obj;
					model.removeElement(obj);

					// For now, the BackupSet deletes the associated files, but
					// we might want to move this into the BackupBase class just
					// so that it knows what is happening.
					set.delete();
				}
			}
		}
	}

	protected void do_helpButton_actionPerformed(ActionEvent e) {
		// Not implemented yet.
	}

	protected void do_selectAllButton_actionPerformed(ActionEvent e) {
		setList.setSelectionInterval(0, model.getSize() - 1);
	}

	private void updateButtonStates() {
		// Update the various button enabled states based on what is in the list
		// and what is selected.
		boolean notEmpty = !setList.isSelectionEmpty();

		deleteButton.setEnabled(notEmpty);
		clearAllButton.setEnabled(notEmpty);

		// Can only select if we have something to select!
		int count = model.size();
		selectAllButton.setEnabled(count > 0);
	}
}
