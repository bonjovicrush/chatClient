package com.pyj.chatClient0213.serverIO.dto;

import java.text.DateFormat;

import java.text.SimpleDateFormat;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @brief 서버에서 각 사용자에 대한 접속정보를 저장하기 위한 클래스
 * @author Park
 *
 */
public class ClientInfo {
	
	private SimpleStringProperty id;	
	private SimpleStringProperty ipAddress;	
	private SimpleStringProperty port;
	private SimpleStringProperty accessTime;
	private SimpleStringProperty connectState;
	
	ClientInfo(){
		this.id = new SimpleStringProperty();
		this.ipAddress = new SimpleStringProperty();
		this.port = new SimpleStringProperty();
		this.accessTime = new SimpleStringProperty();
		this.connectState = new SimpleStringProperty();
	}
	public ClientInfo(String id, String ipAddress, String port, String accessTime, String connectState){
		this.id = new SimpleStringProperty(id);
		this.ipAddress = new SimpleStringProperty(ipAddress);
		this.port = new SimpleStringProperty(port);
		this.accessTime = new SimpleStringProperty(accessTime);
		this.connectState = new SimpleStringProperty(connectState);
	}
	public final SimpleStringProperty idProperty() {
		return this.id;
	}
	
	public final String getId() {
		return this.idProperty().get();
	}
	
	public final void setId(final String id) {
		this.idProperty().set(id);
	}
	
	public final SimpleStringProperty ipAddressProperty() {
		return this.ipAddress;
	}
	
	public final String getIpAddress() {
		return this.ipAddressProperty().get();
	}
	
	public final void setIpAddress(final String ipAddress) {
		this.ipAddressProperty().set(ipAddress);
	}
	
	public final SimpleStringProperty portProperty() {
		return this.port;
	}
	
	public final String getPort() {
		return this.portProperty().get();
	}
	
	public final void setPort(final String port) {
		this.portProperty().set(port);
	}
	
	public final SimpleStringProperty accessTimeProperty() {
		return this.accessTime;
	}
	
	public final String getAccessTime() {
		return this.accessTimeProperty().get();
	}
	
	public final void setAccessTime(final String accessTime) {
		this.accessTimeProperty().set(accessTime);
	}
	
	public final SimpleStringProperty connectStateProperty() {
		return this.connectState;
	}
	
	public final String getConnectState() {
		return this.connectStateProperty().get();
	}
	
	public final void setConnectState(final String connectState) {
		this.connectStateProperty().set(connectState);
	}
	

	
	
}
