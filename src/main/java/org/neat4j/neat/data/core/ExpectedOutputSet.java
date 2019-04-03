package org.neat4j.neat.data.core;

import java.util.List;

/**
 * @author MSimmerson
 *
 */
public interface ExpectedOutputSet extends NetworkOutputSet{
	public NetworkOutput outputAt(int idx);

	List<String> getHeaders();
	void setHeaders(List<String> headers);
}
