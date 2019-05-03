/*
 * Created on Oct 12, 2004
 *
 */
package org.neat4j.neat.data.csv;

import org.apache.log4j.Category;
import org.neat4j.neat.data.core.*;
import org.neat4j.neat.data.set.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author MSimmerson
 *
 */
@Deprecated
public class CSVDataLoader implements DataLoader {
	private static final Category cat = Category.getInstance(CSVDataLoader.class);
	private String fileName;
	private int opCols;
	
	public CSVDataLoader(String fileName, int opCols) {
		this.fileName = fileName;
		this.opCols = opCols;
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.DataLoader#loadData()
	 * @return
	 */
	public NetworkDataSet loadData() {
		cat.info("Loading data from " + this.fileName);
		return createDataSets();
	}
	
	private NetworkDataSet createDataSets() {
		cat.debug("Creating data sets");
		NetworkDataSet dataSet = null;
		File csvFile = new File(this.fileName);
		FileInputStream fis = null;
		StringTokenizer sTok;
		String line;
		ExpectedOutputSet opSet;
		ArrayList eOps = new ArrayList();
		NetworkInputSet ipSet;
		ArrayList ips = new ArrayList();
		int tokens;
		try {
			fis = new FileInputStream(csvFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			sTok = new StringTokenizer(br.readLine(), ";");
			tokens = sTok.countTokens();
			List<String> inputHeaders = new ArrayList<>(tokens-this.opCols);
			List<String> outputHeaders = new ArrayList<>(this.opCols);
			for (int i = 0; i < tokens-this.opCols; i++) {
				inputHeaders.add(sTok.nextToken());
			}
			while(sTok.hasMoreTokens()){
				outputHeaders.add(sTok.nextToken());
			}


			while ((line = br.readLine()) != null) {
				sTok = new StringTokenizer(line, "[,; ]");
				ips.add(this.createInputPattern(sTok));
				eOps.add(this.createExpectedOutput(sTok));
			}
			ipSet = new InputSetImpl(inputHeaders, ips);
			opSet = new ExpectedOutputSetImpl(outputHeaders, eOps);
			dataSet = new DataSetImpl(ipSet, opSet);
			fis.close();
		} catch (FileNotFoundException e) {
			cat.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			cat.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			cat.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if(fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					cat.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		cat.debug("Creating data sets...Done");
		return (dataSet);
	}
	
	private NetworkInput createInputPattern(StringTokenizer sTok) {
		double[] pattern = new double[sTok.countTokens() - this.opCols];
		NetworkInput ip;
		int i = 0;
		
		while (sTok.hasMoreTokens() && i < pattern.length) {
			pattern[i++] = Double.parseDouble(sTok.nextToken());
		}
		
		ip = new InputImpl(pattern);
		
		return (ip);
	}

	private NetworkOutput createExpectedOutput(StringTokenizer sTok) {
		int outputs = sTok.countTokens();
		List<Double> pattern = new ArrayList<>(outputs);
		NetworkOutput op;
		int i = 0;
		String val;
		while (sTok.hasMoreTokens() && i < outputs) {
			val = sTok.nextToken();
			if(!val.equals("null")) {
				pattern.add(Double.parseDouble(val));
				i++;
			}
		}
		
		op = new ExpectedOutputImpl(pattern);
		
		return (op);
	}


}

