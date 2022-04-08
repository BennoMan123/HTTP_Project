public interface TFTPConstants {
   // Networking
   public static final int TFTP_PORT = 69;
   public static final int MAX_PACKET = 1500; // P05
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
}