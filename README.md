How to create .gitignore:

https://stackoverflow.com/questions/10744305/how-to-create-a-gitignore-file


Added all files from other location



https://www.journaldev.com/825/java-create-new-file

-From lines 414 and 520 in program TFTPServer.java

4/6/22 commit

realligned all variables and took out all the unneeded comments and got rid of unnecessary stuff



From TFTP Constants class:

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