package com.yashdev562;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.EthernetPacket.EthernetHeader;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV4Packet.IpV4Header;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.TcpPacket.TcpHeader;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.UdpPacket.UdpHeader;


public class PacketCaptureEngine {
  PcapHandle handle;
  volatile boolean isRunning = false;
  Queue<PacketDTO> packetQueue = new ConcurrentLinkedQueue<>();

  public void startEngine(PcapNetworkInterface dev) {
    isRunning = true;
    try {
      handle = dev.openLive(65535, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 5);
      Runnable engineJob = () -> {
        PacketListener listener = (packet) -> {
            if (packet == null) return;

            int length = packet.length();
            long timeStamp = System.currentTimeMillis();

            String srcMAC = null;
            String destMAC = null;

            String srcIp = null;
            String dstIp = null;
            String protocol = null;

            int srcPort = -1;
            int dstPort = -1;

            EthernetPacket ethPacket = packet.get(EthernetPacket.class);
        
            if (ethPacket != null) {
                EthernetHeader ethHeader = ethPacket.getHeader();
                srcMAC = (ethHeader != null)?ethHeader.getSrcAddr().toString() : null;
                destMAC = (ethHeader != null)?ethHeader.getDstAddr().toString() : null;
            }

            IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
            if (ipV4Packet != null) {
                IpV4Header ipHeader = ipV4Packet.getHeader();
                srcIp = (ipHeader != null)?ipHeader.getSrcAddr().getHostAddress() : null;
                dstIp = (ipHeader != null)?ipHeader.getDstAddr().getHostAddress() : null;
                protocol = (ipHeader != null)?ipHeader.getProtocol().name() : "OTHER";
            }

            // 4. Layer 4: Ports (TCP or UDP)
            TcpPacket tcpPack = packet.get(TcpPacket.class);
            UdpPacket udpPack = packet.get(UdpPacket.class);

            if (tcpPack != null) {
                TcpHeader tcpHeader = tcpPack.getHeader();
                srcPort = (tcpHeader != null)?tcpHeader.getSrcPort().valueAsInt() : -1;
                dstPort = (tcpHeader != null)?tcpHeader.getDstPort().valueAsInt() : -1;
            } else if (udpPack != null) {
                UdpHeader udpHeader = udpPack.getHeader();
                srcPort = (udpHeader != null)?udpHeader.getSrcPort().valueAsInt() : -1;
                dstPort = (udpHeader != null)?udpHeader.getDstPort().valueAsInt() : -1;
            }

            PacketDTO dto = new PacketDTO(timeStamp, length, srcMAC, destMAC, srcIp, dstIp, protocol, srcPort, dstPort);  
            packetQueue.offer(dto);
        };

        try {
          handle.loop(-1, listener);
        } catch(Exception e) {
          System.out.printf("Capture Thread not working. Error : %s \n",e);
        }
      };

      Thread engineThread = new Thread(engineJob);
      engineThread.setDaemon(isRunning);
      engineThread.start();
    } catch(PcapNativeException e) {
        System.out.printf("Error in packet Stream. Error : %s \n",e);
    }
  }

  public void stopEngine() {
    isRunning = false;
    if (handle != null && handle.isOpen()) {
        try {
            handle.breakLoop();
            handle.close();
        } catch (NotOpenException e) {
            System.out.println("Handle already closed.");
        }
    }
  }

  public Queue<PacketDTO> getQueue() {
    return packetQueue;
  }

  public void clearQueue() {
    packetQueue.clear();
  }
}
