package com.bigstock.sharedComponent.error;

public class BizException extends RuntimeException{

	private int code;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1402400751299716502L;
	
	public BizException(ErrorCode errorCode) {
		super(errorCode.getMsg());
		this.code = errorCode.getCode();
	}

	public int getCode() {
		return code;
	}

}
