package com.lambdalogic.test.booking;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.lambdalogic.test.booking.exception.InconsistentCurrenciesException;
import com.lambdalogic.test.booking.model.Booking;
import com.lambdalogic.test.booking.model.CurrencyAmount;
import com.lambdalogic.test.booking.model.Price;

public class BookingsCurrencyAmountsEvaluatorTest {

	private BookingsCurrencyAmountsEvaluator sut; // sut stands for 'subject under test'
	
	@Before
	public void setUp() {
		sut = new BookingsCurrencyAmountsEvaluator();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void calculate_throws_IllegalArgumentException_if_empty_list_is_passed() throws InconsistentCurrenciesException {
		List<Booking> bookings = new ArrayList<>();
		sut.calculate(bookings, 1L);
	}
	
	@Test
	public void calculate_totalAmounts_should_be_calculated_as_expected() throws InconsistentCurrenciesException{
		final String currency = "USD";
		Booking booking1 = createBooking(currency, 1L, 4.50, 19L);
		Booking booking2 = createBooking(currency, 1L, 10.50, 19L);
		List<Booking> bookings = new ArrayList<>(Arrays.asList(booking1, booking2));
		sut.calculate(bookings, 1L);
		CurrencyAmount expected = new CurrencyAmount(BigDecimal.valueOf(15.0), currency);
		assertEquals(expected, sut.getTotalAmount());
		assertEquals(expected, sut.getTotalOpenAmount());
		assertEquals(new CurrencyAmount(BigDecimal.ZERO, currency), sut.getTotalPaidAmount());
	}

	@Test
	public void calculate_calculates_totalAmounts_correctly_if_paidAmont_is_set() throws InconsistentCurrenciesException {
		final String currency = "USD";
		Booking booking1 = createBooking(currency, 1L, 4.50, 19L);
		Booking booking2 = createBooking(currency, 1L, 10.50, 19L);
		booking2.setPaidAmount(BigDecimal.ONE);
		List<Booking> bookings = new ArrayList<>(Arrays.asList(booking1, booking2));
		
		sut.calculate(bookings, 1L);
		
		CurrencyAmount expectedTotalAmount = new CurrencyAmount(BigDecimal.valueOf(15.0), currency);
		CurrencyAmount expectedOpenAmount = new CurrencyAmount(BigDecimal.valueOf(14.0), currency);
		
		assertEquals(expectedTotalAmount, sut.getTotalAmount());
		assertEquals(expectedOpenAmount, sut.getTotalOpenAmount());
	}
	
	
	private Booking createBooking(String currency, long invoiceRecipientId, double amount, long taxRate) {
		Booking booking = new Booking();
		booking.setCurrency(currency);
		booking.setInvoiceRecipientPK(invoiceRecipientId);
		Price price1 = new Price (BigDecimal.valueOf(amount), currency, BigDecimal.valueOf(taxRate), true); 
		booking.setMainPrice(price1);
		return booking;
	}
	
	@Test(expected = InconsistentCurrenciesException.class)
	public void calculate_throws_InconsistentCurrenciesException_if_different_currencies_in_bookings() throws InconsistentCurrenciesException {
		Booking booking1 = createBooking("EURO", 1L, 4.50, 19L);
		Booking booking2 = createBooking("USD", 1L, 10.50, 19L);
		List<Booking> bookings = new ArrayList<>(Arrays.asList(booking1, booking2));
		
		sut.calculate(bookings, 1L);
	}

}
