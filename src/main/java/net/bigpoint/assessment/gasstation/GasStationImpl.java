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
	private List<GasPump> gasPumpsInUse;

	private Integer numberOfCancellationsTooExpensive;
	private Integer numberOfCancellationsNoGas;
	private Integer numberOfSales;

	private Double revenue;

	public GasStationImpl() {
		gasPrices = new HashMap<GasType, Double>();
		gasPumps = new ArrayList<GasPump>();
		gasPumpsInUse = new ArrayList<>();
		numberOfCancellationsTooExpensive = 0;
		numberOfCancellationsNoGas = 0;
		numberOfSales = 0;
		revenue = 0.0;
	}

	@Override
	public void addGasPump(GasPump pump) {
		synchronized (gasPumps) {
			gasPumps.add(pump);
		}
	}

	@Override
	public double buyGas(GasType type, double amountInLiters, double maxPricePerLiter)
			throws NotEnoughGasException, GasTooExpensiveException {
		double result;
		double gasPrice = getPrice(type);
		if (gasPrice > maxPricePerLiter) {
			incNumberOfCancellationsTooExpensive();
			throw new GasTooExpensiveException();
		} else {
			GasPump gasPump = occupyGasPump(type, amountInLiters);
			if (gasPump == null) {
				result = 0;
				incNumberOfCancellationsNoGas();
				// using a third exception when all gas pumps are occupied would
				// be a better solution
				throw new NotEnoughGasException();
			} else {
				gasPump.pumpGas(amountInLiters);
				result = gasPrice * amountInLiters;
				addToRevenue(result);
				incNumberOfSales();
				freeGasPump(gasPump);
			}
		}
		return result;
	}

	private void freeGasPump(GasPump gasPump) {
		synchronized (gasPumpsInUse) {
			gasPumpsInUse.remove(gasPump);
		}
	}

	private void addToRevenue(double money) {
		synchronized (revenue) {
			revenue += money;
		}
	}

	private GasPump occupyGasPump(GasType gasType, double amountInLiters) {
		GasPump result = null;
		synchronized (gasPumps) {
			synchronized (gasPumpsInUse) {
				for (GasPump gasPump : gasPumps) {
					if (gasPump.getGasType().equals(gasType) && gasPump.getRemainingAmount() >= amountInLiters
							&& !gasPumpsInUse.contains(gasPump)) {
						result = gasPump;
						gasPumpsInUse.add(gasPump);
						break;
					} else {
						continue;
					}
				}
			}
		}
		return result;
	}

	private void incNumberOfCancellationsNoGas() {
		synchronized (numberOfCancellationsNoGas) {
			numberOfCancellationsNoGas++;
		}
	}

	private void incNumberOfSales() {
		synchronized (numberOfSales) {
			numberOfSales++;
		}
	}

	private void incNumberOfCancellationsTooExpensive() {
		synchronized (numberOfCancellationsTooExpensive) {
			numberOfCancellationsTooExpensive++;
		}
	}

	@Override
	public Collection<GasPump> getGasPumps() {
		Collection<GasPump> result;
		synchronized (gasPumps) {
			result = Collections.unmodifiableCollection(gasPumps);
		}
		return result;
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