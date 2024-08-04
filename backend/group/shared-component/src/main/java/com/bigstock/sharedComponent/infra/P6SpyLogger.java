package com.bigstock.sharedComponent.infra;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.p6spy.engine.common.P6Util;
import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import com.p6spy.engine.spy.appender.P6Logger;

public class P6SpyLogger implements MessageFormattingStrategy, P6Logger{

	 private static final Log logger = LogFactory.getLog(P6SpyLogger.class);

	    /**
	     * 訊息Fomate，但我們這裡另外記錄我們的QuerySqlRecord紀錄
	     */
	    @Override
	    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
	        return !"".equals(sql.trim()) ? P6Util.singleLine(sql) + ";" : "";
	    }

	    @Override
	    public void logSQL(int connectionId, String now, long elapsed, Category category, String prepared, String sql, String url) {
	                logText(sql);
	    }

	    @Override
	    public void logException(Exception e) {
	        // TODO Auto-generated method stub
	        logger.debug(e);
	    }

	    /**
	     * 實際上要印出的SQL(有綁定執行參數)
	     * 這裡能動態修改，因為每次執行前會讀取sqlLogLevel(印出等級)，sqlLogLevel只有在INFO、WARN、ERROR等級才會印出
	     */
	    @Override
	    public void logText(String text) {
	        logger.info(text);
	    }

	    /**
	     * 必須為true，不然BacchusP6SpyLogger不會被啟用
	     */
	    @Override
	    public boolean isCategoryEnabled(Category category) {
	        return true;
	    }

}
