import java.io.*;
import java.net.*;
import javafx.application.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.stage.FileChooser.*;

/**
 * TFTPClient - a class for a client to communicate with a server via UDP for uploading and downloading files
 * Project
 * @author String teamName = null; (Members: Kelly Appleton, Michael Benno, Ethan Gapay)
 * @version 2022-04-20
 */

public class TFTPClient extends Application implements EventHandler<ActionEvent>, TFTPConstants {
   //Window attributes
   private Stage stage;
   private Scene scene;
   private VBox root = new VBox(8);

   //GUI Components
   //Labels
   private Label lblServer = new Label("Server:");
   private Label lblLog = new Label("Log:");

   //Textfields
   private TextField tfServer = new TextField();
   private TextField tfFolder = new TextField();

   //Buttons
   private Button btnChooseFolder = new Button("Choose Folder");
   private Button btnUpload = new Button("Upload");
   private Button btnDownload = new Button("Download");

   //TextArea
   private TextArea taLog = new TextArea();

   /**
    * main program 
    */
   public static void main(String[] args) {
      //new TFTPClient(args);
      launch(args);
   }

   /**
    * start - draw and set up GUI
    */
   public void start(Stage _stage) {
      stage = _stage;
      stage.setTitle("String teamName = null;'s TFTP Client");
   
      //Listen for window close
      stage.setOnCloseRequest(
         new EventHandler<WindowEvent>() {
            public void handle(WindowEvent evt) {
               System.exit(0);
            }
         });
   
      //Row 1 (Top) - Server/IP
      FlowPane fpTop = new FlowPane(8,8);
      fpTop.setAlignment(Pos.CENTER);
      tfServer.setPrefColumnCount(35);
      tfServer.setText("localhost");  //default to local host
      fpTop.getChildren().addAll(lblServer, tfServer);
   
      //Row 2 - Choose Folder Button
      FlowPane fp2 = new FlowPane(8,8);
      fp2.setAlignment(Pos.CENTER);
      fp2.getChildren().add(btnChooseFolder);
      root.getChildren().addAll(fpTop, fp2);
   
      //Row 3 - Folder TextField with Scrollbar
      //Initial Folder (P02)
      File initial = new File("."); 
      tfFolder.setFont(Font.font("MONOSPACED", FontWeight.NORMAL, tfFolder.getFont().getSize()));
      tfFolder.setText(initial.getAbsolutePath());
      tfFolder.setPrefColumnCount(tfFolder.getText().length());
      tfFolder.setDisable(true);
      //Add Scrollbar to text field
      ScrollPane sp = new ScrollPane();
      sp.setContent(tfFolder);
      root.getChildren().add(sp);
   
      //Row 4 - Upload/Download Buttons
      FlowPane fp4 = new FlowPane(8,8);
      fp4.setAlignment(Pos.CENTER);
      fp4.getChildren().addAll(btnUpload, btnDownload);
   
      //Row 5 - Log Label
      FlowPane fp5 = new FlowPane(8,8);
      fp5.setAlignment(Pos.CENTER);
      fp5.getChildren().add(lblLog);
   
      //Row 6 - TextArea
      FlowPane fpBot = new FlowPane(8,8);
      fpBot.setAlignment(Pos.CENTER);
      taLog.setPrefHeight(380);
      taLog.setPrefWidth(410);
      taLog.setWrapText(true);
      taLog.setEditable(false);
      fpBot.getChildren().add(taLog);
   
      //Add remaining FlowPanes/GUI components to root
      root.getChildren().addAll(fp4, fp5, fpBot);
   
      //Listen for the buttons
      btnChooseFolder.setOnAction(this);
      btnUpload.setOnAction(this);
      btnDownload.setOnAction(this);
   
      //Show window
      scene = new Scene(root, 450, 600);
      //Connect stylesheet
      scene.getStylesheets().add("/styles.css");
      stage.setScene(scene);
      //Set stage position
      stage.setX(200);
      stage.setY(100);
      stage.show();
   } //End start

   /** ActionEvent handler for button clicks*/
   public void handle(ActionEvent ae) {
      String command = ((Button) ae.getSource()).getText();
      switch(command) {
         case "Choose Folder":
            doChooseFolder();
            break;
         case "Upload":
            doUpload();
            break;
         case "Download":
            doDownload();
            break;
         default:
            log("Invalid command.");
            break;
      }
   } //End handle

