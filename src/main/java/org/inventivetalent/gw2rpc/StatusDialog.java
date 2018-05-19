package org.inventivetalent.gw2rpc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StatusDialog extends JDialog {

	final Gw2Rpc main;

	private JPanel     contentPane;
	private JButton    buttonCancel;
	public  JLabel     statusLabel;
	public  JTextField character;
	public  JTextField race;
	public  JTextField profession;
	public  JTextField continent;
	public  JTextField region;
	public  JTextField map;
	public  JTextField sector;
	public  JTextField poi;
	public  JTextField coordsX;
	public  JTextField raceId;
	public  JTextField professionId;
	public  JTextField continentId;
	public  JTextField regionId;
	public  JTextField mapId;
	public  JTextField sectorId;
	public  JTextField poiChat;
	public  JTextField coordsY;
	public  JTextField detailsFormat;
	public  JTextField stateFormat;

	public StatusDialog(Gw2Rpc main) {
		this.main = main;

		setContentPane(contentPane);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationByPlatform(true);

		setTitle("Guild Wars 2 Discord Rich Presence");
		setPreferredSize(new Dimension(350, 420));

		setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("Guild_Wars_2_Dragon_logo.jpg")));

		detailsFormat.setText(PresenceUpdater.DETAILS_FORMAT);
		stateFormat.setText(PresenceUpdater.STATE_FORMAT);

		buttonCancel.addActionListener(e -> onCancel());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

	}

	public void display() {
		pack();
		setVisible(true);
	}

	private void onCancel() {
		statusLabel.setText("Preparing to shut down...");
		main.shouldShutdown = true;
		buttonCancel.setEnabled(false);
	}
}
