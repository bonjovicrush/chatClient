package com.pyj.chatClient0213.serverIO.dao;

import java.sql.Connection;


import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.security.auth.login.LoginContext;

import com.mysql.fabric.xmlrpc.Client;
import com.pyj.chatClient0213.serverIO.dao.DBManager;
import com.pyj.chatClient0213.serverIO.dto.ClientInfo;
import com.pyj.chatServer0213.serverIO.event.MainHandler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DBManager {
	final static int ERROR_CODE_DUPLICATE_ID=1062;
	
	static ResultSet rs;
	
	private static DBManager inst;
	private static DBManager dbm = new DBManager();


	private Connection con = null;
	private Statement state = null;
	
	private String username = "root";
	private String password = "12345";
	private String dbName = "mydb";
	private String jdbcDriver = "com.mysql.jdbc.Driver";
	private String dbUrl = "jdbc:mysql://localhost:3306/" + "mydb?autoReconnect=true&useSSL=false";

	public DBManager() {
	}

	public DBManager getInstance() {
		if (inst == null) {
			inst = new DBManager();
		}
		return inst;
	}

	public static DBManager getInst() {
		return inst;
	}

	public static void setInst(DBManager inst) {
		DBManager.inst = inst;
	}

	public Connection getCon() {
		return con;
	}

	public void setCon(Connection con) {
		this.con = con;
	}

	public Statement getState() {
		return state;
	}

	public void setState(Statement state) {
		this.state = state;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public void connectDB() {
		// DB 접속
		try {
			System.out.println("이니셜라이저 시작");
			Class.forName(dbm.getInstance().getJdbcDriver()).newInstance();
			dbm.setCon(DriverManager.getConnection(dbm.getDbUrl(),
					dbm.getUsername(), dbm.getPassword()));
			if (dbm.getCon() != null) {
				System.out.println("MySQL DB 접속!");
				dbm.setState(dbm.getCon().createStatement());
				System.out.println("Statement 생성!");
			}
			System.out.println("디비 접속 종료");

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean login(String id, String password){
		String query = "select * from userinfo";
		try {
			ResultSet rs = dbm.state.executeQuery(query);
			while (rs.next()) {
				if(id.equals(rs.getString("id")) && password.equals(rs.getString("password"))){
					rs.close();
					return true;
				}
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public int addUser(String id, String password){
		String query = "insert into userinfo values('%s', '%s')";
		query = String.format(query, id, password);
		try {
			dbm.state.executeUpdate(query);
			return 1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return e.getErrorCode();
		}
	}
	
	public void getUserInfo(ObservableList<ClientInfo> connectionList){
		//ObservableList connectionList = FXCollections.observableArrayList();
		String query = "select * from userinfo";
		try {
			ResultSet rs = dbm.state.executeQuery(query);
			while(rs.next()){
				connectionList.add(new ClientInfo(rs.getString("id"), rs.getString("ipAddress"), rs.getString("port"), rs.getString("accessTime"), rs.getString("state")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void updateUserInfo(String id, String ipAddress, String port, String accessTime, boolean state){
		//String query = "update userinfo set ipAddress='1', port = '1', accessTime='1', state='1' where id='%s'";
		String query;
		if(state==true){
			query  = "update userinfo set ipAddress='%s', port='%s', accessTime='%s', state='1' where id='%s'";
		}
		else{
			query  = "update userinfo set ipAddress='%s', port='%s', accessTime='%s', state='0' where id='%s'";

		}
		//query = String.format(query, port, id,id);
			
		query = String.format(query, ipAddress, port, accessTime,id);
		
		try {
			dbm.state.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void updateUserInfo(String id, String accessTime, boolean state){
		String query = "update userinfo set accessTime='%s', state='0' where id = '%s'";
		System.out.println("userUpdate" + id);
		query = String.format(query, accessTime, id);
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY년 MM월 dd일 HH:mm:ss");
		accessTime = sdf.format(new Date());
		try {
			dbm.state.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
