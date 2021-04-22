/*
 * Created on Oct 12, 2004
 *
 */
package org.neat4j.neat.data.loader;

import org.apache.log4j.Category;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutput;
import org.neat4j.neat.data.set.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MSimmerson
 *
 */
public class ListDataLoader{
	private static final Category cat = Category.getInstance(ListDataLoader.class);
	private List<List<Double>> train;
	private List<List<Double>> test;
	private List<String> headers;
	private int opCols;

	public ListDataLoader(List<List<Double>> train, List<List<Double>> test, List<String> headers, int opCols) {
		this.train = train;
		this.test = test;
		this.headers = headers;
		this.opCols = opCols;
	}

	/**
	 * @see org.neat4j.ailibrary.nn.data.DataLoader#loadData()
	 * @return
	 */



	public List<NetworkDataSet> loadData() {
		return (this.createDataSets());
	}

	private List<NetworkDataSet> createDataSets() {
		cat.debug("Creating data sets");
		List<NetworkDataSet> dataSets = new ArrayList<>(2);
		ArrayList trainIps = new ArrayList();
		ArrayList trainEOps = new ArrayList();



		ArrayList testIps = new ArrayList();
		ArrayList testEOps = new ArrayList();
		try {
			List<String> inputHeaders = new ArrayList<>(headers.size()-this.opCols);
			List<String> outputHeaders = new ArrayList<>(this.opCols);
			for (int i = 0; i < headers.size(); i++) {
				if(i < headers.size()-this.opCols) {
					inputHeaders.add(headers.get(i));
				} else {
					outputHeaders.add(headers.get(i));
				}
			}
			for (int i = 0; i < train.size(); i++) {
				this.createPattern(train.get(i), trainIps, trainEOps, opCols);
				if(test!=null){
					if(test.size() < i){
						this.createPattern(test.get(i), testIps, testEOps, opCols);
					}
				}
			}




			dataSets.add(new DataSetImpl(new InputSetImpl(inputHeaders, trainIps), new ExpectedOutputSetImpl(outputHeaders, trainEOps)));
			dataSets.add(new DataSetImpl(new InputSetImpl(inputHeaders, testIps), new ExpectedOutputSetImpl(outputHeaders, testEOps)));


		}  catch (Exception e) {
			cat.error(e.getMessage());
			e.printStackTrace();
		}

		cat.debug("Creating data sets...Done");
		return (dataSets);
	}

	private void createPattern(List<Double> data, List<NetworkInput> inps, List<NetworkOutput> eOps, int opCols) {

		NetworkInput ip;
		NetworkOutput op;

		double[] inputPattern = new double[data.size()-opCols];
		double[] outputPattern = new double[opCols];


		for (int i = 0; i < data.size(); i++) {
			if(i < data.size() - opCols){
				inputPattern[i] = data.get(i);
			} else {
				outputPattern[i] = data.get(i);
			}
		}

		inps.add(new InputImpl(inputPattern));
		eOps.add(new ExpectedOutputImpl(outputPattern));
	}






}

