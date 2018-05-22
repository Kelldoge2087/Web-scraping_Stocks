// Purpose: Final Project
// Date: 04/24/28
// Names: Adam Keller (kadam6) Meredith Wolpert (mwolpert3)

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import com.jaunt.*;


public class MainFrame extends JFrame
{
	private final int MAX_NUM_STOCKS = 50;
	
	private String[] symbols;
	private double[] shares;
	private double[] costs;
	private String[] names;
	private double[] prices;
	private double[] mktvalues;
	private double[] dollarChanges;
	private double[] percentChanges;
	private int numStocks;
		
	private JButton buttonImport;
	private JButton buttonUpdate;
//	Add JButton for About
	private JButton buttonAbout;
	private JLabel labelCost;
	private JLabel labelValue;
	private JLabel labelChangeDollar;
	private JLabel labelChangePercent;
	private JTable table;
	private DefaultTableModel tmodel;
	
	public MainFrame()
	{
		symbols = new String[MAX_NUM_STOCKS];
		shares = new double[MAX_NUM_STOCKS];
		costs = new double[MAX_NUM_STOCKS];
		names = new String[MAX_NUM_STOCKS];
		prices = new double[MAX_NUM_STOCKS];
		mktvalues = new double[MAX_NUM_STOCKS];
		dollarChanges = new double[MAX_NUM_STOCKS];
		percentChanges = new double[MAX_NUM_STOCKS];
		numStocks = 0;
		
		createGUI();

		buttonImport.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
				{
					try
					{
						readData(selectFile());
						updateTable();
					}
					
					//*******************
					// Add an exception handler here for NullPointerException (i.e., no file was selected)
					// Do NOT update the table content in this case.
					//*******************
					catch(NullPointerException e)
					{
						JOptionPane.showMessageDialog(null, "No File Selected!");
					}
					catch(NoSuchElementException e)
					{
						JOptionPane.showMessageDialog(null,"Incorrect File Format!");
						updateTable();
					}
					catch(IndexOutOfBoundsException e)
					{
						JOptionPane.showMessageDialog(null,"Exceeding the Maximum Number of Stocks (" + MAX_NUM_STOCKS + ")!");
						updateTable();
					}
					catch(Exception e)
					{
						JOptionPane.showMessageDialog(null,"Importing Portfolio Error!");
						updateTable();
					}
				}
			
			});
		
		buttonUpdate.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
				{
					if (numStocks == 0)
					{
						JOptionPane.showMessageDialog(null,"Please Import a Portfolio First!");
						return;
					}
					try
					{
						getQuotes();
						updateValues();
					}
					catch(Exception e)
					{
						JOptionPane.showMessageDialog(null,"Failed to Get Quotes!\n"+e.toString());
					}
				}
			
			});
	
		buttonAbout.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent event)
			{
				 
					
				
				JOptionPane.showMessageDialog(null, "\u00a9 2018 - MGT3745 - GATECH \n Adam Keller (kadam6) \n Meredith Wolpert (mwolpert3)");
				
			}
		});
		
		}
		//	Add about buttonAbout.addActionListener (new ActionListener()){
		
	//*******************
	// Note: You need to add the third button "About", and implement its event handler
	// Once clicked, the button pops up a message dialog box showing the author information
	// You need to add multiple lines of code at different places throughout the file (Note: don't add any code here) 
	//*******************	
	
	public void createGUI()
	{
		String[] columnNames = {"Symbol", "Name", "Quantity", "Unit Cost", "Price", "Mkt Value", "$ G/L", "% G/L"};
		tmodel = new DefaultTableModel(null, columnNames)
		  {
		    public boolean isCellEditable(int row, int column)
		    {
		      return false;
		    }
		  };
		table = new JTable(tmodel);
		table.setFillsViewportHeight(true);
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);
		
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		for (int i=2; i<columnNames.length; i++)
		{
			table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
		}
		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(1).setPreferredWidth(180);
				
		JLabel label1 = new JLabel("Total Cost: ");
		JLabel label2 = new JLabel("Total Market Value: ");
		JLabel label3 = new JLabel("Total Gain/Loss ($): ");
		JLabel label4 = new JLabel("Total Gain/Loss (%): ");
		
		labelCost = new JLabel("");
		labelValue = new JLabel("");
		labelChangePercent = new JLabel("");
		labelChangeDollar = new JLabel("");
		
		JPanel panelLabels = new JPanel(new GridLayout(2,4));
		panelLabels.add(label1);
		panelLabels.add(labelCost);
		panelLabels.add(label3);
		panelLabels.add(labelChangeDollar);
		panelLabels.add(label2);
		panelLabels.add(labelValue);
		panelLabels.add(label4);
		panelLabels.add(labelChangePercent);

		JPanel panelTemp1 = new JPanel();
		panelTemp1.setPreferredSize(new Dimension(100, 40));
		JPanel panelTemp2 = new JPanel();
		
		JPanel panelLabelsSurround = new JPanel(new BorderLayout());
		panelLabelsSurround.add(panelTemp1, BorderLayout.WEST);
		panelLabelsSurround.add(panelLabels, BorderLayout.CENTER);
		panelLabelsSurround.add(panelTemp2, BorderLayout.EAST);
				
				
		buttonImport = new JButton("Import Portfolio");
		buttonImport.setPreferredSize(new Dimension(140, 25));
		buttonUpdate = new JButton("Update");
		buttonUpdate.setPreferredSize(new Dimension(140, 25));		
		buttonAbout = new JButton("About");
		buttonAbout.setPreferredSize(new Dimension(140, 25));
		
		JPanel panelButtons = new JPanel();
		panelButtons.add(buttonImport);
		panelButtons.add(buttonUpdate);
		panelButtons.add(buttonAbout);
		
		JPanel panelBottom = new JPanel(new GridLayout(2,1,5,20));
		panelBottom.add(panelLabelsSurround);
		panelBottom.add(panelButtons);
		
		add(panelBottom, BorderLayout.SOUTH);
				
		setSize(800, 300);
		setTitle("Easy Portfolio");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);		
	}
	
	public File selectFile()
	{
		JFileChooser fileChooser = new JFileChooser(".");
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.CANCEL_OPTION)
		{
			throw new NullPointerException();
		}
		
