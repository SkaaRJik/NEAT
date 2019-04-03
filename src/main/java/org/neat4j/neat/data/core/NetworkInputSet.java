package org.neat4j.neat.data.core;

import java.io.Serializable;
import java.util.List;

/**
 * @author MSimmerson
 *
 */
public interface NetworkInputSet extends Serializable{

	public int size();
	public NetworkInput nextInput();
	public NetworkInput inputAt(int idx);	
	public void removeInputAt(int idx);
	List<String> getHeaders();
	void setHeaders(List<String> headers);


}
