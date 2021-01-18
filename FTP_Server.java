import java.net.*;
import java.io.*;
import java.util.ArrayList;

//Server Class
public class FTP_Server
{
    
    //Declaring the required variables
     
    static int serverPort;
    Socket clientSocket;
    static ServerSocket serverSocket;
    String clientCommand;
    Object clientParams;
    Object clientFileContents;
    ObjectOutputStream clientOutputObj = null;
    ObjectInputStream inputStreamObj = null;


    public enum Commands {
        ls,
        pwd,
        mkdir,
        get,
        put,
        delete,
        cd
    };

    // Default Constructor
    FTP_Server(int serverPortNo)
    {
        try
        {
            serverPort = serverPortNo;
            serverSocket = new ServerSocket(serverPort);

            System.out.println("Server Running!!!!");
            System.out.println("Please connect to the Server from Client using "+ serverPort +" port.");

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

  
    public void readCommandFromClient()
    {
        try
        {
            this.inputStreamObj = new ObjectInputStream(this.clientSocket.getInputStream());
            Object inputObj = this.inputStreamObj.readObject();

           
            if (inputObj instanceof String[]) {

             
                String[] clientInput = (String[]) inputObj;
                this.clientCommand = clientInput[0].toLowerCase();
                if (clientInput.length > 1 && clientInput[1] != null) {

                    this.clientParams = (String) clientInput[1];
                }


            } else {
                        
                Object[] clientInput = (Object[]) inputObj;
                this.clientCommand = clientInput[0].toString();
                this.clientParams = (String) clientInput[1];
                this.clientFileContents = clientInput[2];

            }
            System.out.println("\n\nCommand received from Client: '"+this.clientCommand+"'");
            System.out.println("Parameters received from Client: '"+this.clientParams+"'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void validateAndExecuteCommand() {
        try {
            String commandResult = null;
            ArrayList<String> lsResult = new ArrayList<String>();
            this.clientOutputObj = new ObjectOutputStream(this.clientSocket.getOutputStream());
            Commands currentCommand = Commands.valueOf(this.clientCommand);

            //Switch based on the command to be executed
            switch (currentCommand) {

                case ls:
                    lsResult = this.executeLs();
                    this.clientOutputObj.writeObject(lsResult);
                    break;

                case pwd:
                    commandResult = this.executePwd();
                    this.clientOutputObj.writeObject(commandResult);
                    break;

                case mkdir:
                    commandResult = this.executeMkdir((String) this.clientParams);
                    this.clientOutputObj.writeObject(commandResult);
                    break;

                case cd:
                    commandResult = this.executeCd((String) this.clientParams);
                    this.clientOutputObj.writeObject(commandResult);
                    break;

                case delete:
                    commandResult = this.executeDelete((String) this.clientParams);
                    this.clientOutputObj.writeObject(commandResult);
                    break;

                case put:
                    commandResult = this.executePut((String) this.clientParams, this.clientFileContents);
                    this.clientOutputObj.writeObject(commandResult);
                    break;

                case get:
                    this.executeGet((String) this.clientParams);
                    break;

                default:
                    commandResult = "Invalid command!!! Please try again.";
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> executeLs() {

        System.out.println("Executing ls...");
        ArrayList<String> fileList = new ArrayList<String>();
        try {

            //Find the required directory
            File fileObj = new File(System.getProperty("user.dir") + "/");

            //Find the files present inside the directory
            File[] listOfFiles = fileObj.listFiles();

            for (int i = 0; i < listOfFiles.length; i++) {
                fileList.add(listOfFiles[i].getName() + "\t");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

    public String executePwd() {
        System.out.println("Executing pwd...");
        return (System.getProperty("user.dir").toString());

    }

    public String executeMkdir(String dirName) {
        System.out.println("Executing mkdir...");
        String result = null;


        File currentDirectory = new File(System.getProperty("user.dir") + "/" + dirName);
        try {
            boolean status = false;

            //If the directory doesn't exists, then create it
            if (!currentDirectory.exists()) {
                currentDirectory.mkdir();
                currentDirectory.setExecutable(true);
                currentDirectory.setReadable(true);
                currentDirectory.setWritable(true);
                status = true;

                if (status) {
                    result = "Directory created successfully!!";
                } else {
                    result = "Directory cannot be created. Please try again.";
                }

            } else {
                result = "Directory already exists!!";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    public String executeCd(String dirName) {
        System.out.println("Executing cd...");
        String result=null;
directory to parent
        if (dirName.equals("..")) {
            File fileObj = new File(System.getProperty("user.dir"));
            System.setProperty("user.dir", fileObj.getAbsoluteFile().getParent());
            result="Directory changed to: " + System.getProperty("user.dir");

        } else {

   
            File dirObj = new File(dirName);
            if(dirObj.exists()){
            System.setProperty("user.dir", dirObj.getAbsolutePath());
            result="Directory changed to: " + System.getProperty("user.dir");
            }else{
                result = "Directory not be found!!! Please try again";
            }

        }
        //Return the new working directory
        return result;
    }


    public String executeDelete(String fileName) {
        System.out.println("Executing delete...");
        String result = null;

        //Create a file handle for the required file to be deleted
        File fileObj = new File(fileName);

        //If the file doesn't exists, then display an error
        if (!fileObj.exists()) {
            result = "Directory / File does not exists!! Please try again.";

        } else if (fileObj.isFile()) {
            //If file exists, then delete the file
            fileObj.delete();
            result = "Directory / File has been deleted successfully.";

        } else if (fileObj.isDirectory()) {

            File[] allFiles = new File(fileName).listFiles();
            for (File individualFile : allFiles) {
                //Delete individual file within the folder
                this.executeDelete(individualFile.getAbsolutePath());

            }
            if (fileObj.list().length == 0) {
                fileObj.delete();
                System.out.println("After final directory delete");
                result = "Directory / File has been deleted successfully.";

            }

        }
        return result;

    }


    public String executePut(String fileName, Object fileContents) {
        System.out.println("Executing put...");
        String result = null;
        try {

            FileOutputStream fileOutputStreamObject = new FileOutputStream(new File(System.getProperty("user.dir") + "/" + fileName));
            byte[] fileBytes= new byte[1024 * 1024];

            fileBytes = (byte[]) fileContents;

            //Write the bytes to the file
            fileOutputStreamObject.write(fileBytes, 0, 1024 * 1024);

            result = "File uploaded successfully!!!";

            //Close the stream
            fileOutputStreamObject.close();

        } catch (Exception e) {
            result="Some error occurred!!!Please try again";
            //e.printStackTrace();
        }
        return result;
    }


    public void executeGet(String fileName) {

        try {

            String result=null;
            System.out.println("Executing get...");
            File fileObj=new File(fileName);

            if(fileObj.exists()){
            FileInputStream fileInputStreamObj = new FileInputStream(fileName);
            
            byte[] fileBytes = new byte[1024 * 1024];

            //Read the contents of a file in a buffer and send it to Client
            fileInputStreamObj.read(fileBytes);

            //Send the contents of file to Client
            this.clientOutputObj.writeObject(fileBytes);

            //Flush the stream
            this.clientOutputObj.flush();
            }else {
                
                result="File not found!!!Please try again.";
                this.clientOutputObj.writeObject(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Main()
    public static void main(String args[]) {
        FTP_Server serverObject=new FTP_Server(Integer.parseInt(args[0]));
        try {
            while (true) {

                serverObject.clientSocket = serverSocket.accept();
                serverObject.readCommandFromClient();
                serverObject.validateAndExecuteCommand();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
