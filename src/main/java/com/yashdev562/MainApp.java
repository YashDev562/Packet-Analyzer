package com.yashdev562;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {
  List<PcapNetworkInterface> devList;
  PacketCaptureEngine engine;
  private ObservableList<PacketDTO> packetTableList = FXCollections.observableArrayList();
  boolean isCapturing = false;

  public MainApp() {}

  @Override
  public void init() throws Exception {
    try {
      devList = Pcaps.findAllDevs();
      engine = new PacketCaptureEngine();
    } catch(PcapNativeException e) {
      System.out.println("Error in finding network devices in init() \n");
    }
  }
  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("Packet-Sniffer");

    BorderPane root = new BorderPane();
    HBox topBar = new HBox();

    TableView<PacketDTO> packetTable = new TableView<>();

    TableColumn<PacketDTO,Long> colPacketNo = new TableColumn<>();
    colPacketNo.setText("Packet No");
    colPacketNo.setCellValueFactory(new PropertyValueFactory<>("packetNo"));

    TableColumn<PacketDTO,Long> colTimeStamp = new TableColumn<>();
    colTimeStamp.setText("TimeStamp");
    colTimeStamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

    TableColumn<PacketDTO,Integer> colLength = new TableColumn<>();
    colLength.setText("Byte Length");
    colLength.setCellValueFactory(new PropertyValueFactory<>("length"));

    TableColumn<PacketDTO,String> colSrcMac = new TableColumn<>();
    colSrcMac.setText("Source MAC Address");
    colSrcMac.setCellValueFactory(new PropertyValueFactory<>("srcMac"));

    TableColumn<PacketDTO,String> colDstMac= new TableColumn<>();
    colDstMac.setText("Destination MAC Address");
    colDstMac.setCellValueFactory(new PropertyValueFactory<>("dstMac"));

    TableColumn<PacketDTO,String> colSrcIP = new TableColumn<>();
    colSrcIP.setText("Source IP Address");
    colSrcIP.setCellValueFactory(new PropertyValueFactory<>("srcIp"));

    TableColumn<PacketDTO,String> colDstIP = new TableColumn<>();
    colDstIP.setText("Destination IP Address");
    colDstIP.setCellValueFactory(new PropertyValueFactory<>("dstIp"));

    TableColumn<PacketDTO,String> colProtocol = new TableColumn<>();
    colProtocol.setText("Protocol");
    colProtocol.setCellValueFactory(new PropertyValueFactory<>("protocol"));

    TableColumn<PacketDTO,Integer> colSrcPort = new TableColumn<>();
    colSrcPort.setText("Source Port");
    colSrcPort.setCellValueFactory(new PropertyValueFactory<>("srcPort"));

    TableColumn<PacketDTO,Integer> colDstPort = new TableColumn<>();
    colDstPort.setText("Destination Port");
    colDstPort.setCellValueFactory(new PropertyValueFactory<>("dstPort"));

    packetTable.getColumns().addAll(Arrays.asList(colPacketNo,colTimeStamp,colLength,colSrcMac,colDstMac,colSrcIP,
                                    colDstIP,colProtocol,colSrcPort,colDstPort));

    packetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    packetTable.setItems(packetTableList);

    packetTable.setRowFactory(tv -> new TableRow<PacketDTO>() {
    @Override
    protected void updateItem(PacketDTO item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setStyle(""); // Reset empty rows
        } else {
            String proto = item.getProtocol();
            if (proto == null) proto = ""; // Prevent NullPointerException
            
            // Apply Wireshark-style protocol colors
            if ("TCP".equalsIgnoreCase(proto)) {
                setStyle("-fx-background-color: #e4e6f4;"); // Light Purple/Blue
            } else if ("UDP".equalsIgnoreCase(proto)) {
                setStyle("-fx-background-color: #daeef6;"); // Light Cyan
            } else if ("ICMP".equalsIgnoreCase(proto) || "ICMPv4".equalsIgnoreCase(proto)) {
                setStyle("-fx-background-color: #fce0ff;"); // Light Pink
            } else if ("IGMP".equalsIgnoreCase(proto)) {
                setStyle("-fx-background-color: #ffebd6;"); // Light Orange
            } else if ("ARP".equalsIgnoreCase(proto)) {
                setStyle("-fx-background-color: #d6e8e5;"); // Light Greenish Grey
            } else {
                setStyle(""); // Default background for others
            }
         }
      }
    });

    HBox statusBar = new HBox();

    Timeline timer = new Timeline(new KeyFrame(Duration.millis(5), event -> {
      Queue<PacketDTO> queue = engine.getQueue();
      while (!queue.isEmpty()) {
          PacketDTO dto = queue.poll();
          System.out.println("UI received packet: " + dto.getSrcIp());
          if (dto != null) {
              packetTableList.add(dto);
          }
        }
    }));
    timer.setCycleCount(Timeline.INDEFINITE);

    root.setTop(topBar);
    root.setCenter(packetTable);
    root.setBottom(statusBar);

    ComboBox<String> deviceDropDownList = new ComboBox<>();
    deviceDropDownList.setPromptText("Select a device");
    deviceDropDownList.getItems().addAll(getNetDevNames());
    Button startButton = new Button("Start Observation");
    Button stopButton = new Button("Stop Observations");
    HBox.setHgrow(deviceDropDownList, Priority.ALWAYS);
    HBox.setHgrow(startButton, Priority.SOMETIMES);
    HBox.setHgrow(stopButton, Priority.SOMETIMES);

    deviceDropDownList.setMaxWidth(Double.MAX_VALUE);
    startButton.setMaxWidth(Double.MAX_VALUE);
    stopButton.setMaxWidth(Double.MAX_VALUE);

    topBar.getChildren().addAll(deviceDropDownList,startButton,stopButton);
    Label captureStatus = new Label("Capture Status : Not Capturing");
    statusBar.getChildren().addAll(captureStatus);

    startButton.setOnAction(event -> {
      captureStatus.setText("Capture Status : Capturing");
      packetTableList.clear();
      engine.clearQueue();
      PacketDTO.resetPacketNo();
      String devName = deviceDropDownList.getValue();
      PcapNetworkInterface dev = devList.stream()
                               .filter(device -> device.getDescription() != null && device.getDescription().equals(devName))
                               .findFirst()
                               .orElse(null);
      engine.startEngine(dev);
      isCapturing = true;
      timer.play();
    });

    stopButton.setOnAction(event -> {
      captureStatus.setText("Capture Status : Not Capturing");
      engine.stopEngine();
    });
    

    Scene newScene = new Scene(root,900,600);
    primaryStage.setScene(newScene);

    primaryStage.show();
  }

  public List<String> getNetDevNames() {
      List<String> devNames = new ArrayList<>();
      for(PcapNetworkInterface dev : devList) {
        if(dev.getDescription() != null) {
          devNames.add(dev.getDescription());
        }
      }
      return devNames;
  }

}
