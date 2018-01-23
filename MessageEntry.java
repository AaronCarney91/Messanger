import net.jini.core.entry.*;

public class MessageEntry implements Entry
{
    public String message;
    public String from;
    public String to;

    public MessageEntry()
    {
        //No Arg constructor
    }

    //Single message constructor
    public MessageEntry(String m)
    {
        message = m;
    }

    //Full arg constructor
    public MessageEntry(String m, String f, String t)
    {
        message = m;
        from = f;
        to = t;
    }


}
