# Packet-Analyzer
This program analyzes network packets that are sent and received from the different NIC devices (WiFi, Ethernet) and extracts different info about the packet like length, source and destination IP Addresses, MAC Addresses, Ports etc.

Maven is required to install the different dependencies, mainly Pcap (for interacting with the NIC's) and JavaFX (building the application and UI to display the extracted packet info). Before using this project, make sure you have the Maven and JDK extensions installed in VS Code (or suitable alternatives if you are using other IDE's like IntelliJ, Cursor or Antigravity).

Here are the steps to properly utilise the app with Maven : 

1) Clone the repo into your local workspace.

2) A notification will pop up on the bottom right, once you open the repo, stating that Maven is loading the required dependencies (this will take place only if the Maven extension is installed).

3) Once the dependencies are installed, navigate to the src/main/java/com/yashdev562 folder.

4) Once you reach the yashdev562 folder, run the App.java (not the MainApp.java) file and the program will execute.

Future Scope of the Project:

1) Full analysis of the packet being monitored.
2) AI assisted interpretation of the packet (if possible).
