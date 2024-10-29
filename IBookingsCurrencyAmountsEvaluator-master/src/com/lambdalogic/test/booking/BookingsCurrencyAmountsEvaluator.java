package com.lambdalogic.test.booking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.lambdalogic.test.booking.exception.InconsistentCurrenciesException;
import com.lambdalogic.test.booking.model.Booking;
import com.lambdalogic.test.booking.model.CurrencyAmount;

public class BookingsCurrencyAmountsEvaluator implements IBookingsCurrencyAmountsEvaluator {
	
	private static final int NUMBER_OF_DECIMAL_PLACES = 2;
	
	private CurrencyAmount totalAmount;
	private CurrencyAmount totalPaidAmount;
	private CurrencyAmount totalOpenAmount;

	public BookingsCurrencyAmountsEvaluator() {
	}
	
	@Override
	public void calculate(List<Booking> bookingList, Long invoiceRecipientID) throws InconsistentCurrenciesException {
		if (bookingList == null || bookingList.isEmpty()) {
			throw new IllegalArgumentException("Argument 'bookingList' should be given.");
		}
		checkIfAnyTwoCurrenciesAreDifferent(bookingList);
		
		List<Booking> relevantBookings = filterRelevantBooks(bookingList, invoiceRecipientID);
		if (relevantBookings.isEmpty()) {
			return; // Nothing to calculate so we fail fast
		}
		
		String currency = relevantBookings.get(0).getCurrency();
		initTotalAmounts(currency);
		for (Booking booking : relevantBookings) {
			totalAmount = totalAmount.add(booking.getTotalAmountGross());
			totalPaidAmount = totalPaidAmount.add(booking.getPaidAmount());
			totalOpenAmount = totalOpenAmount.add(booking.getOpenAmount());
		}
		totalAmount.getAmount().setScale(NUMBER_OF_DECIMAL_PLACES, RoundingMode.HALF_UP);
		totalPaidAmount.getAmount().setScale(NUMBER_OF_DECIMAL_PLACES, RoundingMode.HALF_UP);
		totalOpenAmount.getAmount().setScale(NUMBER_OF_DECIMAL_PLACES, RoundingMode.HALF_UP);
	}

	private void initTotalAmounts(String currency) {
		totalAmount = new CurrencyAmount(BigDecimal.ZERO, currency);
		totalPaidAmount = new CurrencyAmount(BigDecimal.ZERO, currency);
		totalOpenAmount = new CurrencyAmount(BigDecimal.ZERO, currency);
	}
	
	private List<Booking> filterRelevantBooks(List<Booking> bookingList, Long invoiceRecipientID) {
		List<Booking> resultList = new ArrayList<>();
		for (Booking booking: bookingList) {
			if (isBookingBelongsToGivenInvoiceRecipient(invoiceRecipientID, booking) && !isAmountAndPaidAmountZero(booking)) {
				resultList.add(booking);
			}
		}
		return resultList;
	}

	private boolean isAmountAndPaidAmountZero(Booking booking) {
		boolean value = false;
		if (booking.getCurrencyAmount() != null && booking.getOpenAmount() != null) {
			value = BigDecimal.ZERO.equals(booking.getCurrencyAmount().getAmount());
			value = value && BigDecimal.ZERO.equals(booking.getOpenAmount());
		}
		return value;
	}

	private boolean isBookingBelongsToGivenInvoiceRecipient(Long invoiceRecipientID, Booking booking) {
		return booking.getInvoiceRecipientPK().equals(invoiceRecipientID);
	}

	private void checkIfAnyTwoCurrenciesAreDifferent(List<Booking> bookings) throws InconsistentCurrenciesException {
		String oldCurrency = bookings.get(0).getCurrency();
		for (Booking booking : bookings) {
			if (!oldCurrency.equals(booking.getCurrency())) {
				throw new InconsistentCurrenciesException(oldCurrency, booking.getCurrency());
			}
		}
	}

	@Override
	public CurrencyAmount getTotalAmount() {
		return totalAmount;
	}

	@Override
	public CurrencyAmount getTotalPaidAmount() {
		return totalPaidAmount;
	}

	@Override
	public CurrencyAmount getTotalOpenAmount() {
		return totalOpenAmount;
	}

}
