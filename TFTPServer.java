import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * TFTPServer - a class for a server to communicate with one or more clients via UDP for uploading and downloading files
 * Project
 * @author String teamName = null; (Members: Kelly Appleton, Michael Benno, Ethan Gapay)
 * @version 2021-05-04
 */

public class TFTPServer extends Application implements EventHandler<ActionEvent>, TFTPConstants {
   // Window attributes
   private Stage stage;
   private Scene scene;
   private VBox root = new VBox(8);
   
   // GUI Components
   // Labels
   private Label lblStartStop = new Label("Start the server: ");
   
   // Textfields
   private TextField tfFolder = new TextField();
   
   // Buttons
   private Button btnChooseFolder = new Button("Choose Folder");
   private Button btnStartStop = new Button("Start");
   
   // TextArea
   private TextArea taLog = new TextArea();

   // Socket
   private DatagramSocket mainSocket = null;
   private InetAddress iServer = null;
   
   // Port
   public static final int SERVER_PORT = TFTP_PORT;
   
   /**
    * main program
    */
   public static void main(String[] args) {
      launch(args);
   }
   
   /**
    * Start, draw and set up GUI
    */
   public void start(Stage _stage) {
      // Window setup
      stage = _stage;
      stage.setTitle("String teamName = null;'s TFTP Server");
      
      // Listen for window close
      stage.setOnCloseRequest(
         new EventHandler<WindowEvent>() {
            public void handle(WindowEvent evt) { 
               System.exit(0);
            }
         } );
               
      // Row 1 (Top) - Choose Folder Button
      FlowPane fpTop = new FlowPane(8,8);
      fpTop.setAlignment(Pos.CENTER);
      fpTop.getChildren().add(btnChooseFolder);
      root.getChildren().add(fpTop);
      
      // Row 2 - Folder Text Field with Scrollbar
      // Initial Folder (P02)
      File initial = new File(".");
      tfFolder.setFont(Font.font(
         "MONOSPACED", FontWeight.NORMAL, tfFolder.getFont().getSize()));
      tfFolder.setText(initial.getAbsolutePath());
      tfFolder.setPrefColumnCount(tfFolder.getText().length());
      tfFolder.setDisable(true);
      // add Scrollbar to text field (P02)
      ScrollPane sp = new ScrollPane();
      sp.setContent(tfFolder);
      root.getChildren().add(sp);
      
      // Row 3 - Server Start (and Stop) Button
      FlowPane fp3 = new FlowPane(8,8);
      fp3.setAlignment(Pos.CENTER);
      // style button
      btnStartStop.setStyle("-fx-background-color: #30c224;");  // green
      fp3.getChildren().addAll(lblStartStop, btnStartStop);
      
      // Row 4 (Bottom) - Text Area (log)
      FlowPane fpBot = new FlowPane(8,8);
      fpBot.setAlignment(Pos.CENTER);
      taLog.setPrefWidth(360);
      taLog.setPrefHeight(320);
      taLog.setWrapText(true);
      taLog.setEditable(false);
      fpBot.getChildren().add(taLog);
   
      // add remaining FlowPanes/GUI components to root
      root.getChildren().addAll(fp3, fpBot);
      
      // Listen for buttons
      btnStartStop.setOnAction(this);
      btnChooseFolder.setOnAction(this);
      
      // Show window
      scene = new Scene(root, 400, 450);
      // connect stylesheet
      scene.getStylesheets().add("/styles.css");
      stage.setScene(scene);
      // set stage position
      stage.setX(800);
      stage.setY(100);
      stage.show();    
   }
   
   /** ActionEvent handler for button clicks*/
   public void handle(ActionEvent ae) {
      String command = ((Button) ae.getSource()).getText();
      
      switch(command) {
         case "Choose Folder":
            doChooseFolder();
            break;
         case "Start":
            doStartStop();
            break;
         case "Stop":
            doStartStop();
            break;
         default:
            log("Invalid command");
            break;
      }
   } // end handle
   
