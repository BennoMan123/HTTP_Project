import java.net.*;
import java.io.*;

/**
 * ACKPacket - a class to create and parse acknowlegdement datagram packets
 * Project
 * @author String teamName = null; (Members: Kelly Appleton, Michael Benno, Ethan Gapay)
 * @version 2021-05-04
 */

public class ACKPacket extends Packet implements TFTPConstants {
   // attributes
   private int blockNo;
   
   /**
    * Constructor
    * @param _toAddress the destination address
    * @param _port the destination port number
    * @param _blockNo the block number of the data
    */
   public ACKPacket (InetAddress _toAddress, int _port, int _blockNo) {
      super(_toAddress, _port, ACK);
      blockNo = _blockNo;
   }
   
   /**
    * Default Constructor
    */
   public ACKPacket() {}
   
   // Accessor method
   /**
    * getBlockNo
    * @return blockNo
    */
   public int getBlockNo() {
      return blockNo;
   }
   
   /**
    * build
    * @return ackPkt the acknowledgement datagram packet
    */
   public DatagramPacket build() {
      //creates the output array length
      ByteArrayOutputStream baos = new ByteArrayOutputStream(
         2 /*opcode*/ + 2/*blockNo*/);
      
      // sets up the data output stream and then writes all the necessary info
      DataOutputStream dos = null;
      try {
         dos = new DataOutputStream(baos);
         dos.writeShort(ACK);
         dos.writeShort(blockNo);
         
         // Close
         dos.close();
      }
      catch (Exception e) {
         System.out.println(e);
      }
      
      byte[] holder = baos.toByteArray();   // Get the underlying byte[]
      DatagramPacket ackPkt = new DatagramPacket(holder, holder.length, super.getAddress(), super.getPort());  // Build a DatagramPacket from the byte[]
      return ackPkt; // returns the packet
   } // end build
   
   /**
    * dissect
    * @param ackPkt the acknowledgement datagram packet
    */
   public void dissect(DatagramPacket ackPkt) {
      // Create a ByteArrayInputStream from the payload
      // NOTE: give the packet data, offset, and length to ByteArrayInputStream
      ByteArrayInputStream bais =
         new ByteArrayInputStream(ackPkt.getData(), ackPkt.getOffset(), ackPkt.getLength());
   
      DataInputStream dis = new DataInputStream(bais);
      
      // gets opcode & block number
      try {
         super.setOpcode(dis.readShort());
         blockNo = dis.readShort();
         
         // Close
         dis.close();
      }
      catch (Exception e) {
         System.out.println(e);
      }
   } // end dissect
}