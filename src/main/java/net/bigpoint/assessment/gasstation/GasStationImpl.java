package net.bigpoint.assessment.gasstation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

public class GasStationImpl implements GasStation {

	private Map<GasType, Double> gasPrices;
	private List<GasPump> gasPumps;

	private int numberOfCancellationsTooExpensive;
	private int numberOfCancellationsNoGas;
	private int numberOfSales;

	private double revenue;

	public GasStationImpl() {
		gasPrices = new HashMap<GasType, Double>();
		gasPumps = new ArrayList<GasPump>();
	}

	@Override
	public synchronized void addGasPump(GasPump pump) {
		gasPumps.add(pump);
	}

	// synch
	@Override
	public synchronized double buyGas(GasType type, double amountInLiters, double maxPricePerLiter)
			throws NotEnoughGasException, GasTooExpensiveException {
		double result = 0;
		double gasPrice = getPrice(type);
		if (gasPrice > maxPricePerLiter) {
			numberOfCancellationsTooExpensive++;
			throw new GasTooExpensiveException();
		} else {
			boolean hasPumped = false;
			for (GasPump gasPump : gasPumps) {
				if (gasPump.getGasType().equals(type) && gasPump.getRemainingAmount() >= amountInLiters) {
					gasPump.pumpGas(amountInLiters);
					result = gasPrice * amountInLiters;
					revenue += result;
					numberOfSales++;
					hasPumped = true;
					break;
				} else {
					continue;
				}
			}

			if (!hasPumped) {
				numberOfCancellationsNoGas++;
				throw new NotEnoughGasException();
			} else {
				// nothing to do
			}
		}
		return result;

	}

	@Override
	public Collection<GasPump> getGasPumps() {
		return Collections.unmodifiableCollection(gasPumps);
	}

	@Override
	public int getNumberOfCancellationsNoGas() {
		return numberOfCancellationsNoGas;
	}

	@Override
	public int getNumberOfCancellationsTooExpensive() {
		return numberOfCancellationsTooExpensive;
	}

	@Override
	public int getNumberOfSales() {
		return numberOfSales;
	}

	@Override
	public double getPrice(GasType type) {
		return gasPrices.get(type);
	}

	@Override
	public double getRevenue() {
		return revenue;
	}

	@Override
	public void setPrice(GasType type, double price) {
		gasPrices.put(type, price);
	}
}