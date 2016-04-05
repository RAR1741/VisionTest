package visionTracking;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

@SuppressWarnings("serial")
public class IntChooser extends JPanel implements ChangeListener 
{
	private String n;
	private NetworkTable t;
	private JLabel label;
	private JSpinner spinner;

	public IntChooser(String name, int d, int min, int max, NetworkTable table)
	{
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setAlignmentY(TOP_ALIGNMENT);
		n = name;
		t = table;
		label = new JLabel(name + ": ");
		this.add(label);
		SpinnerModel model = new SpinnerNumberModel(d, min, max, 1);
		spinner = new JSpinner(model);
		spinner.setMaximumSize(new Dimension(50, 50));
		this.add(spinner);
		spinner.addChangeListener(this);
		t.putNumber(n, (int)spinner.getValue());
	}

	@Override
	public void stateChanged(ChangeEvent e) 
	{
		t.putNumber(n, (int)spinner.getValue());
	}
}