   /**
    * doStartStop - starts or stops the server
    */
   public void doStartStop() {
      if(btnStartStop.getText().equals("Start")) {  // change Start btn to Stop btn
         btnStartStop.setText("Stop");
         btnStartStop.setStyle("-fx-background-color: #ff0000;"); // change button color to red
         lblStartStop.setText("Stop the server: ");
         tfFolder.setDisable(true);  // P02
         btnChooseFolder.setDisable(true);  // P02
         // create and start a ServerThread
         ServerThread st = new ServerThread();
         st.start();
         log("< Server started >");
      } else {  // change Stop btn to Start btn
         btnStartStop.setText("Start");
         btnStartStop.setStyle("-fx-background-color: #30c224;"); // change button color to green
         lblStartStop.setText("Start the server: ");
         tfFolder.setDisable(false);  // P02
         btnChooseFolder.setDisable(false);  // P02
         // close socket
         if(mainSocket != null) {
            try {
               mainSocket.close();
            }
            catch(Exception e) {
              // reset socket
              log("Exception in stop: " + e);
            }
            mainSocket = null;
         }
      }
   } // end doStartStop
   
   /**
    * doChooseFolder - sets the folder location for uploads and downloads
    */
   public void doChooseFolder() {
      DirectoryChooser dirChooser = new DirectoryChooser();
      dirChooser.setInitialDirectory(new File(tfFolder.getText()));
      dirChooser.setTitle("Select Folder for Uploads and Downloads");
      File folder = dirChooser.showDialog(stage);
      if (folder == null) {
         log("No folder chosen");
         return;
      }
      tfFolder.setText(folder.getAbsolutePath());
      tfFolder.setPrefColumnCount(tfFolder.getText().length()); // P02
   } // end doChooseFolder


   // Inner Class
   // P05
   class ServerThread extends Thread {
      public void run() {
         // Server stuff ... wait for a packet and process it
         try {
            iServer = InetAddress.getLocalHost();
            // mainSocket is a DatagramSocket declared in the global scope
            // and initialized to null
            mainSocket = new DatagramSocket(SERVER_PORT);   // Binds the socket to the port
         }
         catch(UnknownHostException uhe) {
            log("Unknown Host " + uhe);
            return;
         }
         catch(IOException ioe) {
            log("Exception " + ioe);
            return;
         }
         
         // wait for a packet from a new client, then
         // start a client thread
         while(true) {
            // The socket for the client is created in the client thread
            // create DatagramPacket of max size to receive first packet from a client
            byte[] holder = new byte[MAX_PACKET];
            DatagramPacket recPacket = new DatagramPacket(holder, MAX_PACKET);
            try {
               // We get a DatagramPacket, instead of a Socket, in the UDP case
               mainSocket.receive(recPacket);  // wait for 1st packet
               log("\n--- Client packet received! ---");
            }
            catch(IOException ioe) {
               // Happens when the mainSocket is closed while waiting
               // to receive - This is how we stop the server.
               log("< Server stopped >");
               return;
            }
               
            // Create a thread for the client
            // Instead of passing a Socket to the client thread, we pass the 1st packet
            ClientThread ct = new ClientThread(recPacket); // client socket will be created in client thread
            ct.start();
            log("Starting Client Thread...");
         } // of while loop
      } // of run
   } // end of inner class ServerThread


   // Inner Class
   class ClientThread extends Thread {
      // Since attributes are per-object items, each ClientThread has its OWN
      // socket, unique to that client
      private DatagramSocket cSocket = null;
      private int clientPort = 0;
      private DatagramPacket firstPkt = null;
   
      // Constructor for ClientThread
      public ClientThread(DatagramPacket _pkt) {
         firstPkt = _pkt;
         // So - the new DatagramSocket is on a DIFFERENT port,
         // chosen by the OS. If we use cSocket from now on, then
         // port switching has been achieved.
      }
      
