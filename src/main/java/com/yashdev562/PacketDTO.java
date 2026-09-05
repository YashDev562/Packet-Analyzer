package com.yashdev562;

public class PacketDTO {
    private static long counter = 1;
    private final long packetNo;
    private final long timestamp;
    private final int length;
    private final String srcMac;
    private final String dstMac;
    private final String srcIp;
    private final String dstIp;
    private final String protocol;
    private final int srcPort;
    private final int dstPort;

    public PacketDTO(long timestamp, int length, String srcMac, String dstMac, 
                     String srcIp, String dstIp, String protocol, int srcPort, int dstPort) {
        this.packetNo = counter++;
        this.timestamp = timestamp;
        this.length = length;
        this.srcMac = srcMac;
        this.dstMac = dstMac;
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.protocol = protocol;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
    }

    public static void resetPacketNo() {counter = 1;}

    // Getters
    
    public long getTimestamp() { return timestamp; }
    public int getLength() { return length; }
    public String getSrcMac() { return srcMac; }
    public String getDstMac() { return dstMac; }
    public String getSrcIp() { return srcIp; }
    public String getDstIp() { return dstIp; }
    public String getProtocol() { return protocol; }
    public int getSrcPort() { return srcPort; }
    public int getDstPort() { return dstPort; }
    public long getPacketNo() {return packetNo;}

    public static long getCounter() {
      return counter;
    }

    public static void setCounter(long counter) {
      PacketDTO.counter = counter;
    }

    
}
