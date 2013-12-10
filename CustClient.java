/*Name: Cory Lassila, Z1622287, Date: 12/3/13, Assignment#6
 * This class is the client application which sends messages
 * to the server program which in turn communicates with a database.
 * The user can put information in various textboxes to manipulate the DB.
 * 
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.*;

public class CustClient extends JFrame implements ActionListener {

   // Declare GUI components here

   private Socket connection;
   private ObjectInputStream in;
   private ObjectOutputStream out;
   private JTextArea jta = new JTextArea(); 
   private JButton getAllButton = new JButton("Get All");
   private JButton addButton = new JButton("Add");
   private JButton updateButton = new JButton("Update");
   private JButton deleteButton = new JButton("Delete");
   private JLabel namelbl = new JLabel("Name:");
   private JLabel ssnlbl = new JLabel("SSN:");
   private JLabel addresslbl = new JLabel("Address:");
   private JLabel codelbl = new JLabel("Code:");
   private JTextField namejtf = new JTextField();
   private JTextField ssnjtf = new JTextField();
   private JTextField addressjtf = new JTextField();
   private JTextField codejtf = new JTextField();
   private JLabel clientlbl = new JLabel("Client started");
   
   
   private static final long serialVersionUID = 1L;

   /**
    * @param args
    */
   public static void main(String[] args) {
      CustClient client = new CustClient();
      client.connectToServer();
   }
   
   /**
    * CustClient()
    * 
    * Sets up interface and attempts to connect to the server.
    */
   public CustClient() {

      super("Customer Database");
      
      SwingUtilities.invokeLater(new Runnable() { public void run() {
          
          setupGui(); // Code to set up GUI components and listeners
        

      }});
      
   }

   //This method sets up the entire GUI and adds listeners for buttons
   public void setupGui()
   {
       setLayout(new BorderLayout());
       setSize(600,300);
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       setVisible(true);
       
       JPanel jp = new JPanel(new BorderLayout());
       JScrollPane jsp = new JScrollPane(jta);
       JPanel jpgrid = new JPanel(new GridLayout(2,4));
       JPanel jpflow = new JPanel(new FlowLayout());
       jsp.setPreferredSize(new Dimension(this.getSize().width/2,this.getSize().height/2));
       jta.setEnabled(false);
       jta.setDisabledTextColor(Color.BLACK);//Set text color of the disabled textarea
       
       jpgrid.add(namelbl);
       jpgrid.add(namejtf);
       jpgrid.add(addresslbl);
       jpgrid.add(addressjtf);
       jpgrid.add(ssnlbl);
       jpgrid.add(ssnjtf);
       jpgrid.add(codelbl);
       jpgrid.add(codejtf);
       
       jpflow.add(getAllButton);
       jpflow.add(addButton);
       jpflow.add(updateButton);
       jpflow.add(deleteButton);
       
       jp.add(jpgrid,BorderLayout.NORTH);
       jp.add(jpflow,BorderLayout.CENTER);
       jp.add(clientlbl,BorderLayout.SOUTH);
       
       getAllButton.addActionListener(this);
       addButton.addActionListener(this);
       updateButton.addActionListener(this);
       deleteButton.addActionListener(this);
       
       this.add(jp,BorderLayout.CENTER);
       this.add(jsp,BorderLayout.SOUTH);
   }//end setupGui
   
   
   /**
    * connectToServer()
    * 
    * Creates a Socket to connect to the server. If successful, the
    * input and output streams are obtained.
    */
   public void connectToServer() {
      try {

         // Enter your port number in place of '97xx' in the following statement
          connection = new Socket("hopper.cs.niu.edu",9711);

         System.out.println("Socket opened");

         out = new ObjectOutputStream(connection.getOutputStream()); 
         in  = new ObjectInputStream(connection.getInputStream()); 

         System.out.println("Streams opened");
            
      } catch (UnknownHostException e) {
         System.err.println("Unable to resolve host name");
      } catch (IOException e) {
         System.err.println("Unable to establish connection");
         System.err.println("IOException " + e);
      }
   }

   
   /**
    * actionPerformed()
    * 
    * Responds to ActionEvents from buttons.
    * 
    * @param e - An ActionEvent.
    */
   @Override
   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == getAllButton) {
         handleGetAll();  
      } else if (e.getSource() == addButton) {
         handleAdd();
      } else if (e.getSource() == updateButton) {
         handleUpdate();
      } else if (e.getSource() == deleteButton) {
         handleDelete();
      }
   }
   
   //This method sets the textbox fields blank
   public void clearTextbox()
   {
       namejtf.setText("");
       ssnjtf.setText("");
       addressjtf.setText("");
       codejtf.setText("");
       
   }

   //This method sends a GETALL message to server if successful
   public void handleGetAll() {
       MessageObject mo = new MessageObject();
       mo.setTransactionType("GETALL");
       try 
       {
           out.writeObject(mo);
           out.flush();
           while(true)//wait for response from server
           {
               mo = (MessageObject)in.readObject();
               
               if(!mo.Message.equals(""))
               {
                   jta.append("------------------------------------------------------------------------------------------");
                   jta.append("\n");
                   clientlbl.setText(mo.getMessage());
                   break;
               }
               jta.append("NAME:"+mo.getName()+","+"     SSN:"+mo.getSsn()+","+"     ADDRESS:"+mo.getAddress()+","+"     CODE:"+
               mo.getCode());
               jta.append("\n");   
           }
       } 
       catch (IOException e)
       {
           e.printStackTrace();
       } catch (ClassNotFoundException e) {
        e.printStackTrace();
    }
   }//end getall

 //This method sends a ADD message to server if successful
   public void handleAdd() {
       MessageObject mo = new MessageObject();
       
       //Make sure all textbox have text in them
       if(!addressjtf.getText().isEmpty()&&!namejtf.getText().isEmpty()&&!ssnjtf.getText().isEmpty()&&!codejtf.getText().isEmpty())
       {
           try//Test to make sure code is a number greater than 0
           {
           Integer.parseInt(codejtf.getText());
           }
           catch(NumberFormatException nfe)
           {
               clientlbl.setText("ERROR: The Code Field must be a number greater than 0!");
               return;
           }
           mo.setTransactionType("ADD");
           mo.setAddress(addressjtf.getText());
           mo.setName(namejtf.getText());
           mo.setSsn(ssnjtf.getText());
           mo.setCode(codejtf.getText().toString());
           
           try {
               out.writeObject(mo);
               out.flush();
               while(true)//Wait for response from server
               {
                   mo = (MessageObject)in.readObject();
                   if(mo.getMessage()!="")
                   {
                   clientlbl.setText(mo.Message);
                   clearTextbox();
                   break;
                   }
               }
           } catch (IOException e) {
               e.printStackTrace();
           } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
       }
       else
       {
           clientlbl.setText("ERROR: All fields must have an entry to ADD a record!");
       }
      
   }//end add

 //This method sends a UPDATE message to server if successful
   public void handleUpdate() 
   {
       MessageObject mo = new MessageObject();
       mo.setTransactionType("UPDATE");
       if(!ssnjtf.getText().isEmpty()&&!addressjtf.getText().isEmpty())//Check for text in textbox
       {
           mo.setSsn(ssnjtf.getText());
           mo.setAddress(addressjtf.getText());
           try {
               out.writeObject(mo);
               out.flush();
           
         while(true)//Wait for response from server
         {
             mo = (MessageObject)in.readObject();
             if(mo.getMessage()!="")
             {
             clientlbl.setText(mo.Message);
             clearTextbox();
             break;
             }
         }
          
         } catch (IOException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
           e.printStackTrace();
       }
       }
       else
           clientlbl.setText("Error: Both Address & ssn fields must have an entry!");
   }//end update

 //This method sends a DELETE message to server if successful
   public void handleDelete() {
       MessageObject mo = new MessageObject();
       mo.setTransactionType("DELETE");
       if(!ssnjtf.getText().isEmpty())
       {
           mo.setSsn(ssnjtf.getText());
          try {
                out.writeObject(mo);
                out.flush();
            
          while(true)//Wait for response from server
          {
              mo = (MessageObject)in.readObject();
              if(mo.getMessage()!="")
              {
              clientlbl.setText(mo.Message);
              clearTextbox();
              break;
              }
          }  
          } catch (IOException e) {
              e.printStackTrace();
          } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
       }
       else
           clientlbl.setText("Error: Must enter a ssn to delete a record!");   
   }//end delete
}
