package com.bigstock.sharedComponent.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class SeqGenerator {

	public static String createUserAccountId() {
		LocalDateTime now = LocalDateTime.now();
		String partUuid = UUID.randomUUID().toString().substring(0, 5);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

		StringBuilder sb = new StringBuilder();
		String id = sb.append(now.format(formatter)).append(partUuid).toString();
		return id;
	}
	
}
