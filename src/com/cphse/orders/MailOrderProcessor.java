package com.cphse.orders;

import com.cphse.queue.OrderProcessor;
import com.cphse.queue.OrderType;
import com.cphse.queue.Task;
import com.cphse.tasks.MailAccountTask;

public class MailOrderProcessor extends OrderProcessor<MailAccountOrder>{

	public MailOrderProcessor(int mininumSize) {
		super(OrderType.TEST, mininumSize);
	}

	@Override
	protected Task<MailAccountOrder> createTask(MailAccountOrder order) {
		return new MailAccountTask(order);
	}

}
