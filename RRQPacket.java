import java.net.*;
import java.io.*;

/**
 * RRQPacket - a class to create and parse read request datagram packets
 * Project
 * @author String teamName = null; (Members: Kelly Appleton, Michael Benno, Ethan Gapay)
 * @version 2021-05-04
 */

public class RRQPacket extends Packet implements TFTPConstants {
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
   public RRQPacket (InetAddress _toAddress, int _port, String _fileName, String _mode) {
      super(_toAddress, _port, RRQ);
      fileName = _fileName;
      mode = _mode;
   }

   /**
    * Default Constructor
    */
   public RRQPacket() {}

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
    * Build
    * @return rrqPkt the read request datagram packet
    */
   public DatagramPacket build() {
      //Method to actually convert data which is read in binary and converts it into a packet, bytes
      //P06
      //Sets up the output streams
      ByteArrayOutputStream baos = new ByteArrayOutputStream(2 + fileName.length() + 1 + mode.length() + 1);
      DataOutputStream dos = new DataOutputStream(baos);

      try {
         //Writes to the output stream
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
      
      //Sets the byte array to the bytes in the baos
      byte[] holder = baos.toByteArray();
      //Creates a rrq datagram packet then returns it
      DatagramPacket rrqPkt = new DatagramPacket(holder, holder.length, super.getAddress(), super.getPort());
      return rrqPkt;
   } //End build
   
   /**
    * dissect
    * @param rrqPkt the read request datagram packet
    */
   public void dissect(DatagramPacket rrqPkt) {
      //Sets the address and port in packet superclass
      super.setAddress(rrqPkt.getAddress());
      super.setPort(rrqPkt.getPort());

      //Sets up input streams
      ByteArrayInputStream bais = new ByteArrayInputStream(rrqPkt.getData(), rrqPkt.getOffset(), rrqPkt.getLength());
      DataInputStream dis = new DataInputStream(bais);

      try {
         //Reads in the data
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