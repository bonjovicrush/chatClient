package com.pyj.chatServer0213.serverIO.event;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pyj.chatClient0213.serverIO.dao.DBManager;
import com.pyj.chatClient0213.serverIO.dto.ClientInfo;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainHandler implements Initializable {
	@FXML
	private Button btnStartStop; // ���� ����, ����
	@FXML
	private TextArea txtDisplay; // Ŭ���̾�Ʈ ����
	@FXML
	private TableView<ClientInfo> tableView;
	@FXML
	private Button btnRefresh;

	MainHandler mainHandler;

	ExecutorService executorService; // ������ Ǯ ����
	ServerSocket serverSocket; // ���� ����

	ObservableList connectionList = FXCollections.observableArrayList();
	DBManager dbm = new DBManager();

	List<Client> connections = new Vector<Client>(); // ������ �����ϴ� Ŭ���̾�Ʈ���� ����

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		dbm.connectDB();
		TableColumn tcID = tableView.getColumns().get(0);
		tcID.setCellValueFactory(new PropertyValueFactory("id"));
		tcID.setStyle("-fx-alignment:CENTER;");

		TableColumn tcIPAddress = tableView.getColumns().get(1);
		tcIPAddress.setCellValueFactory(new PropertyValueFactory("ipAddress"));
		tcIPAddress.setStyle("-fx-alignment:CENTER;");

		TableColumn tcPort = tableView.getColumns().get(2);
		tcPort.setCellValueFactory(new PropertyValueFactory("port"));
		tcPort.setStyle("-fx-alignment:CENTER;");

		TableColumn tcAccessTime = tableView.getColumns().get(3);
		tcAccessTime.setCellValueFactory(new PropertyValueFactory("accessTime"));
		tcAccessTime.setStyle("-fx-alignment:CENTER;");

		TableColumn tcConnState = tableView.getColumns().get(4);
		tcConnState.setCellValueFactory(new PropertyValueFactory("connectState"));
		tcConnState.setStyle("-fx-alignment:CENTER;");

		dbm.getUserInfo(connectionList);

		tableView.setItems(connectionList);
	}

	public ObservableList getConnectionList() {

		return connectionList;
	}

	public MainHandler getMainHandler() {
		return mainHandler;
	}

	public void setMainHandler(MainHandler mainHandler) {
		this.mainHandler = mainHandler;
	}

	@FXML
	void onRefresh(ActionEvent event) {
		connectionList.clear();
		dbm.getUserInfo(connectionList);
		tableView.setItems(connectionList);
	}

	/**
	 * @brief �������� �� ����
	 * @param event
	 */
	@FXML
	void onConnect(ActionEvent event) {
		if (btnStartStop.getText().equals("����")) {
			startServer();
		} else if (btnStartStop.getText().equals("����")) {
			stopServer();
		}
	}

	/**
	 * @brief ���� ���� ����
	 */
	void startServer() {
		// executorService =
		// Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		executorService = Executors.newFixedThreadPool(20);
		// System.out.println(Runtime.getRuntime().availableProcessors());
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("localhost", 5001));
		} catch (Exception e) {
			if (!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					displayText("[���� ����]");
					btnStartStop.setText("����");
				});
				while (true) {
					try {
						Socket socket = serverSocket.accept();
						String message = "[���� ����: " + socket.getRemoteSocketAddress() + ":"
								+ Thread.currentThread().getName() + "]";
						Platform.runLater(() -> displayText(message));

						Client client = new Client(socket);
						connections.add(client);
						Platform.runLater(() -> displayText("[���� ����: " + connections.size() + "]"));
					} catch (Exception e) {
						if (!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		executorService.submit(runnable);
	}

	/**
	 * @brief ���� ����
	 */
	public void stopServer() {
		try {
			Iterator<Client> iterator = connections.iterator();
			// ������ ����Ǹ� ������ �������� ���� Ŭ���̾�Ʈ ���ϵ��� ���� ����
			while (iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			if (executorService != null && !executorService.isShutdown()) {
				executorService.shutdown();
			}
			Platform.runLater(() -> {
				displayText("[���� ����]");
				btnStartStop.setText("����");
			});
		} catch (Exception e) {
		}
	}

	/**
	 * @brief ������ ������ Ŭ���̾�Ʈ���� ������ ���
	 * @param text
	 */
	void displayText(String text) {
		txtDisplay.appendText(text + "\n");
	}

	/**
	 * @brief ������ �����ϴ� Ŭ���̾�Ʈ ����
	 * @author Park
	 *
	 */
	public class Client {
		Socket socket; // ������ �����ϴ� Ŭ���̾�Ʈ ����
		private String id;
		private String ipAddress;
		private String port;
		private String accessTime;
		private boolean connectState = false;

		// String password;
		Client(Socket socket) {
			this.socket = socket;
			this.ipAddress = socket.getInetAddress().toString();
			this.port = String.valueOf(socket.getPort());
			SimpleDateFormat sdf = new SimpleDateFormat("YYYY�� MM�� dd�� HH:mm:ss");
			this.accessTime = sdf.format(new Date());
			this.connectState = true;

			receive();
			// dbm.getInst().updateUserInfo(this.id, this.ipAddress, this.port,
			// this.accessTime, this.connectState);
		}

		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}

		public String getAccessTime() {
			return accessTime;
		}

		public void setAccessTime(String accessTime) {
			this.accessTime = accessTime;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getIpAddress() {
			return ipAddress;
		}

		public void setIpAddress(String ipAddress) {
			this.ipAddress = ipAddress;
		}

		public String getLatestAccessTime() {
			return accessTime;
		}

		public void setLatestAccessTime(String latestAccessTime) {
			this.accessTime = latestAccessTime;
		}

		public boolean isConnectState() {
			return connectState;
		}

		public void setConnectState(boolean connectState) {
			this.connectState = connectState;
		}

		/**
		 * @brief Ŭ���̾�Ʈ�κ��� ������ �ޱ�
		 */
		void receive() {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						while (true) {
							byte[] byteArr = new byte[100];
							InputStream inputStream = socket.getInputStream();

							// Ŭ���̾�Ʈ�� ������ ���Ḧ ���� ��� IOException �߻�
							int readByteCount = inputStream.read(byteArr); // ������
																			// �ޱ�

							// Ŭ���̾�Ʈ�� ���������� socket�� close()�� ȣ�� ���� ���
							if (readByteCount == -1) {
								throw new IOException();
							}
							String message = "[��û ó��: " + socket.getRemoteSocketAddress() + ": "
									+ Thread.currentThread().getName() + "]";
							Platform.runLater(() -> displayText(message));

							String data = new String(byteArr, 0, readByteCount, "UTF-8");
							// String receiveID = getId(); // ���� ����� id�� ����

							for (Client client : connections) {
								if (new String(byteArr, 0, readByteCount, "UTF-8").contains("#id")) {
									Platform.runLater(() -> {
										try {
											displayText(getConnState(byteArr, readByteCount));
											client.send(getConnState(byteArr, readByteCount));
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										dbm.getInst().updateUserInfo(client.id, client.ipAddress, client.port,
												client.accessTime, client.connectState);
									});
								} 
								else if(new String(byteArr,0,readByteCount, "UTF-8").contains("#message")){
									receiveMessage(byteArr, readByteCount);
								}
								else {
									client.send(getId() + ": " + data);
								}
							}
						}
					} catch (IOException e) {
						disConnClient();
					}
				}
			};
			executorService.submit(runnable);
		}

		/**
		 * @brief Ŭ���̾�Ʈ���� ������ ������
		 * @param data
		 */
		void send(String data) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						byte[] byteArr = data.getBytes("UTF-8");
						OutputStream outputStream = socket.getOutputStream();
						outputStream.write(byteArr);
						outputStream.flush();
					} catch (Exception e) {
						try {
							String message = "[Ŭ���̾�Ʈ ��� �ȵ�: " + socket.getRemoteSocketAddress() + ": "
									+ Thread.currentThread().getName() + "]";
							Platform.runLater(() -> displayText(message));
							connections.remove(Client.this);
							socket.close();
						} catch (IOException e2) {

						}
					}
				}
			};
			executorService.submit(runnable);
		}

		String getConnState(byte[] byteArr, int readByteCount) throws Exception {
			String resultString = null;
			if (new String(byteArr, 0, readByteCount, "UTF-8").contains("#id")) {
				String st = new String(byteArr, "#id".length(), readByteCount, "UTF-8");
				st = st.substring(0, readByteCount - "#id".length());
				resultString = st + " ���� �����ϼ̽��ϴ�.";
				this.setId(st);
			}

			return resultString;
		}

		void disConnClient() {
			try {
				connections.remove(Client.this);

				System.out.println("Ŭ���̾�Ʈ ����");
				DBManager.getInst().updateUserInfo(id, accessTime, connectState);
				
				for (Client client : connections) {
					client.send(id + "���� ������ �����Ͽ����ϴ�.");
				}
				
				String message = "[Ŭ���̾�Ʈ ��� �ȵ�: " + socket.getRemoteSocketAddress() + ": "
						+ Thread.currentThread().getName() + "]";
				Platform.runLater(() -> {
					displayText(id + "���� ������ �����Ͽ����ϴ�.");
					displayText(message);
				});
				socket.close();

			} catch (IOException e1) {
			
			}
		}
		
		void receiveMessage(byte[] byteArr, int readByteCount){
			String toID;
			String data;
			try {
				String str = new String(byteArr,0,readByteCount,"UTF-8");
				//StringTokenizer st2 = new StringTokenizer(str, "#message");
				str = str.substring(8, str.length());

				
				//String str = new String(byteArr, "#message".length(), readByteCount, "UTF-8");
				str = str.substring(3, str.length());	//id + #data + ����
				StringTokenizer st = new StringTokenizer(str);
				toID = st.nextToken("#");
				String str2 = st.nextToken();
				data = str2.substring(3, str2.length());
				
				System.out.println("ReceiveMessage"+toID+" : "+data);
				for (Client client : connections) {
					if(client.id.equals(toID)){
						client.send("#message#from" + Client.this.id + "#data"+data);
						System.out.println("ReceiveMessage [send]:"+"#message#from" + Client.this.id + "#data"+data);
					}
				}

				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

}
