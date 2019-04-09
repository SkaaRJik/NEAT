/*
 * Created on Oct 1, 2004
 *
 */
package org.neat4j.neat.data.core;

import java.io.Serializable;
import java.util.List;

/**
 * @author MSimmerson
 *
 */
public interface NetworkOutput extends Serializable {
	List<Double> getNetOutputs();
}
