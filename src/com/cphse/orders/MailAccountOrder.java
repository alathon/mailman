package com.cphse.orders;

import java.util.concurrent.TimeUnit;

import com.cphse.mailman.connection.MailConnectionDetails;
import com.cphse.queue.OrderType;
import com.cphse.time.TimeDelay;

public class MailAccountOrder extends com.cphse.queue.Order {
	private static final long serialVersionUID = -1615873465424618527L;
	public final MailConnectionDetails details;

	public MailAccountOrder(MailConnectionDetails details) {
		this.details = details;
	}

	@Override
	public TimeDelay getNextExecutionDelay() {
		return new TimeDelay(0, TimeUnit.SECONDS);
	}

	@Override
	public OrderType getOrderType() {
		return OrderType.TEST;
	}

}
