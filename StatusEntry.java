import net.jini.core.entry.*;

public class StatusEntry implements Entry {

    String sender;
    String receiver;

    public StatusEntry()
    {

    }

    public StatusEntry(String s, String r)
    {
        sender = s;
        receiver = r;
    }
}