      // main program for a ClientThread
      public void run() {
         try {
            // create a DatagramSocket on an available port
            cSocket = new DatagramSocket();
            cSocket.setSoTimeout(1000);  // set a timeout on the socket (P07)
         } 
         catch (Exception e) {
            log("ClientThread Exception: " + e);
            return;
         } 
         
         try {
            // In this try-catch run the protocol, using firstPkt as
            // the first packet in the conversation
            Packet packet = new Packet();
            packet.packetChecker(firstPkt);
            int opcode = packet.getOpcode();
            clientPort = packet.getPort();
            iServer = packet.getAddress();
            
            if (opcode == RRQ) {
               log("<--- Server received RRQPacket");
               doDownload(firstPkt);
            } else if (opcode == WRQ) {
               log("<--- Server received WRQPacket");
               doUpload(firstPkt);
            } else { // ex. DATAPacket or ACKPacket as first packet
               log("Unexpected opcode received from client... Sending Error Packet");
               // Create ERRORPacket and send
               String errMsg = "Unexpected opcode";
               ERRORPacket errPkt = new ERRORPacket(iServer, clientPort, ILLOP, errMsg); // illegal opcode error
               //  (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg)
               DatagramPacket dgmErr = errPkt.build();
               cSocket.send(dgmErr);
            }    
         }
         catch(Exception e) {
            log("Error checking received packet " + e);
            // Create ERRORPacket and send
            String errMsg = "Error checking received packet";
            ERRORPacket errPkt = new ERRORPacket(iServer, clientPort, UNDEF, errMsg);
               //  (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg)
            DatagramPacket dgmErr = errPkt.build();
            try {
               cSocket.send(dgmErr);
            }
            catch(IOException ioe) {
               log("Exception " + ioe);
               return;
            }
            return;
         }
      
         /*
            More from P05: 
            As the conversation progresses, to receive a packet:
               byte[] holder = new holder[MAX_PACKET];
               DatagramPacket incoming = new DatagramPacket(holder, MAX_PACKET);
               cSocket.receive(incoming);
               Then - dissect the incoming packet and process it
            THE NEXT SET OF NOTES DISCUSSES HOW TO DISSECT PACKETS
      
               To send a packet:
               Compute the contents of the outgoing packet
               Build the packet ... producing a DatagramPacket, outgoing
               cSocket.send(outgoing);
            THE NEXT SET OF NOTES ALSO DISCUSSES HOW TO BUILD PACKETS
         
               log("Client disconnected!\n");
         */
      
         log("--- Client completed! ---");
         return;
      } // end run 
      
      /**
       * doUpload - uploads a file from a client to the server
       * @param dgmPkt the first DatagramPacket sent from the client containing the write request 
       */
      private void doUpload(DatagramPacket dgmPkt) {
         String fileName = null;
         int blockNo = 0;
         int opcode = 0;
         int size = 512;
         DataOutputStream dos = null;
         
         // Opcode checked as WRQ -> create and dissect
         WRQPacket wrqPkt = new WRQPacket();
         wrqPkt.dissect(dgmPkt);
         
         // file to upload
         fileName = wrqPkt.getFileName();
         
         // create ACK
         // P06
         ACKPacket ackPkt = new ACKPacket(iServer, clientPort, blockNo);
         //  (InetAddress _toAddress, int _port, int _blockNo)
         dgmPkt = ackPkt.build();
         
         log("Starting Server UPLOAD -- sending ACKPacket (" + blockNo + ")");
         try {
            // send to Client
            cSocket.send(dgmPkt);
         }
         catch(Exception e) {
            log("Error sending ACKPacket " + e);
            return;
         }
         
         try {
            while(size == 512) {  // size initially set to 512, so first pass will always go in while loop
               // prepare to receive DatagramPacket of max packet size
               dgmPkt = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET); 
               try {  // P07
                  cSocket.receive(dgmPkt);   
               }
               catch(SocketTimeoutException ste) {  // P07
                  log("DATA not received - upload timed out");
                  return;
               }
               
               Packet packet = new Packet();  // create generic packet - don't know opcode yet
               packet.packetChecker(dgmPkt);
               // generic packet contains opcode, InetAddress, and port                  
               opcode = packet.getOpcode();
               // check if opcode is an ERROR
               if(opcode == ERROR) {
                  readError(dgmPkt);
               } else if(opcode != DATA) { // if it's anything but DATA or ERROR, send ERRORPacket
                  log("Unexpected opcode received from client... Sending Error Packet");
                  // Create ERRORPacket and send
                  String errMsg = "Unexpected opcode";
                  ERRORPacket errPkt = new ERRORPacket(iServer, clientPort, ILLOP, errMsg); // illegal opcode error
                  //  (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg)
                  DatagramPacket dgmErr = errPkt.build();
                  cSocket.send(dgmErr);
                  return;
               }
               
               byte[] maxData = new byte[512];
               
               // increment blockNo
               blockNo++;
            
               // opcode checked as 3 (DATAPacket)
               // DATAPacket dataPkt = new DATAPacket(iServer, clientPort, blockNo, maxData, size);
                                                  // (InetAddress _toAddress, int _port, int _blockNo, byte[] _data, int _dataLen)            
               DATAPacket dataPkt = new DATAPacket();
               dataPkt.dissect(dgmPkt);
               log("<-- Server received reply! DATAPacket (" + dataPkt.getBlockNo() + ")");
               
               // set size to length of data in DATAPacket
               size = dataPkt.getDataLen();
            
               // do on first pass (when dos is not instantiated)
               if(dos == null) {
                  log("Server Download -- Opening file: " + fileName);
               
                  // https://www.journaldev.com/825/java-create-new-file
                  String fileSeparator = System.getProperty("file.separator");
                  String absoluteFilePath = fileSeparator + tfFolder.getText() + fileSeparator + fileName;
                  File file = new File(absoluteFilePath);
                  try {
                     dos = new DataOutputStream(new FileOutputStream(file));
                  }
                  catch(IOException ioe) {
                     log("Cannot open file");
                     // Create ERRORPacket and send
                     String errMsg = "Server is unable to open file for upload";
                     ERRORPacket errPkt = new ERRORPacket(iServer, clientPort, ACCESS, errMsg);
                     //  (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg)
                     DatagramPacket dgmErr = errPkt.build();
                     cSocket.send(dgmErr);
                     return;
                  }
               }
               
               // write data
               dos.write(dataPkt.getData());            
            
               // create ACKPacket
               // P06
               ackPkt = new ACKPacket(iServer, clientPort, blockNo);
               // create DatagramPacket using ackPkt
               dgmPkt = ackPkt.build();
               // send DatagramPacket
               log("--> Server sending ACKPacket (" + blockNo + ")");
               cSocket.send(dgmPkt);
            } // end while
         }
         catch (Exception e) {
            log("Exception during upload " + e);
            // Create ERRORPacket and send
            String errMsg = "Exception during upload " + e;
            ERRORPacket errPkt = new ERRORPacket(iServer, clientPort, UNDEF, errMsg);
               //  (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg)
            DatagramPacket dgmErr = errPkt.build();
            try {
               cSocket.send(dgmErr);
            }            
            catch(Exception ex) {
               log("Exception sending upload ERRORPacket");
            }
            return;
         }
         
