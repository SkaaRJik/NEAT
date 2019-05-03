/*
 * Created on Oct 12, 2004
 *
 */
package org.neat4j.neat.data.set;

import org.neat4j.neat.data.core.ExpectedOutputSet;
import org.neat4j.neat.data.core.NetworkDataSet;
import org.neat4j.neat.data.core.NetworkInputSet;

/**
 * @author MSimmerson
 *
 */
public class DataSetImpl implements NetworkDataSet {

	private NetworkInputSet inputSet;
	private ExpectedOutputSet expectedOutputSet;
	
	public DataSetImpl() {
		// deliberate empty constructor
	}
	
	public DataSetImpl(NetworkInputSet inputSet, ExpectedOutputSet expectedOutputSet) {
		this.inputSet = inputSet;
		this.expectedOutputSet = expectedOutputSet;
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkDataSet#inputSet()
	 */
	public NetworkInputSet inputSet() {
		return (this.inputSet);
	}

	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkDataSet#expectedOutputSet()
	 */
	public ExpectedOutputSet expectedOutputSet() {
		return (this.expectedOutputSet);
	}
}
