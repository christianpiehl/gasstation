package net.bigpoint.assessment.gasstation;

import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

public class TestGasCustomer implements Runnable {

	private long waitingTime;
	private double gasAmount;
	private double maxPrice;
	private GasStation gasStation;
	private GasType gasType;

	public TestGasCustomer(long waitingTime, GasType gasType, double gasAmount, double maxPrice,
			GasStation gasStation) {
		this.waitingTime = waitingTime;
		this.gasType = gasType;
		this.gasAmount = gasAmount;
		this.maxPrice = maxPrice;
		this.gasStation = gasStation;
	}

	@Override
	public void run() {
		while (true) {
			try {
				gasStation.buyGas(gasType, gasAmount, maxPrice);
			} catch (NotEnoughGasException | GasTooExpensiveException e) {
				// ignore
			}
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException ie) {
				// ignore
			}
		}
	}

}