   /**
    * doChooseFolder - sets the folder location for uploads and downloads
    */
   private void doChooseFolder() {
      DirectoryChooser dirChooser = new DirectoryChooser();
      dirChooser.setInitialDirectory(new File(tfFolder.getText()));
      dirChooser.setTitle("Select Folder for Uploads and Downloads");
      File folder = dirChooser.showDialog(stage);
      if (folder == null) {
         log("No folder choosen");
         return;
      }
      tfFolder.setText(folder.getAbsolutePath());
      tfFolder.setPrefColumnCount(tfFolder.getText().length());  //P02
   } //End doChooseFolder

   /**
    * doUpload - uploads a file from the client to a server
    */
   private void doUpload() {      
      try {
         //Present FileChooser (P02)
         FileChooser fileChooser = new FileChooser();
         fileChooser.setInitialDirectory(new File(tfFolder.getText()));
         fileChooser.setTitle("Select the Local File to Upload");
         fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Files", "*.*")); 
         File locFile = fileChooser.showOpenDialog(stage);
         //If no local file is chosen
         if (locFile == null) {
            log("No local file chosen... cancelled!");
            return;
         }
      
         //Setup text input dialog
         TextInputDialog td = new TextInputDialog();
         td.setTitle("Remote Name");
         td.setHeaderText("Enter the file name to save\non the server for the upload");
         td.setContentText("Enter remote file name:");
         td.showAndWait();
         //Result is user entered file name
         String remoteFileName = td.getResult();
         if (remoteFileName == null || remoteFileName.equals("")) {
            log("No remote file name chosen!");
            return;
         }
      
         //Log
         log("--- Start Client Upload ---");
         log("Chosen File Path: " + locFile.getAbsolutePath());
         log("Upload File Name: " + remoteFileName);
         //Create UploadThread and start
         new UploadThread(locFile, remoteFileName).start();
      }
      catch(Exception e) {
         log("Exception during upload  " + e);
         return;
      }  
   }

   //Inner Class
   class UploadThread extends Thread {
      private DatagramSocket dSocket = null;
      private InetAddress iServer = null;   //IP address
      private int serverPort = TFTP_PORT;
      private File locFile = null;
      private String remoteFileName = null;
   
      /**
       * Constructor
       * @param _locFile - the local file chosen for upload
       * @param _remoteFileName - the remote file name to upload as on the server
       */
      public UploadThread(File _locFile, String _remoteFileName) {
         locFile = _locFile;
         remoteFileName = _remoteFileName;
         try {
            //Create a DatagramSocket on an available port
            dSocket = new DatagramSocket();
            dSocket.setSoTimeout(1000);  //Set a timeout on the socket (P07)
            iServer = InetAddress.getByName(tfServer.getText());
         }
         catch(Exception e) {
            log("Error starting upload: " + e);
            return;
         }
      } //End constructor
   
