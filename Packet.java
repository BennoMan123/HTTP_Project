import java.net.*;
import java.io.*;

/**
 * Packet - a superclass to create and parse datagram packets
 * Project
 * @author String teamName = null; (Members: Kelly Appleton, Michael Benno, Ethan Gapay)
 * @version 2021-05-04
 */

public class Packet implements TFTPConstants {
   // attributes used for each packet
   private InetAddress toAddress;
   private int port;
   private int opcode;
   
   /**
    * Constructor
    * @param _toAddress the destination address
    * @param _port the destination port number
    * @param _opcode the code to identify the type of packet
    */
   public Packet(InetAddress _toAddress, int _port, int _opcode) {
      toAddress = _toAddress;
      port = _port;
      opcode = _opcode;
   }
   
   /**
    * Default Constructor
    */
   public Packet() {}
   
   // Accessor methods
   /**
    * getAddress
    * @return toAddress
    */
   public InetAddress getAddress() {
      return toAddress;
   }
   
   /**
    * getPort
    * @return port
    */
   public int getPort() {
      return port;
   }
   
   /**
    * getOpcode
    * @return opcode
    */
   public int getOpcode() {
      return opcode;
   }
   
   // Mutator methods
   /**
    * setAddress
    * @param _toAddress the IP address to set the InetAddress to
    */
   public void setAddress(InetAddress _toAddress) {
      toAddress = _toAddress;
   }
   
   /**
    * setPort
    * @param _port the number to set the port to
    */
   public void setPort(int _port) {
      port = _port;
   }
   
   /**
    * setOpcode
    * @param _opcode the opcode to set
    */
   public void setOpcode(int _opcode) {
      opcode = _opcode;
   }
   
   /**
    * packetChecker
    * @param packet - the datagram packet to check
    */
   public void packetChecker(DatagramPacket packet) {
      // sets up the input streams and gets the port, address, and opcode
      ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
      DataInputStream dis = new DataInputStream(bais);
      
      try {
         opcode = dis.readShort();
         this.setAddress(packet.getAddress());
         this.setPort(packet.getPort());
         
         // Close
         bais.close();
         dis.close();
      }
      
      catch(Exception e) {
         System.out.println("Error in packetChecker: " + e);
      }
   } // end packetChecker
}