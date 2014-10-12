// Kernel.java

package jmri.jmrit.operations.rollingstock.cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockGroup;

/**
 * A Kernel is a group of cars that is managed as one car.
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version $Revision$
 */
public class Kernel extends RollingStockGroup {

	public Kernel(String name) {
		super(name);
		log.debug("New Kernel ({})", name);
	}

	public List<Car> getCars() {
		List<Car> cars = new ArrayList<Car>();
		for (RollingStock rs : getGroup()) {
			cars.add((Car) rs);
		}
		return cars;
	}

	public void dispose() {
		while (getGroup().size() > 0) {
			Car car = (Car) getGroup().get(0);
			if (car != null) {
				car.setKernel(null);
			}
		}
		super.dispose();
	}

	static Logger log = LoggerFactory.getLogger(Kernel.class.getName());
}