         finally {
            // Close socket when upload is complete or if error occurs
            try { 
               dos.close();
               cSocket.close();
            }
            catch(Exception e) {
               log("Exception closing socket/stream " + e);
               return;
            }
         }
         
         // log file uploaded complete
         log("Uploading " + fileName + " complete!");
         
      } // end doUpload
      
      /**
       * doDownload - downloads a file from the server to the client
       * @param dgmPkt the first DatagramPacket sent from the client containing the read request 
       */
      private void doDownload(DatagramPacket dgmPkt) {   
         String fileName = null;
         int blockNo = 0;
         int opcode = 0;
         int readSize = 512;
         DataInputStream dis = null;
         
         // Opcode checked as RRQ -> create and dissect
         RRQPacket rrqPkt = new RRQPacket();
         rrqPkt.dissect(dgmPkt);
         
         // file to download
         fileName = rrqPkt.getFileName();
               
         clientPort = rrqPkt.getPort();
         
         // check if file requested exists in folder
         if (!checkDir(fileName)) {
            log("No such file exists...");
            // Create ERRORPacket and send
            String errMsg = "File does not exist";
            ERRORPacket errPkt = new ERRORPacket(iServer, clientPort, NOTFND, errMsg); // File Not Found error code
            //  (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg)
            DatagramPacket dgmErr = errPkt.build();
            try {
               cSocket.send(dgmErr);
            }
            catch(IOException ioe) {
               log("Error sending packet " + ioe);
            }
            return;
         }
         
         log("Server Download -- Opening file: " + fileName);
         
         try { 
            while(readSize == 512) {  // readSize initially set to 512, so first pass will always go in while loop
               try {
                  // do on first pass (when dis is not instantiated)
                  if (dis == null) {                            
                     try {
                        // https://www.journaldev.com/825/java-create-new-file
                        String fileSeparator = System.getProperty("file.separator");
                        String absoluteFilePath = fileSeparator + tfFolder.getText() + fileSeparator + fileName;
                        File file = new File(absoluteFilePath);
                        dis = new DataInputStream(new FileInputStream(file));
                     }
                     catch(IOException ioe) {
                        log("Cannot open file");
                        // Create ERRORPacket and send
                        String errMsg = "Server is unable to open file for download";
                        ERRORPacket errPkt = new ERRORPacket(iServer, clientPort, ACCESS, errMsg);
                        //  (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg)
                        DatagramPacket dgmErr = errPkt.build();
                        cSocket.send(dgmErr);
                        return;
                     }
                  }
               
                  byte[] maxData = new byte[512];  // prepare to read max number of bytes
                  
                  try {
                     readSize = dis.read(maxData);  // set readSize to number of bytes read, allowing for up to 512
                  } 
                  // Account for errors, empty files, and reaching end of file           
                  catch(EOFException eofe) {
                     readSize = 0;
                  }
                  catch(Exception e) {
                     log("Exception " + e);
                     return;
                  }
                     
                  if (readSize == -1) {
                     readSize = 0;  // to not pass -1 into dataPkt
                  }
               
                  // increment blockNo
                  blockNo++;
               
                  // create DATAPacket to send file contents
                  // P06
                  DATAPacket dataPkt = new DATAPacket(iServer, clientPort, blockNo, maxData, readSize);
                  //                   (InetAddress _toAddress, int _port, int _blockNo, byte[] _data, int _dataLen)
                  dgmPkt = dataPkt.build();
                     
                  log("--> Server sending DATAPacket (" + blockNo + ")");
                  // send DatagramPacket
                  cSocket.send(dgmPkt);              
               
                  // prepare to receive a datagram and allow to receive max packet size
                  dgmPkt = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET);
                  try {
                     cSocket.receive(dgmPkt);
                  }
                  catch(SocketTimeoutException ste) {  // P07
                     log("ACK not received - download timed out");
                     return;
                  }
                  
                  Packet packet = new Packet();
                  packet.packetChecker(dgmPkt);
                  // generic packet contains opcode, InetAddress, and port                
                  opcode = packet.getOpcode();
                  // check received packet's op code... should be 4 (ACK packet)
                  if(opcode == ERROR) {
                     readError(dgmPkt);
                     return;
                  } else if(opcode != ACK) {
                     // Create ERRORPacket and send
                     String errMsg = "Unexpected opcode";
                     ERRORPacket errPkt = new ERRORPacket(iServer, clientPort, ILLOP, errMsg);
                     //  (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg)
                     DatagramPacket dgmErr = errPkt.build();
                     cSocket.send(dgmErr);
                     return;
                  }
                  
                  ACKPacket ackPkt = new ACKPacket();
                  ackPkt.dissect(dgmPkt);
                  log("<-- Server received reply! ACKPacket (" + ackPkt.getBlockNo() + ")");
               }
               catch(Exception e) {
                  log("Exception during download " + e);
                  return;
               }
            } // end while
         }
         catch(Exception e) {
            log("Exception during download " + e);
            // Create ERRORPacket and send
            String errMsg = "Exception during download";
            ERRORPacket errPkt = new ERRORPacket(iServer, clientPort, UNDEF, errMsg);
            //  (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg)
            DatagramPacket dgmErr = errPkt.build();
            try {
               cSocket.send(dgmErr);
            }
            catch(Exception ex) {
               log("Exception sending download ERRORPacket");
            }
            
            return;
         }
         
