/*
 * Created on Oct 6, 2004
 *
 */
package org.neat4j.neat.data.set;

import org.neat4j.neat.data.core.ExpectedOutputSet;
import org.neat4j.neat.data.core.NetworkOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MSimmerson
 *
 */
public class ExpectedOutputSetImpl implements ExpectedOutputSet {

	List<String> headers;

	private ArrayList ops;
	private int idx;
	
	public ExpectedOutputSetImpl(List<String> headers, ArrayList eOps) {
		this.idx = 0;
		this.ops = eOps;
		this.headers = headers;
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkOutputSet#size()
	 */
	public int size() {
		return (this.ops.size());
	}

	/**
	 * Wraps round to beginning
	 * @see org.neat4j.ailibrary.nn.data.NetworkOutputSet#nextOutput()
	 */
	public NetworkOutput nextOutput() {
		this.idx = this.idx % this.size();
		return ((NetworkOutput)this.ops.get(this.idx++));		
	}

	@Override
	public void refresh() {
		this.idx = 0;
	}

	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkOutputSet#addNetworkOutput(org.neat4j.ailibrary.nn.core.NetworkOutput)
	 */
	public void addNetworkOutput(NetworkOutput op) {
		this.ops.add(op);
	}
	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.nn.core.ExpectedOutputSet#outputAt(int)
	 */
	public NetworkOutput outputAt(int idx) {
		return ((NetworkOutput)this.ops.get(idx));		
	}

	@Override
	public List<String> getHeaders() {
		return this.headers;
	}

	@Override
	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.nn.data.NetworkOutputSet#removeNetworkOutput(int)
	 */
	public void removeNetworkOutput(int idx) {
		if (idx < this.size()) {
			this.ops.remove(idx);
		}
	}

	@Override
	public boolean hasNext() {
		return this.idx < size();
	}


}