//		If non checked exception; exception is thrown, then the flow is interupted 
		
		
		return fileChooser.getSelectedFile();
	}
	
	public void readData(File infile) throws FileNotFoundException // may throw exceptions
	{
		
		Scanner fin = new Scanner (infile);
		
		if (fin.hasNext() == false) {
			throw new NoSuchElementException();
		}
		numStocks = 0;
		while (fin.hasNext())
//			for(numStocks = 0; numStocks < MAX_NUM_STOCKS; numStocks++)
//			needs to be a while loop for homework, so that we can have a file that has an indeterminate amount of lines.
			{
				symbols[numStocks] = fin.next();
				shares[numStocks]= fin.nextDouble();
				costs[numStocks] = fin.nextDouble();
				numStocks++;
			}
			fin.close();	
			}
		
		//**********************
		// Implement the body of this method
		// This method reads the data from the selected file, and save them into the corresponding arrays: symbols, shares, costs
		//**********************

	
	public void updateTable()
	{
		tmodel.setRowCount(numStocks);
		for (int i=0; i < numStocks; i++)
		{
			table.setValueAt(symbols[i], i, 0);
			table.setValueAt("", i, 1);
			table.setValueAt(String.format("%,.2f", shares[i]), i, 2);
			table.setValueAt(String.format("%,.2f", costs[i]), i, 3);
			table.setValueAt("", i, 4);
			table.setValueAt("", i, 5);
			table.setValueAt("", i, 6);
			table.setValueAt("", i, 7);
		}
		labelCost.setText("");
		labelValue.setText("");
		labelChangeDollar.setText("");
		labelChangePercent.setText("");
	}
	
	public void getQuotes() throws JauntException
	{
		//**********************
		// Implement the body of this method
		// This method retrieves the name and the latest price for each stock symbol from online, and save them into the "names" and "prices" arrays
		//**********************
	
						
				
						for(int i = 0; i< numStocks; i++) {
							
						String url =  ("http://money.cnn.com/quote/quote.html?symb=");
						
						UserAgent agent = new UserAgent();
						agent.visit(url + symbols[i]);
						Element elementName = agent.doc.findFirst("<div id=wsod_companyName>");
						String name = elementName.innerText().trim();
						
						int end = name.indexOf('(');
						name = name.substring(0,end).trim();
												
						names[i] = (name);
						
						Element elementPrice = agent.doc.findFirst("<td class =wsod_last>");
						String priceStr = elementPrice.innerText().trim();
						
//						find location of dot
						end = priceStr.indexOf('.') + 3;
//								include 2 digits after the dot for substring 
						priceStr = priceStr.substring(0,end).trim();
						priceStr = priceStr.replaceAll("," ,  "");
						double price = Double.parseDouble(priceStr);
						
						
						prices[i] = (price);
						}
	}
						
						
	
	public void updateValues()
	{
//		Current Market Value = latest price * number of shares
//		Gain/Loss (in Dollar) = (latest price – average cost) * number of shares
//		Gain/Loss (in Percentage) = ( latest price / average cost  ‐  1 ) * 100

//		If average cost is zero, set Gain/Loss (in Percentage) to zero and do not do the division.
//		Save these values in the corresponding arrays: mktvalues[], dollarChanges[], percentChanges[].
		
//		 Total Market Value = Sum of the market value of each stock
//		 Total Cost = Sum of (average cost * number of shares) for each stock
//		 Total Gain/Loss ($) = Sum of Gain/Loss ($) for each stock
//		 Total Gain/Loss (%) = Total Gain/Loss ($) / Total Cost * 100

//		If the total cost is zero, set Total Gain/Loss (%) to zero and do not do the division.
//		You need to use local variables to store these values.

		double totalMV = 0;
		double totalCost = 0;
		double totalDollarChange = 0;
		double totalPercentChange = 0;
		
		
		for (int i = 0; i < numStocks; i++) {
			mktvalues[i] = prices[i] * shares[i];
			dollarChanges[i] = (prices[i] - costs[i])*shares[i];
			if (costs[i] != 0) 
			{
			percentChanges[i] = (prices [i]/costs[i] - 1)*100;
			}
			else
			{
				percentChanges[i] = 0;
			}
			totalMV = totalMV + mktvalues[i];
			totalCost = totalCost + (costs[i]*shares[i]);
			totalDollarChange = totalDollarChange + dollarChanges[i];
			if (totalCost !=0 ) {
				totalPercentChange = (totalDollarChange + percentChanges[i])/totalCost * 100;
			}
			else
			{
				totalPercentChange = 0;
			}
			
			tmodel.setRowCount(numStocks);
			{
				table.setValueAt(symbols[i], i, 0);
				table.setValueAt(names[i], i, 1);
				table.setValueAt(String.format("%,.2f", shares[i]), i, 2);
				table.setValueAt(String.format("%,.2f", costs[i]), i, 3);
				table.setValueAt(String.format("%,.2f", prices[i]), i, 4);
				table.setValueAt(String.format("%,.2f", mktvalues[i]), i, 5);
				table.setValueAt(String.format("%,.2f", dollarChanges[i]), i, 6);
				table.setValueAt(String.format("%+.2f", percentChanges[i]), i, 7);
			}
			labelCost.setText(String.format("$%,.2f", totalCost));
			labelValue.setText(String.format("$%,.2f", totalMV));
			labelChangeDollar.setText(String.format("$%,.2f", totalDollarChange));
			labelChangePercent.setText(String.format("%+.2f %%", totalPercentChange));
				
		}}
		
		 
		
		//**********************
		// Implement the body of this method
		// This method calculates all the measures for each stock (save them into the corresponding arrays: mktvalues, dollarChanges, percentChanges)
		// It updates the corresponding cells in "table"
		// It also calculates the measures for the entire portfolio, and updates the 4 labels: labelValue, labelCost, labelChangeDollar, labelChangePercent
		// Pay attention to the proper formatting of the numbers.
		//**********************

}

