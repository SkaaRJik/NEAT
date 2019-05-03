/*
 * Created on Oct 6, 2004
 *
 */
package org.neat4j.neat.data.set;

import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkInputSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MSimmerson
 *
 */
public class InputSetImpl implements NetworkInputSet {


	private List<String> headers;
	private List inputs;
	private int idx;
	
	public InputSetImpl(List<String> headers , ArrayList inputs) {
		this.headers = headers;
		this.inputs = inputs;
		this.idx = 0;
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkInputSet#size()
	 */
	public int size() {
		return (this.inputs.size());
	}

	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkInputSet#nextInput()
	 */
	public NetworkInput nextInput() {
		this.idx = this.idx % this.size();
		return ((NetworkInput)this.inputs.get(idx++));		
	}
	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.nn.core.NetworkInputSet#inputAt(int)
	 */
	public NetworkInput inputAt(int idx) {
		return ((NetworkInput)this.inputs.get(idx));		
	}
	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.nn.data.NetworkInputSet#removeInputAt(int)
	 */
	public void removeInputAt(int idx) {
		if (idx < this.size()) {
			this.inputs.remove(idx);
		}
	}

	@Override
	public List<String> getHeaders() {
		return this.headers;
	}

	@Override
	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}
}
