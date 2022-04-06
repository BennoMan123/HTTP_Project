import java.net.*;
import java.io.*;

/**
 * DATAPacket - a class to create and parse data datagram packets
 * Project
 * @author String teamName = null; (Members: Kelly Appleton, Michael Benno, Ethan Gapay)
 * @version 2021-05-04
 */

public class DATAPacket extends Packet implements TFTPConstants {
   // attributes
   private int blockNo;
   private int dataLen;
   private byte[] data;
   
   /**
    * Constructor
    * @param _toAddress the destination address
    * @param _port the destination port number
    * @param _blockNo the block number of the data
    * @param _data the byte array of packet data
    * @param _dataLen the packet data length
    */
   public DATAPacket (InetAddress _toAddress, int _port, int _blockNo, byte[] _data, int _dataLen) {
      super(_toAddress, _port, DATA);
      blockNo = _blockNo;
      data = _data;
      dataLen = _dataLen;
   }
   
   /**
    * Default Constructor
    */
   public DATAPacket() {}
   
   // Accessor methods 
   /**
    * getBlockNo
    * @return blockNo
    */
   public int getBlockNo() {
      return blockNo;
   }

   /**
    * getData
    * @return data
    */
   public byte[] getData() {
      return data;
   }
   
   /**
    * getDataLen
    * @return dataLen
    */
   public int getDataLen() {
      return dataLen;
   }

   /**
    * build
    * @return dataPkt the data datagram packet
    */
   public DatagramPacket build() {
      //creates the output array length
      ByteArrayOutputStream baos = new ByteArrayOutputStream(2 /*opcode*/ + 2/*blockNo*/ + dataLen/*bytes 0-512*/);
      
      //sets up the data output stream and then writes all the necessary info
      DataOutputStream dos = null;
      try {
         dos = new DataOutputStream(baos);
         dos.writeShort(DATA);
         dos.writeShort(blockNo);
         dos.write(data, 0, dataLen);
      
         // Close
         dos.close();
      }
      catch(Exception e) {
         System.out.println(e);
      }
      
      byte[] holder = baos.toByteArray();   // Get the underlying byte[]
      DatagramPacket dataPkt = new DatagramPacket(holder, holder.length, super.getAddress(), super.getPort());
      return dataPkt; // returns the packet
   } // end build
   
   /**
    * dissect
    * @param dataPkt the data datagram packet
    */
   public void dissect(DatagramPacket dataPkt) {
      // Create a ByteArrayInputStream from the payload
      // NOTE: give the packet data, offset, and length to ByteArrayInputStream
      ByteArrayInputStream bais =
         new ByteArrayInputStream(dataPkt.getData(), dataPkt.getOffset(), dataPkt.getLength());
   
      DataInputStream dis = new DataInputStream(bais);
      
      try {
         super.setOpcode(dis.readShort());
         blockNo = dis.readShort();
         data = new byte[dataPkt.getLength()-4];
         dataLen = dataPkt.getLength()-4;
         for (int i = 0; i < data.length; i++) {
            data[i] = dis.readByte();
         }
                  
         // Close
         dis.close();
      }
      catch (Exception e) {
         System.out.println(e);
      }
   } // end dissect
}