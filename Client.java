import javax.swing.*;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import net.jini.core.lease.*;



public class Client extends JFrame implements RemoteEventListener
{
    //Global Variable
    private static int ONE_MINUTE = 1000 * 60;
    private static int FIVE_SECONDS= 1000 * 5;
    private boolean notifyOn = true;
    private JavaSpace space;
    private RemoteEventListener msgStub, statStub;
    private String userName;
    private MessageEntry msgTemplate;
    //private StatusEntry sTemplate;


    //GUI components
    private JButton getButton, saveButton, deleteButton, sendButton, replyButton, notifyButton;
    private JTextField toBox, messageBox, timeBox;
    private DefaultListModel chatListModel, fromListModel;
    private JList chatList, fromList;
    private JPanel toPanel, messagePanel, chatPanel, buttonPanel, readPanel, writePanel;
    private JLabel toLabel, messageLabel, chatLabel, notifyLabel;




    public Client(String user)
    {
        //Look for JavaSpace
        space = SpaceUtils.getSpace();
        if(space == null)
        {
            System.err.println("Cannot find JavaSpace");
            System.exit(1);
        }
        else
        {
            System.out.println("Connection Successful");
        }

        //Assigns user name and passes it to the Entry msgTemplate.
        //Only receive entries where "To" == "userName"
        userName = user;
        msgTemplate = new MessageEntry(null, null, userName);
        //seTemplate = new StatusEntry(userName, null);


        initComponents();
        pack();

        //Notification Code
        Exporter messageExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
                new BasicILFactory(), false, true);
        try{
            msgStub = (RemoteEventListener) messageExporter.export(this);
            space.notify(msgTemplate, null, msgStub, Lease.FOREVER, null);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        //Attempted to notify user of a message been read by creating a separate Entry class.
        //When a status Entry object was recieved, it would tell user that their message had been read
        /*
        Exporter statusExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
                new BasicILFactory(), false, true);
        try{
            msgStub = (RemoteEventListener) statusExporter.export(this);
            space.notify(sTemplate, null, statStub, Lease.FOREVER, null);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        */




    }


    //Initialize and set up GUI components, also handles listeners
    private void initComponents() {

        setTitle("JavaSpace Client: " + userName);
        addWindowListener (new WindowAdapter () {
            public void windowClosing (WindowEvent evt) {
                exitForm (evt);
            }
        }   );

        Container cp = getContentPane();
        getContentPane().setPreferredSize(new Dimension(500, 250));
        cp.setLayout (new BorderLayout ());

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        cp.add(mainPanel);

        writePanel = new JPanel();
        writePanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(writePanel, BorderLayout.NORTH);

        toPanel = new JPanel();
        toPanel.setLayout(new BorderLayout(0, 0));
        writePanel.add(toPanel, BorderLayout.NORTH);

        toLabel = new JLabel();
        toLabel.setText("To:");
        toPanel.add(toLabel, BorderLayout.WEST);

        toBox = new JTextField();
        toPanel.add(toBox, BorderLayout.CENTER);

        timeBox = new JTextField();
        timeBox.setColumns(3);
        timeBox.setText("");
        toPanel.add(timeBox, BorderLayout.EAST);

        messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout(0, 0));
        writePanel.add(messagePanel, BorderLayout.SOUTH);

        messageLabel = new JLabel();
        messageLabel.setText("Message:");
        messagePanel.add(messageLabel, BorderLayout.WEST);

