package net.bigpoint.assessment.gasstation;

import static net.bigpoint.assessment.gasstation.GasType.DIESEL;
import static net.bigpoint.assessment.gasstation.GasType.REGULAR;
import static net.bigpoint.assessment.gasstation.GasType.SUPER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

public class GasStationTest {

	private GasStation gasStation;

	private static final double regularPrice = 1.40;
	private static final double superPrice = 1.42;
	private static final double dieselPrice = 1.20;

	@Before
	public void setUpGasStation() {
		gasStation = new GasStationImpl();
		gasStation.setPrice(DIESEL, dieselPrice);
		gasStation.setPrice(REGULAR, regularPrice);
		gasStation.setPrice(SUPER, superPrice);

		GasPump superGasPump = new GasPump(SUPER, 200);
		gasStation.addGasPump(superGasPump);

		GasPump dieselGasPump = new GasPump(DIESEL, 150);
		gasStation.addGasPump(dieselGasPump);

		GasPump regularGasPump = new GasPump(REGULAR, 100);
		gasStation.addGasPump(regularGasPump);

		GasPump regularGasPump2 = new GasPump(REGULAR, 200);
		gasStation.addGasPump(regularGasPump2);
	}

	@Test()
	public void testGasPumps() {
		Collection<GasPump> gasPumps = gasStation.getGasPumps();
		assertEquals(gasPumps.size(), 4);

		boolean isExceptionThrown = false;
		try {
			gasPumps.add(new GasPump(SUPER, 100));
		} catch (UnsupportedOperationException uoe) {
			isExceptionThrown = true;
		}
		assertTrue(isExceptionThrown);

		isExceptionThrown = false;
		try {
			gasPumps.clear();
		} catch (UnsupportedOperationException uoe) {
			isExceptionThrown = true;
		}
		assertTrue(isExceptionThrown);
	}

	@Test
	public void testGasPrices() {
		assertEquals(superPrice, gasStation.getPrice(SUPER), 0);
		assertEquals(regularPrice, gasStation.getPrice(REGULAR), 0);
		assertEquals(dieselPrice, gasStation.getPrice(DIESEL), 0);

		gasStation.setPrice(SUPER, regularPrice);
		assertEquals(regularPrice, gasStation.getPrice(SUPER), 0);

		gasStation.setPrice(REGULAR, dieselPrice);
		assertEquals(dieselPrice, gasStation.getPrice(REGULAR), 0);

		gasStation.setPrice(DIESEL, superPrice);
		assertEquals(superPrice, gasStation.getPrice(DIESEL), 0);
	}

	@Test
	public void testBuyGasExceptions() {
		boolean isExceptionThrown = false;
		try {
			gasStation.buyGas(SUPER, 50, 1.41);
		} catch (NotEnoughGasException nege) {
			// ignore
		} catch (GasTooExpensiveException gtee) {
			isExceptionThrown = true;
		}
		assertTrue(isExceptionThrown);

		isExceptionThrown = false;
		try {
			gasStation.buyGas(SUPER, 50, 1.42);
		} catch (NotEnoughGasException nege) {
			// ignore
		} catch (GasTooExpensiveException gtee) {
			isExceptionThrown = true;
		}
		assertFalse(isExceptionThrown);

		isExceptionThrown = false;
		try {
			gasStation.buyGas(DIESEL, 151, 1.5);
		} catch (NotEnoughGasException nege) {
			isExceptionThrown = true;
		} catch (GasTooExpensiveException gtee) {
			// ignore
		}
		assertTrue(isExceptionThrown);

		isExceptionThrown = false;
		try {
			gasStation.buyGas(DIESEL, 150, 1.5);
		} catch (NotEnoughGasException nege) {
			isExceptionThrown = true;
		} catch (GasTooExpensiveException gtee) {
			// ignore
		}
		assertFalse(isExceptionThrown);

	}

	@Test
	public void testSimplePumpings() {
		assertEquals(0, gasStation.getNumberOfCancellationsNoGas());
		assertEquals(0, gasStation.getNumberOfCancellationsTooExpensive());
		assertEquals(0, gasStation.getNumberOfSales());
		assertEquals(0, gasStation.getRevenue(), 0);

		double gasStationPrice = 0;
		try {
			gasStationPrice = gasStation.buyGas(SUPER, 50, superPrice);
		} catch (NotEnoughGasException | GasTooExpensiveException e) {
			assertTrue(false);
		}
		double expectedPrice = 50 * superPrice;
		double expectedRevenue = expectedPrice;
		assertEquals(expectedPrice, gasStationPrice, 0);

		assertEquals(0, gasStation.getNumberOfCancellationsNoGas());
		assertEquals(0, gasStation.getNumberOfCancellationsTooExpensive());
		assertEquals(1, gasStation.getNumberOfSales());
		assertEquals(expectedRevenue, gasStation.getRevenue(), 0);

		try {
			gasStationPrice = gasStation.buyGas(SUPER, 150, 1.42);
		} catch (NotEnoughGasException | GasTooExpensiveException e) {
			assertTrue(false);
		}
		expectedPrice = 150 * superPrice;
		expectedRevenue += expectedPrice;
		assertEquals(expectedPrice, gasStationPrice, 0);

		assertEquals(0, gasStation.getNumberOfCancellationsNoGas());
		assertEquals(0, gasStation.getNumberOfCancellationsTooExpensive());
		assertEquals(2, gasStation.getNumberOfSales());
		assertEquals(expectedRevenue, gasStation.getRevenue(), 0);

		try {
			gasStationPrice = gasStation.buyGas(SUPER, 1, 1.5);
		} catch (NotEnoughGasException | GasTooExpensiveException e) {
			// ignore
		}
		assertEquals(1, gasStation.getNumberOfCancellationsNoGas());
		assertEquals(0, gasStation.getNumberOfCancellationsTooExpensive());
		assertEquals(2, gasStation.getNumberOfSales());
		assertEquals(expectedRevenue, gasStation.getRevenue(), 0);

		try {
			gasStationPrice = gasStation.buyGas(SUPER, 1, 1.3);
		} catch (NotEnoughGasException | GasTooExpensiveException e) {
			// ignore
		}
		assertEquals(1, gasStation.getNumberOfCancellationsNoGas());
		assertEquals(1, gasStation.getNumberOfCancellationsTooExpensive());
		assertEquals(2, gasStation.getNumberOfSales());
		assertEquals(expectedRevenue, gasStation.getRevenue(), 0);
	}

	@Test
	public void testMultiCustomer() {

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
		assertEquals(30,  remainingRegular, 0);
		assertEquals(20,  remainingSuper, 0);

	}

}