package visionTracking;

import javax.swing.JPanel;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

public class DashboardGenerator implements ITableListener 
{
	
	
	public DashboardGenerator(NetworkTable table, JPanel panel)
	{
		table.addTableListener(this);
	}

	@Override
	public void valueChanged(ITable arg0, String arg1, Object arg2, boolean arg3) 
	{
		if(arg1.getClass().equals(int.class))
		{
			
		}
	}
}
