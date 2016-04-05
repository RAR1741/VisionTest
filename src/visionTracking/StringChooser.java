package visionTracking;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

@SuppressWarnings("serial")
public class StringChooser extends JPanel implements ActionListener 
{
	private String n;
	private String[] c;
	private NetworkTable t;
	private JLabel label;
	private JComboBox<String> chooser;
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		t.putString(n, (String)chooser.getSelectedItem());
	}

	public StringChooser(String name, String[] choices, NetworkTable table)
	{
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setAlignmentY(TOP_ALIGNMENT);
		n = name;
		c = choices;
		t = table;
		label = new JLabel(name + ": ");
		this.add(label);
		chooser = new JComboBox<String>(c);
		this.add(chooser);
	}
}
