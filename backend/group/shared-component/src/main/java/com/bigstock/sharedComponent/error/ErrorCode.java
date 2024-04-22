package com.bigstock.sharedComponent.error;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCode {
	
	BIZ_CAN_NOT_FIND_STOCK_DAY_PRICE_HEIGH_OR_LOW_PRICE_OR_OPENING_PRICE_OR_CLOSE_PRICE(501001, "CAN_NOT_FIND_STOCK_DAY_PRICE_HEIGH_OR_LOW_PRICE_OR_OPENING_PRICE_OR_CLOSE_PRICE");
	
	ErrorCode(final int code, final String message) {
		this.code = code;
		this.message = message;
	}
	
	private int code;
	
	private String message;
	
	private static final Map<Integer, ErrorCode> BY_LABEL = new HashMap<>();

	static {
		for (ErrorCode element : values()) {
			BY_LABEL.put(element.getCode(), element);
		}
	}
	public static ErrorCode valueOfCode(int number) {
		return BY_LABEL.get(number);
	}

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return message;
	}
}
