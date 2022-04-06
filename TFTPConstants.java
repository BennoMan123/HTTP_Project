public interface TFTPConstants {
   // Networking
   public static final int TFTP_PORT = 69;
   public static final int MAX_PACKET = 1500;  // P05
   public static final String MODE = "octet";

   // Opcode constants (P06)
   public static final int RRQ = 1;
   public static final int WRQ = 2;                                
   public static final int DATA = 3;
   public static final int ACK = 4;                              
   public static final int ERROR = 5;
   
   // Ecode constants (P03, P06)
   public static final int UNDEF = 0;  // Undefined error - need to implement
   public static final int NOTFND = 1; // File not found - need to implement
   public static final int ACCESS = 2; // Access violation (cannot open file) - need to implement
   public static final int DSKFUL = 3; // Disk full
   public static final int ILLOP = 4;  // Illegal Opcode - need to implement
   public static final int UNKID = 5;  // Unknown transfer ID
   public static final int FILEX = 6;  // File already exists
   public static final int NOUSR = 7;  // No such user
   
   /* RRQ and WRQ Packet Format
   *             n bytes
   * | 2 bytes | string | 1 byte | string | 1 byte |
   * -------------------------------------------
   * |Opcode(1 or 2)|"filename"| 0 | Mode(we will always use octet) | 0 |
   */
   
   // octet request for file to be written or read in binary
   // if WRQ packet send from client first, Server sends back ACK with Block #0 
   
   /* DATA and ACK packet format
   *  |2 bytes |2 bytes  |n bytes
   * ----------------------------
   *  | Opcode | Block # | Data
   *  | Opcode | Block # |
   */
   
   // Block # - number of block being sent over/read ex. sending over Block 1. sending over Block 2, etc. 
   // Data - the packet being sent will always contain 512 bytes (unless its the last packet n < 512 bytes)
   
   
    /* Error Packet Format
    *   | 2 bytes| 2 bytes   | string | 1 byte|
    *   ---------------------------------------
    *   | Opcode | ErrorCode | ErrMsg |   0   |
    */
    
    // ErrorCode -> Ecode Constant
    // ErrMsg -> The error message is intended for human consumption, and should be in netascii (text) (source https://tools.ietf.org/html/rfc1350 below Figure 5-4: ERROR packet) 
    
   // WRQ and DATA packets are acknowledged by ACK or ERROR packets
   // RRQ and ACK packets are acknowledged by DATA or ERROR packets (source https://tools.ietf.org/html/rfc1350 above Figure 5-3: ACK packet)
}