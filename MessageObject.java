/*Name: Cory Lassila, Z1622287, Date: 12/3/13, Assignment#6
 * This class serves as an object that the server and client 
 * use to communicate.
 * 
 */
import java.io.Serializable;


public class MessageObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private String transactionType = "";
    String name = "";
    String ssn = "";
    String address = "";
    String code = "";
    String Message ="";
    
    public String getTransactionType()
    {
        return transactionType;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getSsn()
    {
        return ssn;
    }
    
    public String getAddress()
    {
        return address;
    }
    
    public String getCode()
    {
        return code;
    }
    public String getMessage()
    {
        return Message;
    }
    
    public void setTransactionType(String transactionType)
    {
        this.transactionType = transactionType;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public void setSsn(String ssn)
    {
        this.ssn = ssn;
    }
    public void setAddress(String address)
    {
        this.address = address;
    }
    public void setCode(String code)
    {
        this.code = code;
    }
    public void setMessage(String msg)
    {
        this.Message = msg;
    }
    
    
}