         finally {
            // Close socket when download is complete or if error occurs
            try {
               dis.close();
               cSocket.close();
            }
            catch(Exception e) {
               log("Exception closing socket/stream " + e);
            }
         }
         
         // log file downloaded complete
         log("Downloading " + fileName + " complete!");
         
      } // end doDownload
   } // end of inner class TFTPClientThread
   
   /**
    * readError - gets the error message from a DatagramPacket
    * @param dgmPkt - the DatagramPacket to read the error from
    */
   private void readError(DatagramPacket dgmPkt){
      ERRORPacket errPkt = new ERRORPacket();
      errPkt.dissect(dgmPkt);
      log("Server Recieved -- Opcode " + ERROR + ", Ecode " + errPkt.getErrorNo() + ": " + errPkt.getErrorMsg()); 
      return;
   }
   
   /**
    * checkDir - checks if file exists in directory
    * @param fileName - the file name to check for
    */
   private boolean checkDir(String fileName) {
      File dir = new File(tfFolder.getText());
      File[] folderContents = dir.listFiles();
      for (File f: folderContents) {
         if(fileName.equals(f.getName())) {
            return true;
         }
      }
      return false;
   }
   
   /**
    * log - utility method to log a message in a thread safe manner
    * @param message - the message to append to the text area
    */
   private void log(String message) {
      Platform.runLater(
         new Runnable() {
            public void run() {
               taLog.appendText(message + "\n");
            }
         } );
   } // end of log
} // end of TFTPServer