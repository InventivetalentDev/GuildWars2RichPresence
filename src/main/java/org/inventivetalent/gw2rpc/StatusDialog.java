package org.inventivetalent.gw2rpc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StatusDialog extends JDialog {

	final Gw2Rpc main;
	JLabel labelContent;
	private JButton buttonCancel;

	StatusDialog(Gw2Rpc main) {
		this.main = main;
	}

	void createDialog() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationByPlatform(true);

		setTitle("Guild Wars 2 Discord Rich Presence");
		setPreferredSize(new Dimension(350, 100));

		JPanel contentPane = new JPanel();
		labelContent = new JLabel("");
		contentPane.add(labelContent);

		JPanel buttonPane = new JPanel();
		buttonCancel = new JButton("Exit");
		buttonPane.add(buttonCancel);
		contentPane.add(buttonPane);

		add(contentPane);

		buttonCancel.addActionListener(e -> onCancel());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		pack();
		setVisible(true);
	}

	private void onCancel() {
		main.shouldShutdown = true;
	}

}
