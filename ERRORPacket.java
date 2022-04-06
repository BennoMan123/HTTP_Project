import java.net.*;
import java.io.*;

/**
 * ERRORPacket - a class to create and parse error datagram packets
 * Project
 * @author String teamName = null; (Members: Kelly Appleton, Michael Benno, Ethan Gapay)
 * @version 2021-05-04
 */

public class ERRORPacket extends Packet implements TFTPConstants {
   // attributes
   private int errorNo;
   private String errorMsg;
   
   /**
    * Constructor
    * @param _toAddress the destination address
    * @param _port the destination port number
    * @param _errorNo the error number to define the error type
    * @param _errorMsg the error message
    */
   public ERRORPacket (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg) {
      super(_toAddress, _port, ERROR);
      errorNo = _errorNo;
      errorMsg = _errorMsg;
   }
   
   /**
    * Default Constructor
    */
   public ERRORPacket() {}
   
   // Accessor methods
   /**
    * getErrorMsg
    * @return errorMsg
    */
   public String getErrorMsg() {
      return errorMsg;
   }
   
   /**
    * getErrorNo
    * @return errorNo
    */
   public int getErrorNo() {
      return errorNo;
   }
   
   /**
    * build
    * @return errPkt the error datagram packet
    */
   public DatagramPacket build() {
      // creates the output array length
      ByteArrayOutputStream baos = new ByteArrayOutputStream(
         2 /*opcode*/ + 2/*Ecode*/ + errorMsg.length() + 1 /*0*/);
      
      // sets up the data output stream and then writes all the necessary info
      DataOutputStream dos = null;
      try {
         dos = new DataOutputStream(baos);
         dos.writeShort(ERROR);
         dos.writeShort(errorNo);
         dos.writeBytes(errorMsg);
         dos.writeByte(0);
         
         // Close
         dos.close();
      }
      catch (Exception e) {
         System.out.println(e);
      }
      
      byte[] holder = baos.toByteArray();   // Get the underlying byte[]
      DatagramPacket errPkt = new DatagramPacket(holder, holder.length, super.getAddress(), super.getPort());  // Build a DatagramPacket from the byte[]
      return errPkt; // returns the packet
   } // end build
   
   /**
    * dissect
    * @param errPkt the error datagram packet
    */
   public void dissect(DatagramPacket errPkt) {
      // Create a ByteArrayInputStream from the payload
      // NOTE: give the packet data, offset, and length to ByteArrayInputStream
      ByteArrayInputStream bais =
         new ByteArrayInputStream(errPkt.getData(), errPkt.getOffset(), errPkt.getLength());
   
      DataInputStream dis = new DataInputStream(bais);
      
      // reads the information
      try {
         dis.readShort();
         errorNo = dis.readShort();
         errorMsg = readToZ(dis);
         
         // Close
         dis.close();
      }
      catch(Exception e) {
         System.out.println(e);
      }
   } // end dissect
   
   
   /**
    * readToZ - utility method for reading packet error message
    * @param dis the DataInputStream
    */
   public static String readToZ(DataInputStream dis) {
      String value = "";
      while (true) {
         try {
            byte b = dis.readByte();
            if (b == 0)
               return value;
            value += (char) b;
         }
         catch (Exception e) {
            System.out.println(e);
         }
      }
   } // end readToZ
}