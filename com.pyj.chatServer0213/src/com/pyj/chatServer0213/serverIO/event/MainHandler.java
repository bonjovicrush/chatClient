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
	private Button btnStartStop; // 서버 시작, 종료
	@FXML
	private TextArea txtDisplay; // 클라이언트 정보
	@FXML
	private TableView<ClientInfo> tableView;
	@FXML
	private Button btnRefresh;

	MainHandler mainHandler;

	ExecutorService executorService; // 쓰레드 풀 제어
	ServerSocket serverSocket; // 서버 소켓

	ObservableList connectionList = FXCollections.observableArrayList();
	DBManager dbm = new DBManager();

	List<Client> connections = new Vector<Client>(); // 서버에 접속하는 클라이언트들을 관리

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
	 * @brief 서버시작 및 종료
	 * @param event
	 */
	@FXML
	void onConnect(ActionEvent event) {
		if (btnStartStop.getText().equals("시작")) {
			startServer();
		} else if (btnStartStop.getText().equals("종료")) {
			stopServer();
		}
	}

	/**
	 * @brief 소켓 서버 시작
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
					displayText("[서버 시작]");
					btnStartStop.setText("종료");
				});
				while (true) {
					try {
						Socket socket = serverSocket.accept();
						String message = "[연결 수락: " + socket.getRemoteSocketAddress() + ":"
								+ Thread.currentThread().getName() + "]";
						Platform.runLater(() -> displayText(message));

						Client client = new Client(socket);
						connections.add(client);
						Platform.runLater(() -> displayText("[연결 개수: " + connections.size() + "]"));
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
	 * @brief 서버 종료
	 */
	public void stopServer() {
		try {
			Iterator<Client> iterator = connections.iterator();
			// 서버가 종료되면 연결이 끊어지지 않은 클라이언트 소켓들을 강제 종료
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
				displayText("[서버 멈춤]");
				btnStartStop.setText("시작");
			});
		} catch (Exception e) {
		}
	}

	/**
	 * @brief 서버에 접속한 클라이언트들의 정보를 출력
	 * @param text
	 */
	void displayText(String text) {
		txtDisplay.appendText(text + "\n");
	}

	/**
	 * @brief 서버에 접속하는 클라이언트 정보
	 * @author Park
	 *
	 */
	public class Client {
		Socket socket; // 서버에 접속하는 클라이언트 소켓
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
			SimpleDateFormat sdf = new SimpleDateFormat("YYYY년 MM월 dd일 HH:mm:ss");
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
		 * @brief 클라이언트로부터 데이터 받기
		 */
		void receive() {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						while (true) {
							byte[] byteArr = new byte[100];
							InputStream inputStream = socket.getInputStream();

							// 클라이언트가 비정상 종료를 했을 경우 IOException 발생
							int readByteCount = inputStream.read(byteArr); // 데이터
																			// 받기

							// 클라이언트가 정상적으로 socket의 close()를 호출 했을 경우
							if (readByteCount == -1) {
								throw new IOException();
							}
							String message = "[요청 처리: " + socket.getRemoteSocketAddress() + ": "
									+ Thread.currentThread().getName() + "]";
							Platform.runLater(() -> displayText(message));

							String data = new String(byteArr, 0, readByteCount, "UTF-8");
							// String receiveID = getId(); // 보낸 사람의 id를 저장

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
		 * @brief 클라이언트에게 데이터 보내기
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
							String message = "[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": "
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
				resultString = st + " 님이 접속하셨습니다.";
				this.setId(st);
			}

			return resultString;
		}

		void disConnClient() {
			try {
				connections.remove(Client.this);

				System.out.println("클라이언트 종료");
				DBManager.getInst().updateUserInfo(id, accessTime, connectState);
				
				for (Client client : connections) {
					client.send(id + "님이 접속을 종료하였습니다.");
				}
				
				String message = "[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": "
						+ Thread.currentThread().getName() + "]";
				Platform.runLater(() -> {
					displayText(id + "님이 접속을 종료하였습니다.");
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
				str = str.substring(3, str.length());	//id + #data + 내용
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
