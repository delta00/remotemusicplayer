package application.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import application.controller.Controller;
import cookxml.cookswing.CookSwing;

public class NetworkConfiguration {
	private Controller		controller;
	private CookSwing		cookSwing		= new CookSwing(this);
	
	public ActionListener 	buttonSave		= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			try {
				getPortNumber();
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(
					null,
					"You've entered invalid port number. It must be a numeric value in range 1 to 65535.",
					"Invalid port number",
					JOptionPane.ERROR_MESSAGE
				);
			}
		}
	};
	
	public ActionListener	buttonCancel	= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			((JFrame) cookSwing.getRootObject()).dispose();
		}
	};
	
	public NetworkConfiguration(Controller controller) {
		this.controller = controller;
		cookSwing.render("res/gui/NetworkConfiguration.xml").setVisible(true);
	}
	
	protected void setDefaultPortNumber(int portNumber) {
		((JTextField) cookSwing.getId("fieldPortNumber").object).setText(String.valueOf(portNumber));
	}
	
	protected int getPortNumber() {
		int port = Integer.valueOf(((JTextField) cookSwing.getId("fieldPortNumber").object).getText());
		
		if (port <= 0 || port > 65535) {
			throw new NumberFormatException();
		}
		
		return port;
	}
}
