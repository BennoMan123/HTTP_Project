import java.io.*;
import java.net.*;

/**
 * WRQPacket - a class to create and parse write request datagram packets
 * Project
 * @author String teamName = null; (Members: Kelly Appleton, Michael Benno, Ethan Gapay)
 * @version 2021-05-04
 */
public class WRQPacket extends Packet implements TFTPConstants {
   //Attributes
   private String fileName;
   private String mode;

   /**
    * Constructor
    * @param _toAddress the destination address
    * @param _port the destination port number
    * @param _fileName the name of the file
    * @param _mode the mode of transfer
    */
   public WRQPacket (InetAddress _toAddress, int _port, String _fileName, String _mode) {
      super(_toAddress, _port, WRQ);
      fileName = _fileName;
      mode = _mode;
   }

   /**
    * Default Constructor
    */
   public WRQPacket() {}

   //Accessor methods
   /**
    * getFileName
    * @return fileName
    */
   public String getFileName() {
      return fileName;
   }

   /**
    * getMode
    * @return mode
    */
   public String getMode() {
      return mode;
   }

   /**
    * build
    * @return wrqPkt the write request datagram packet
    */
   public DatagramPacket build() {
      //Makes the output streams
      ByteArrayOutputStream baos = new ByteArrayOutputStream(2 + fileName.length() + 1 + mode.length() + 1);
      DataOutputStream dos = new DataOutputStream(baos);
   
      try {
         //Writes to the output streams
         dos.writeShort(super.getOpcode());
         dos.writeBytes(fileName);
         dos.writeByte(0);
         dos.writeBytes(mode);
         dos.writeByte(0);
         //Close
         dos.close();
      }
      catch(Exception e) {
         System.out.println(e);
      }

      //Makes byte array from bytes of inputstream
      byte[] holder = baos.toByteArray();
      //Makes datagram packet then returns it
      DatagramPacket wrqPkt = new DatagramPacket(holder, holder.length, super.getAddress(), super.getPort());
      return wrqPkt;
   } //End build

   /**
    * dissect
    * @param wrqPkt the write request datagram packet
    */
   public void dissect(DatagramPacket wrqPkt) {
      //Calling super mutators to set the address and port
      super.setAddress(wrqPkt.getAddress());
      super.setPort(wrqPkt.getPort());

      //Creates the inputstreams
      ByteArrayInputStream bais = new ByteArrayInputStream(wrqPkt.getData(), wrqPkt.getOffset(), wrqPkt.getLength());
      DataInputStream dis = new DataInputStream(bais);

      try {
         //Sets all of the attributes and variables
         super.setOpcode(dis.readShort());
         fileName = readToZ(dis);
         mode = readToZ(dis);
         //Close
         dis.close();
      }      
      catch(Exception e) {
         System.out.println(e);
      }
   } //End dissect

   /**
    * readToZ - utility method for reading packet file name and mode
    * @param dis the DataInputStream
    */
   public static String readToZ(DataInputStream dis) {
      try {
         String value = "";
         while (true) {
            byte b = dis.readByte();
            if (b == 0)
               return value;
            value += (char) b;
         }
      }

      catch(Exception e) {
         System.out.println(e);
      }
      return "";
   } //End readToZ
}