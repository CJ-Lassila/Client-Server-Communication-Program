/*Name: Cory Lassila, Z1622287, Date: 12/3/13, Assignment#6
 * These classes are meant to be a server that
 * recieves messages from a client application
 * so the server can manipulate data in a database.
 * 
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;


public class CustServer extends Thread {
   protected final int port = 9711;    // Where 97xx is your port number
   protected ServerSocket listen_socket;
      
   // Constructor -----------------------------------------------
   // Create a ServerSocket to listen for connections.
   // Start the thread.
      
   public CustServer () {
      try {
         listen_socket = new ServerSocket(port);
      } catch (IOException e) {
         fail(e, "Exception creating server socket");
      }
        
      System.out.println("Server listening on port " + port);
      this.start();
   }
          
   // fail ------------------------------------------------------
   // Exit with an error message when an exception occurs
      
   public static void fail(Exception e, String msg) {
      System.err.println(msg + ": " + e);
      System.exit(1);
   }
      
   // run -------------------------------------------------------
   // The body of the server thread. Loop forever, listening for and
   // accepting connections from clients. For each connection, create a
   // new Conversation object to handle the communication through the
   // new Socket.
      
   public void run() {
      try {
         while (true) {
            Socket client_socket = listen_socket.accept();
            
            // create a Conversation object to handle this client and pass
            // it the Socket to use.  If needed, we could save the Conversation
            // object reference in a Vector. In this way we could later iterate
            // through this vector looking for "dead" connections and reclaim
            // any resources.
            Conversation conv = new Conversation(client_socket);
         }
      } catch (IOException e) {
         fail(e, "Exception listening for connections");
      }
   }
      
   // main-------------------------------------------------------
   // Start up the Server program.
     
   public static void main (String args[]) {
      new CustServer();
   }
} // end Server

//**************************************************************
// This class is the Thread that handles all communication with
// the client

class Conversation extends Thread
   {
   protected Socket client;
   protected ObjectInputStream in;
   protected ObjectOutputStream out;

   // Where JavaCustxx is your database name
   private static final String URL = "jdbc:mysql://courses:3306/JavaCust41";

   protected Connection con;
    
   private Statement getAllStmt = null;
   private PreparedStatement addStmt = null;
   private PreparedStatement deleteStmt = null;
   private PreparedStatement updateStmt = null;
      
   // Constructor -----------------------------------------------
   // Initialize the streams and start the thread
      
   public Conversation(Socket client_socket) {


      client = client_socket;

      try {
         out = new ObjectOutputStream(client.getOutputStream()); 
         in  = new ObjectInputStream(client.getInputStream()); 
      } catch(IOException e) {
         try {
            client.close();
         } catch (IOException e2) {}
            
         System.err.println("Exception getting socket streams " + e);
         return;
      }

      try {
         Class.forName("com.mysql.jdbc.Driver").newInstance();
      } catch (ClassNotFoundException e) {
         System.err.println("Exception loading DriverManager class " + e + " " + e.getMessage());
         return;
      } catch (InstantiationException e) {
         System.err.println("Exception loading DriverManager class " + e);
         return;
      } catch (IllegalAccessException e) {
         System.err.println("Exception loading DriverManager class " + e);
         return;
      }

        
      try {
         con = DriverManager.getConnection(URL);
         // Create your Statements and PreparedStatements here            
             
      } catch (SQLException e) {
         System.err.println("Exception connecting to database manager " + e);
         return;
      }

      // start the run loop
      this.start();
   }
      
   // run -------------------------------------------------------
      
   public void run() {
      MessageObject msg = null;
        
      try {
         while (true) {
            //read an object
            msg = (MessageObject) in.readObject();
            if (msg == null)
               break;
            

            String transactionType = msg.getTransactionType();
                
            if (transactionType.equals("GETALL")) {
               handleGetAll();
            } else if (transactionType.equals("ADD")) {
               handleAdd(msg);
            } else if (transactionType.equals("UPDATE")) {
               handleUpdate(msg);
            } else if (transactionType.equals("DELETE")) {
               handleDelete(msg);
            }
         }
      } catch (IOException e) {
         System.err.println("IOException " + e);
      } catch (ClassNotFoundException e) {
         System.err.println("ClassNotFoundException " + e);
      } finally {
         try {
            client.close();
         } catch (IOException e) {
            System.err.println("IOException " + e);
         }
      }  
   } // end run

   //This method handles the GETALL message from the client
   public void handleGetAll() {
       try 
       {
        getAllStmt = con.createStatement();
        ResultSet rs = getAllStmt.executeQuery("select * from cust");//Get the result set from query

        if(rs.next())//If there are any records
        {
            MessageObject mo = new MessageObject();
            mo.setSsn(rs.getString(2));
            mo.setName(rs.getString(1));
            mo.setAddress(rs.getString(3));
            mo.setCode(rs.getString(4));
            out.writeObject(mo);//Send the first object to client
            out.flush();
            out.reset();
            while(rs.next())//If there are more than 1 record send the rest of them
            {
            mo = new MessageObject();
            mo.setSsn(rs.getString(2));
            mo.setName(rs.getString(1));
            mo.setAddress(rs.getString(3));
            mo.setCode(rs.getString(4));
            out.writeObject(mo);
            out.flush();
            out.reset();
            }
                
            MessageObject finale = new MessageObject();
            finale.setMessage("Record Retrieval Complete.");
            out.writeObject(finale);//Send a message after sending all records
            out.flush();  
        }
        else//if there are no records
        {
            MessageObject mo = new MessageObject();
            mo.setMessage("Result Set is empty!");
            out.writeObject(mo);
            out.flush();
        }
       } 
       catch (Exception e) 
       {
        e.printStackTrace();
       }
   }//End handleGetAll
    
 //This method handles the ADD message from the client
   public void handleAdd(MessageObject clientMsg) {
       String sqlstatement = "INSERT INTO cust VALUES ('" + clientMsg.getName() +
               "','" + clientMsg.getSsn() + "','" + clientMsg.getAddress() + "'," + 
               String.valueOf(clientMsg.getCode()) + ")";
       
       try {
        addStmt = con.prepareStatement(sqlstatement);
        addStmt.executeUpdate();
        clientMsg.setMessage("Record successfully added to database!");
        out.writeObject(clientMsg);
        out.flush();
    }
       catch (SQLException e) {
        e.printStackTrace();
        //If there is a duplicate entry and a SQLException is thrown send a message
        clientMsg.setMessage("Error: Duplicate entry, record has not been added!");
        try {
            out.writeObject(clientMsg);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
   }//End handleAdd
    
 //This method handles the DELETE message from the client
   public void handleDelete(MessageObject clientMsg) 
   {
       try {

           deleteStmt = con.prepareStatement("DELETE FROM cust WHERE ssn = ?");
           deleteStmt.setString(1, clientMsg.getSsn());
           int n = deleteStmt.executeUpdate();
           if(n>0)//If theres 1 or more records delete them
           {
           clientMsg.setMessage("Record successfully deleted from database!");
           out.writeObject(clientMsg);
           out.flush();
           }
           else//If theres 0 matching records send a message
           {
               clientMsg.setMessage("Record with matching SSN not found!");
               out.writeObject(clientMsg);
               out.flush();
           }
       }
          catch (SQLException e) {
           e.printStackTrace();
           
           
           } catch (IOException e1) {
               e1.printStackTrace();
           }
   }//end handleDelete

 //This method handles the UPDATE message from the client
   public void handleUpdate(MessageObject clientMsg) 
   {
       try {
       updateStmt = con.prepareStatement("UPDATE cust SET address = ? WHERE ssn = ?");
       updateStmt.setString(1, clientMsg.getAddress());
       updateStmt.setString(2, clientMsg.getSsn());
       int n = updateStmt.executeUpdate();
       if(n>0)//If theres more than 1 record
       {
       clientMsg.setMessage("Record successfully updated!");
       out.writeObject(clientMsg);
       out.flush();
       }
       else//If there are no matching records
       {
       clientMsg.setMessage("Record not found, update did not occur!");
       out.writeObject(clientMsg);
       out.flush();
       }
       } catch (SQLException e) {
           e.printStackTrace();
       } catch (IOException e) {
        e.printStackTrace();
    }
   }//end handleUpdate
}
