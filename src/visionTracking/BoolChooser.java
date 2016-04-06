package visionTracking;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

@SuppressWarnings("serial")
public class BoolChooser extends JPanel implements ActionListener
{
	private JLabel l;
	private JButton b1;
	private JButton b2;
	private String n;
	private NetworkTable t;
	
	public void actionPerformed(ActionEvent arg0) 
	{
		t.putBoolean(n, arg0.getSource().equals(b1));
	}

	public BoolChooser(String name, String label1, NetworkTable table)
	{
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setAlignmentY(TOP_ALIGNMENT);
		t = table;
		n = name;
		l = new JLabel(n + ": ");
		add(l);
		b1 = new JButton(label1);
		add(b1);
		b1.addActionListener(this);
	}
	
	public BoolChooser(String name, String label1, String label2, NetworkTable table)
	{
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setAlignmentY(TOP_ALIGNMENT);
		t = table;
		n = name;
		l = new JLabel(n + ": ");
		add(l);
		b1 = new JButton(label1);
		add(b1);
		b1.addActionListener(this);
		b2 = new JButton(label2);
		add(b2);
		b2.addActionListener(this);
	}
}
