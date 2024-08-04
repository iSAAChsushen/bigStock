package com.bigstock.sharedComponent.infra;

import com.p6spy.engine.spy.P6DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class CustomHikariDataSource implements DataSource {

	private static HikariDataSource hikariDataSource;
	
    public CustomHikariDataSource(HikariConfig config) {
         hikariDataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
       return new P6DataSource(hikariDataSource).getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        // 在这里添加自定义的连接逻辑
        System.out.println("Custom getConnection with credentials called");
        return new P6DataSource(hikariDataSource).getConnection();
    }

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return hikariDataSource.getParentLogger();
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return hikariDataSource.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		hikariDataSource.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return hikariDataSource.getLoginTimeout();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return hikariDataSource.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return hikariDataSource.isWrapperFor(iface);
	}
}
