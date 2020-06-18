import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
import javax.swing.JOptionPane;

//based on code snippets from original Java course
public class ChatClient {
  private Socket connection = null;
  private BufferedReader serverIn = null;
  private PrintStream serverOut = null;
    
  private TextArea output;
  private Label usernameLabel;
  private Label currentUsernameLabel;
  private TextField username;
  private TextField input;
  private Button connectButton;
  private Button sendButton;
  private Button quitButton;
  private Frame frame;

  public ChatClient() {
    output = new TextArea(10,50);
    usernameLabel = new Label("Enter your username and click Connect:");
    currentUsernameLabel = new Label("Your username is: ");
    username = new TextField(30);
    input = new TextField(50);
    connectButton = new Button("Connect");
    sendButton = new Button("Send");
    quitButton = new Button("Quit");
  }

  public void launchFrame() {
    frame = new Frame("PPC Chat");

    // Use the Border Layout for the frame
    frame.setLayout(new BorderLayout());

    frame.add(output, BorderLayout.WEST);
    frame.add(input, BorderLayout.SOUTH);

    // Create the button panel
    Panel p1 = new Panel(); 
    p1.setLayout(new GridLayout(6, 1));
    p1.add(usernameLabel);
    p1.add(username);
    p1.add(currentUsernameLabel);
    p1.add(connectButton);
    p1.add(sendButton);
    p1.add(quitButton);
    
    sendButton.setVisible(false);
    currentUsernameLabel.setVisible(false);

    // Add the button panel to the center
    frame.add(p1, BorderLayout.CENTER);

    // Create menu bar and File menu
    MenuBar mb = new MenuBar();
    Menu file = new Menu("File");
    MenuItem quitMenuItem = new MenuItem("Quit");
    quitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	System.exit(0);
      }
    });
    file.add(quitMenuItem);
    mb.add(file);
    frame.setMenuBar(mb);

    // Add Help menu to menu bar
    Menu help = new Menu("Help");
    MenuItem aboutMenuItem = new MenuItem("About");
    aboutMenuItem.addActionListener(new AboutHandler());
    help.add(aboutMenuItem);
    mb.setHelpMenu(help);

    // Attach listener to the appropriate components
    connectButton.addActionListener(new ConnectHandler());
    sendButton.addActionListener(new SendHandler());
    input.addActionListener(new SendHandler());
    frame.addWindowListener(new CloseHandler());
    quitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.exit(0);
        }
    });

    frame.pack();
    frame.setVisible(true);
    frame.setLocationRelativeTo(null);
  }
  
  public void doConnect() {
    String serverIP = System.getProperty("serverIP", "127.0.0.1");
    String serverPort = System.getProperty("serverPort", "2000");
    
      try {
        connection = new Socket(serverIP, Integer.parseInt(serverPort));
        
        InputStream is = connection.getInputStream();
        InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        
        serverIn = new BufferedReader(isr);
        serverOut = new PrintStream(connection.getOutputStream());
        
        Thread t = new Thread(new RemoteReader());
        t.start();
        
        currentUsernameLabel.setText(currentUsernameLabel.getText() + username.getText());
        
        sendButton.setVisible(true);
        connectButton.setVisible(false);
        usernameLabel.setVisible(false);
        username.setVisible(false);
        currentUsernameLabel.setVisible(true);
      } catch (Exception e) {
          System.err.println("Unable to connect to server!");
          e.printStackTrace();
      }
  }
  
  private class ConnectHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      doConnect();
    }
  }

  private class SendHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String text = input.getText();
      text = username.getText()+ ": " + text + "\n";
      serverOut.print(text);
      input.setText("");
    }
  }
  
  private class RemoteReader implements Runnable {
    public void run() {
      try {
        while ( true ) {
          String nextLine = serverIn.readLine();
          output.append(nextLine + "\n");
        }
      } catch (Exception e) {
          System.err.println("Error while reading from server.");
          e.printStackTrace();
        }
    }
}

  private class CloseHandler extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      System.exit(0);
    }
  }

  private class AboutHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JOptionPane.showMessageDialog(frame, "The ChatClient is a neat tool that allows you to talk " +
                  "to other ChatClients via a ChatServer");
    }
  }

  public static void main(String[] args) {
    ChatClient c = new ChatClient();
    c.launchFrame();
  }
}