      //Main program for an UploadThread
      public void run() {  
         DataInputStream dis = null; 
         int blockNo = 0;
         int opcode = 0;
         int readSize = 512; //Set to 512 initially to go through entire loop
         String locFileName = locFile.getName();  
         //Log start upload
         log("Starting UPLOAD " + locFileName + " ---> " + remoteFileName);
      
         //Upload/WRQ code
         //P06
         WRQPacket wrqPkt = new WRQPacket(iServer, serverPort, remoteFileName, MODE);
         DatagramPacket dgmPkt = wrqPkt.build();
      
         try {
            //Send DatagramPacket to Server
            log("--> Client sending " + locFileName + "...");
            dSocket.send(dgmPkt);
         
            while(true) {
               //Prepare to receive a datagram and allow to receive max packet size
               DatagramPacket dgmPktRec = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET);
               try {//P07
                  //Receive the packet from the server
                  dSocket.receive(dgmPktRec); //expecting ACKPacket
               }
               catch(SocketTimeoutException ste) { //P07
                  log("ACK not received - upload timed out");
                  return;
               }
            
               Packet packet = new Packet();    
               packet.packetChecker(dgmPktRec);                      
               //Generic packet contains opcode, InetAddress, and port  
               serverPort = packet.getPort();               
               iServer = packet.getAddress();
               opcode = packet.getOpcode();
               //Check received packet's op code... should be 4 (ACK packet)
               if (opcode == ERROR) {
                  readError(dgmPktRec);
                  return;
               }
               else if (opcode != ACK) {
                  log("Unexpected opcode received from server... Sending Error Packet");
                  //Create ERRORPacket and send
                  String errMsg = "Unexpected opcode";
                  ERRORPacket errPkt = new ERRORPacket(iServer, serverPort, ILLOP, errMsg); //illegal opcode error
                  DatagramPacket dgmErr = errPkt.build();
                  dSocket.send(dgmErr);
                  return;
               }
            
               //Get block # of received ACKPacket
               ACKPacket ackPkt = new ACKPacket();
               ackPkt.dissect(dgmPktRec);
               log("<-- Client received reply! ACKPacket (" + ackPkt.getBlockNo() + ")");
            
               //Do on first pass (when dis is not instantiated)
               if (dis == null) {
                  log("Client Upload -- Opening File: " + locFileName);
                  try {
                     dis = new DataInputStream(new FileInputStream(locFile));
                  }
                  catch(Exception e) {
                     log("Cannot open file");
                     //Create ERRORPacket and send
                     String errMsg = "Server is unable to open file for upload";
                     ERRORPacket errPkt = new ERRORPacket(iServer, serverPort, ACCESS, errMsg);
                     DatagramPacket dgmErr = errPkt.build();
                     dSocket.send(dgmErr);
                     return;
                  }
               }
            
               if (readSize < 512) { //Won't be on first pass because we set readSize initially to 512
                  break; //Break out of loop if < 512 read on prior loop
               }
            
               byte[] maxData = new byte[512];
               try {
                  readSize = dis.read(maxData); //Set readSize to number of bytes read, allowing for up to 512
               }
               //Account for errors, empty files, and reaching end of file
               catch(EOFException eofe) {
                  readSize = 0;
               }
               catch(Exception e) {
                  log("Exception: " + e);
                  return;
               }
               if (readSize == -1) {
                  readSize = 0; //to not pass -1 into dataPkt
               }           
               //Increment blockNo
               blockNo++;
            
               //Create DATAPacket to send file contents
               //P06
               DATAPacket dataPkt = new DATAPacket(iServer, serverPort, blockNo, maxData, readSize);
               dgmPkt = dataPkt.build();
            
               //Send DatagramPacket
               log("--> Client sending DATAPacket " + "(" + blockNo + ")");
               dSocket.send(dgmPkt);
            } //End while
            //Close stream
            dis.close();
         }
         catch(Exception e) {
            log("Exception during upload " + e);
            //CREATE ERROR PACKET AND SEND
            String errMsg = "Exception during upload";
            ERRORPacket errPkt = new ERRORPacket(iServer, serverPort, UNDEF, errMsg);
            DatagramPacket dgmErr = errPkt.build();
            try {
               dSocket.send(dgmErr);
            }            
            catch(Exception ex) {
               log("Exception sending upload ERRORPacket");
            }
            return;
         }
         
         finally {
            //Close socket when upload is complete or if error occurs
            try {
               dSocket.close();
            }
            catch(Exception e) {
               log("Error closing socket");
            }
         }
      