        sendButton = new JButton();
        sendButton.setText("Send");
        sendButton.setEnabled(false);
        sendButton.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                write(evt);
                messageBox.setText("");
            }
        }  );
        messagePanel.add(sendButton, BorderLayout.EAST);

        messageBox = new JTextField();
        messageBox.setText("");
        messageBox.addKeyListener(new KeyAdapter() {
            //If text fields are empty, disable send button
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (messageBox.getDocument().getLength() > 0 && toBox.getDocument().getLength() > 0)
                {
                    sendButton.setEnabled(true);
                }
                else
                {
                    sendButton.setEnabled(false);
                }
            }
        });
        messagePanel.add(messageBox, BorderLayout.CENTER);

        notifyLabel = new JLabel();
        notifyLabel.setText("");
        mainPanel.add(notifyLabel, BorderLayout.SOUTH);

        readPanel = new JPanel();
        readPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(readPanel, BorderLayout.CENTER);

        chatPanel = new JPanel();
        chatPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        readPanel.add(chatPanel, BorderLayout.WEST);

        chatLabel = new JLabel();
        chatLabel.setText("Chat");
        chatPanel.add(chatLabel);

        fromListModel = new DefaultListModel();
        fromList = new JList(fromListModel);
        fromList.setPreferredSize(new Dimension(120, 200));
        chatPanel.add(fromList);

        chatListModel = new DefaultListModel();
        chatList = new JList(chatListModel);
        chatList.setPreferredSize(new Dimension(120, 200));
        chatPanel.add(chatList);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        readPanel.add(buttonPanel, BorderLayout.CENTER);

        getButton = new JButton();
        getButton.setText("Get");
        getButton.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                get (evt);
            }
        }   );
        buttonPanel.add(getButton);

        saveButton = new JButton();
        saveButton.setText("Save");
        saveButton.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                save(evt);
            }
        }   );
        buttonPanel.add(saveButton);

        deleteButton = new JButton();
        deleteButton.setText("Delete");
        deleteButton.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                //Delete selected content
                int i = chatList.getSelectedIndex();
                if(i >= 0){
                    chatListModel.removeElementAt(i);
                    fromListModel.removeElementAt(i);
                }
            }
        }   );
        buttonPanel.add(deleteButton);

        replyButton = new JButton();
        replyButton.setText("Reply");
        replyButton.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                //Assign selected author to "To:" field. (Reply to sender)
                int i = fromList.getSelectedIndex();
                if(i >= 0){
                    toBox.setText((String) fromListModel.getElementAt(i));
                }
            }
        }   );
        buttonPanel.add(replyButton);

        notifyLabel = new JLabel();
        notifyLabel.setText("");
        mainPanel.add(notifyLabel, BorderLayout.SOUTH);

        notifyButton = new JButton();
        notifyButton.setText("Notify: ON");
        notifyButton.addActionListener (new ActionListener () {
            //Turn notifications on or off
            public void actionPerformed (ActionEvent evt) {
                if (notifyOn == true)
                {
                    notifyOn = false;
                    notifyButton.setText("Notify: OFF");
                }
                else
                {
                    notifyOn = true;
                    notifyButton.setText("Notify: ON");
                }
            }
        }   );
        buttonPanel.add(notifyButton);


    }


    //Write an entry object to the space
    private void write (java.awt.event.ActionEvent evt) {

        try{
            //Separate author names by ',' and assign to a List
            String authors = toBox.getText();
            List<String> authorList = new ArrayList<String>(Arrays.asList(authors.split(",")));
            String message = messageBox.getText();

            //Create entry for each author name entered
            for (int i = 0; i < authorList.size(); i++)
            {
                MessageEntry msg = new MessageEntry(message, userName, authorList.get(i));
                space.write( msg, null, getLease());
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    //Returns the time in seconds for an entry objects lease
    private int getLease()
    {
        //If lease is empty, default set to one minute, otherwise assign lease entered
        int lease;
        try {
            if (timeBox.getText().contentEquals(""))
            {
                lease = ONE_MINUTE;
            }
            else
            {
                lease = Integer.parseInt(timeBox.getText());
                lease = 1000 * lease;
            }
        } catch ( Exception e) {
            e.printStackTrace();

            //If text entered is not numeric, assign default of one minute
            notifyLabel.setText("Invalid Lease");
            lease = ONE_MINUTE;
        }

        return lease;
    }

    //Retrieve entry from space
    private void get (java.awt.event.ActionEvent evt) {
        try {
            MessageEntry got = (MessageEntry)space.take(msgTemplate, null, FIVE_SECONDS);
            notifyLabel.setText(""); //Reset Text
            if (got == null) {
                //If no entry of interest exists
                //fromListModel.addElement("---");
                //chatListModel.addElement("No object found");
                notifyLabel.setText("No Messages");
            }
            else {
                //Add entry object's content to GUI
                fromListModel.addElement(got.from);
                chatListModel.addElement(got.message);

                //Write status entry object back to sender to notify them message has been read
                //StatusEntry received = new StatusEntry(got.from, got.to);
                //space.write(received, null, FIVE_SECONDS);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Resend received entry back to space
    private void save (java.awt.event.ActionEvent evt) {
        try{
            MessageEntry save = new MessageEntry();
            int i = chatList.getSelectedIndex();
            if(i < 0) {
                //Nothing was selected
                notifyLabel.setText("No Message Selected");
            }
            else
            {
                //Assign selected content to entry and write to the space
                save.to = userName; //Message should only be available to saver
                save.from = (String) fromListModel.getElementAt(i);
                save.message = (String) chatListModel.getElementAt(i);
                space.write(save, null, ONE_MINUTE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Called when a entry of interest is written to the space
    @Override
    public void notify(RemoteEvent remoteEvent){
        try{


            if (notifyOn == true)
            {
                if(notifyLabel.getText() == "NEW MESSAGE!")
                {
                    notifyLabel.setText("NEW MESSAGES!");
                }
                else
                {
                    notifyLabel.setText("NEW MESSAGE!");
                }
            }

            //Tell sender their message was read
            /*
            StatusEntry received = (StatusEntry) space.take(seTemplate, null, FIVE_SECONDS);
            if (received != null)
            {
                notifyLabel.setText(received.receiver + " Received their message");
            }
            */

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void exitForm(java.awt.event.WindowEvent evt) {
        System.exit (0);
    }

    public static void main(String[] args)
    {
        new Client("a").setVisible(true);
        new Client("b").setVisible(true);
        new Client("c").setVisible(true);
    }



}
