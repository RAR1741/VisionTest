package visionTracking;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

@SuppressWarnings("serial")
public class BoolIndicator extends JPanel implements ITableListener
{
	private JLabel label;
	private JLabel indicator;
	private String[] states;
	private NetworkTable t;
	private String n;
	
	public BoolIndicator(String name, String onState, String offState)
	{
		super();
		this.states = new String[2];
		this.n = name;
		this.states[0] = onState;
		this.states[1] = offState;
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.label = new JLabel();
		this.label.setText(name + ": ");
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setAlignmentY(TOP_ALIGNMENT);
		this.add(this.label);
		this.indicator = new JLabel();
		this.indicator.setForeground(Color.blue);
		this.indicator.setText("Connecting...");
		this.add(this.indicator);
	}
	
	public void Set(boolean state)
	{
		if(state)
		{
			this.indicator.setText(this.states[0]);
			this.indicator.setForeground(Color.green);
		}
		else
		{
			this.indicator.setText(this.states[1]);
			this.indicator.setForeground(Color.red);
		}
	}
	
	public void UseNetworkTable(NetworkTable table)
	{
		t = table;
		t.addTableListener(n, this, true);
	}

	@Override
	public void valueChanged(ITable arg0, String arg1, Object arg2, boolean arg3)
	{
		this.Set(this.t.getBoolean(this.n, false));
	}
}
