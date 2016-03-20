package net.bigpoint.assessment.gasstation;

import static net.bigpoint.assessment.gasstation.GasType.DIESEL;
import static net.bigpoint.assessment.gasstation.GasType.REGULAR;
import static net.bigpoint.assessment.gasstation.GasType.SUPER;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

public class GasStationTest {

	private GasStation gasStation;

	private final double regularPrice = 1.40;
	private final double superPrice = 1.42;
	private final double dieselPrice = 1.20;

	private final double superStartAmount = 200;
	private final double dieselStartAmount = 150;
	private final double regularStartAmount1 = 100;
	private final double regularStartAmount2 = 200;

	@Test(expected = GasTooExpensiveException.class)
	public void buyGas_gasTooExpensive() throws Exception {
		gasStation.buyGas(SUPER, 50, 1.41);
	}

	@Test(expected = NotEnoughGasException.class)
	public void buyGas_notEnoughGas() throws Exception {
		gasStation.buyGas(DIESEL, 151, 1.5);
	}

	@Test
	public void buyGas_numberCancellationsNoGas() {
		try {
			gasStation.buyGas(SUPER, 201, 1.42);
		} catch (Exception e) {
			// ignore
		}
		int expectedCancellations = 1;
		int cancellations = gasStation.getNumberOfCancellationsNoGas();
		assertEquals(expectedCancellations, cancellations, 0);
	}

	@Test
	public void buyGas_numberCancellationsTooExpensive() {
		try {
			gasStation.buyGas(SUPER, 50, 1.41);
		} catch (Exception e) {
			// ignore
		}
		int expectedCancellations = 1;
		int cancellations = gasStation.getNumberOfCancellationsTooExpensive();
		assertEquals(expectedCancellations, cancellations, 0);
	}

	@Test
	public void buyGas_numberOfSales() throws Exception {
		gasStation.buyGas(SUPER, 50, 1.42);
		int expectedNumberOfSales = 1;
		int numberOfSales = gasStation.getNumberOfSales();
		assertEquals(expectedNumberOfSales, numberOfSales, 0);
	}

	@Test
	public void buyGas_numberOfSalesNotChangedByCancellations() {
		try {
			gasStation.buyGas(SUPER, 201, 1.42);
		} catch (NotEnoughGasException | GasTooExpensiveException e) {
			// ignore
		}
		try {
			gasStation.buyGas(SUPER, 50, 1.20);
		} catch (NotEnoughGasException | GasTooExpensiveException e) {
			// ignore
		}

		int expectedNumberOfSales = 0;
		int numberOfSales = gasStation.getNumberOfSales();
		assertEquals(expectedNumberOfSales, numberOfSales, 0);
	}

	@Test
	public void buyGas_remainingAmount() throws Exception {
		double amount = 5;
		gasStation.buyGas(SUPER, amount, 1.42);
		double remainingAmount = 0;
		for (GasPump gasPump : gasStation.getGasPumps()) {
			if (gasPump.getGasType() == GasType.SUPER) {
				remainingAmount = gasPump.getRemainingAmount();
			}
		}
		double expectedAmount = superStartAmount - amount;
		assertEquals(expectedAmount, remainingAmount, 0);
	}

	@Test
	public void buyGas_revenue() throws Exception {
		double amount = 5;
		gasStation.buyGas(SUPER, amount, 1.42);
		double revenue = gasStation.getRevenue();
		double expectedRevenue = amount * superPrice;
		assertEquals(expectedRevenue, revenue, 0);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getGasPumps_add() {
		Collection<GasPump> gasPumps = gasStation.getGasPumps();
		gasPumps.add(new GasPump(SUPER, 100));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getGasPumps_clear() {
		Collection<GasPump> gasPumps = gasStation.getGasPumps();
		gasPumps.clear();
	}

	@Test
	public void setPrice_normalBehaviour() {
		gasStation.setPrice(SUPER, regularPrice);
		assertEquals(regularPrice, gasStation.getPrice(SUPER), 0);
	}

	@Before
	public void setUpGasStation() {
		gasStation = new GasStationImpl();
		gasStation.setPrice(DIESEL, dieselPrice);
		gasStation.setPrice(REGULAR, regularPrice);
		gasStation.setPrice(SUPER, superPrice);

		GasPump superGasPump = new GasPump(SUPER, superStartAmount);
		gasStation.addGasPump(superGasPump);

		GasPump dieselGasPump = new GasPump(DIESEL, dieselStartAmount);
		gasStation.addGasPump(dieselGasPump);

		GasPump regularGasPump = new GasPump(REGULAR, regularStartAmount1);
		gasStation.addGasPump(regularGasPump);

		GasPump regularGasPump2 = new GasPump(REGULAR, regularStartAmount2);
		gasStation.addGasPump(regularGasPump2);
	}

	@Test
	public void buyGas_multiCustomer() {
		/*
		 * Regarding your hints this is no good test-method. But right now I
		 * have no better idea how to test the multi-threaded access to the gas
		 * station.
		 */
		TestGasCustomer customer1 = new TestGasCustomer(1000, SUPER, 30, 1.50, gasStation);
		TestGasCustomer customer2 = new TestGasCustomer(1200, DIESEL, 60, 1.50, gasStation);
		TestGasCustomer customer3 = new TestGasCustomer(1300, REGULAR, 30, 1.50, gasStation);

		new Thread(customer1).start();
		new Thread(customer2).start();
		new Thread(customer3).start();

		try {
			// 10 lit per sec -> 600 lit per min
			Thread.sleep(60_000);
		} catch (InterruptedException e) {
			// ignore
		}

		double expectedRevenue = 270 * regularPrice;
		expectedRevenue += 180 * superPrice;
		expectedRevenue += 120 * dieselPrice;
		assertEquals(expectedRevenue, gasStation.getRevenue(), 0);
		assertEquals(17, gasStation.getNumberOfSales());

		double remainingDiesel = 0;
		double remainingRegular = 0;
		double remainingSuper = 0;
		Collection<GasPump> gasPumps = gasStation.getGasPumps();
		for (GasPump gasPump : gasPumps) {
			switch (gasPump.getGasType()) {
			case DIESEL:
				remainingDiesel += gasPump.getRemainingAmount();
				break;
			case REGULAR:
				remainingRegular += gasPump.getRemainingAmount();
				break;
			case SUPER:
				remainingSuper += gasPump.getRemainingAmount();
				break;
			default:
				break;
			}
		}
		assertEquals(30, remainingDiesel, 0);
		assertEquals(30, remainingRegular, 0);
		assertEquals(20, remainingSuper, 0);
	}
}