         //Log file uploaded complete
         log("--- Uploading " + locFileName + " complete! ---\n");
      } //End run
   } //End inner class UploadThread

   /**
    * doDownload - downloads a file from a server to the client
    */
   private void doDownload() {      
      try {
         //Setup text input dialog
         TextInputDialog td = new TextInputDialog();
         td.setTitle("Remote Name");
         td.setHeaderText("Enter the name of\nthe remote file to download");
         td.setContentText("Enter remote file name:");
         td.showAndWait();
         //Result is user entered file name
         String remoteFileName = td.getResult();
         if (remoteFileName == null || remoteFileName.equals("")){
            log("No remote file name chosen!");
            return;
         }
      
         //Setup FileChooser
         FileChooser fileChooser = new FileChooser();
         fileChooser.setInitialDirectory(new File(tfFolder.getText()));
         fileChooser.setTitle("Select/Enter the file name for saving the download");
         fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Files", "*.*")); 
         File locFile = fileChooser.showSaveDialog(stage);
         //If no local file is chosen
         if (locFile == null) {
            log("No local file chosen... cancelled!");
            return;
         }
         log("--- Starting Client Download ---");
      
         //Create and start DownloadThread
         new DownloadThread(locFile, remoteFileName).start();
      }
      catch(Exception e) {
         log("Exception during download " + e);
         return;
      }
   }

   //Inner Class
   class DownloadThread extends Thread {
      private DatagramSocket dSocket = null;
      private InetAddress iServer = null; //IP address
      private int serverPort = TFTP_PORT;
      private File locFile = null;
      private String remoteFileName = null;
   
      /**
       * Constructor
       * @param _locFile - the local file chosen to download to
       * @param _remoteFileName - the remote file name to download from a server
       */
      public DownloadThread(File _locFile, String _remoteFileName) {
         locFile = _locFile;
         remoteFileName = _remoteFileName;
         try {
            //Create a DatagramSocket on an available port
            dSocket = new DatagramSocket();
            dSocket.setSoTimeout(1000); //Set a timeout on the socket (P07)
            iServer = InetAddress.getByName(tfServer.getText());
         }
         catch(Exception e) {
            log("Error starting download " + e);
            return;
         }
      } //End constructor
   
      //Main program for an DownloadThread
      public void run() {              
         DataOutputStream dos = null; 
         int blockNo = 0;
         int opcode = 0;
         int size = 512;
         String locFileName = locFile.getName();
         //Log start download
         log("Starting DOWNLOAD " + remoteFileName + " ---> " + locFileName);
      
         //Download/RRQ code
         //P06
         RRQPacket rrqPkt = new RRQPacket(iServer, serverPort, remoteFileName, MODE);
         DatagramPacket dgmPkt = rrqPkt.build();
         try {
            //Send DatagramPacket to Server
            log("--> Client sending " + locFileName);
            dSocket.send(dgmPkt);
            while(size == 512) { //Size initially set to 512, so first pass will always go in while loop
               //Prepare to receive DatagramPacket of max packet size
               DatagramPacket dgmPktRec = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET); 
               try { //P07
                  dSocket.receive(dgmPktRec);   
               }
               catch(SocketTimeoutException ste) { //P07
                  log("DATA not received - upload timed out");
                  return;
               }
               Packet packet = new Packet(); //Create generic packet - don't know opcode yet
               packet.packetChecker(dgmPktRec);
               //Generic packet contains opcode, InetAddress, and port  
               serverPort = packet.getPort();               
               iServer = packet.getAddress();
               opcode = packet.getOpcode();
               //Check received packet's opcode... should be 3 (DATA packet)
               if (opcode == ERROR) {
                  readError(dgmPktRec);
                  return;
               }
               else if (opcode != DATA) {
                  //Create ERRORPacket and send
                  String errMsg = "Unexpected opcode";
                  ERRORPacket errPkt = new ERRORPacket(iServer, serverPort, ILLOP, errMsg); //illegal opcode error
                  DatagramPacket dgmErr = errPkt.build();
                  dSocket.send(dgmErr);
                  return;
               }
            
               byte[] maxData = new byte[512];
               //Increment blockNo
               blockNo++;
            
               //Opcode checked as 3 (DATAPacket)
               DATAPacket dataPkt = new DATAPacket();
               dataPkt.dissect(dgmPktRec);
               log("<-- Client received reply! DATAPacket (" + dataPkt.getBlockNo() + ")");
            
               //Set size to length of data in DATAPacket
               size = dataPkt.getDataLen();
            
               //Do on first pass (when dos is not instantiated)
               if (dos == null) { 
                  log("Client Download -- Opening file: " + locFileName);
                  try {
                     dos = new DataOutputStream(new FileOutputStream(locFile)); 
                  }
                  catch(IOException ioe) {
                     log("Cannot create file");
                     //Create ERRORPacket and send
                     String errMsg = "Client is unable to create file for download";
                     ERRORPacket errPkt = new ERRORPacket(iServer, serverPort, UNDEF, errMsg);
                     DatagramPacket dgmErr = errPkt.build();
                     dSocket.send(dgmErr);
                     return;
                  }
               }
               //Write data
               dos.write(dataPkt.getData());            
            
               //Create ACKPacket
               //P06
               ACKPacket ackPkt = new ACKPacket(iServer, serverPort, blockNo);
               //Create DatagramPacket using ackPkt
               dgmPkt = ackPkt.build();
               //Send DatagramPacket
               log("--> Client sending ACKPacket (" + blockNo + ")");
               dSocket.send(dgmPkt);
            } //End while
            //Close stream
            dos.close();
         }
         catch(Exception e) {
            //Log error
            log("Exception during download");
            //Create ERRORPacket and send
            String errMsg = "Exception during download " + e;
            ERRORPacket errPkt = new ERRORPacket(iServer, serverPort, UNDEF, errMsg);
            DatagramPacket dgmErr = errPkt.build();    
            try {
               dSocket.send(dgmErr);
            }
            catch(Exception ex) {
               log("Exception sending download ERRORPacket");
            }  
            return;
         }
         
         finally {
            //Close socket when download is complete or if error occurs
            try {
               dSocket.close();
            }
            catch(Exception e) {
               log("Error closing socket");
            }
         }
      
         //Log file downloaded complete
         log("--- Downloading " + locFileName + " complete! ---\n");
      } //End run
   } //End inner class DownloadThread

   /**
    * readError - gets the error message from a DatagramPacket
    * @param dgmPkt - the DatagramPacket to read the error from
    */
   private void readError(DatagramPacket dgmPkt){
      ERRORPacket errPkt = new ERRORPacket();
      errPkt.dissect(dgmPkt);
      log("ERROR Received -- Ecode " + errPkt.getErrorNo() + ": " + errPkt.getErrorMsg() + "\n --- End Client Upload/Download ---"); 
      return;
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
         });
   } //End of log
} //End TFTPClient