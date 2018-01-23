import net.jini.space.JavaSpace;
import net.jini.core.transaction.server.TransactionManager;
import java.rmi.RMISecurityManager;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;


public class SpaceUtils
{
    public static JavaSpace getSpace(String host)
    {
        JavaSpace space = null;

        try
        {
            LookupLocator lul = new LookupLocator("jini://" + host);
            ServiceRegistrar reg = lul.getRegistrar();

            Class c = Class.forName("net.jini.space.JavaSpace");
            Class[] classTemplate = {c};

            space = (JavaSpace) reg.lookup(new ServiceTemplate(null, classTemplate, null));
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e);
        }
        return space;
    }

    public static JavaSpace getSpace()
    {
        return getSpace("Aaron-PC");
    }

    public static TransactionManager getManager(String host)
    {
        if(System.getSecurityManager() == null)
        {
            System.setSecurityManager(new RMISecurityManager());
        }

        TransactionManager manager = null;

        try
        {
            LookupLocator lul = new LookupLocator("jini://" + host);
            ServiceRegistrar reg = lul.getRegistrar();

            Class c = Class.forName("net.jini.core.transaction.server.TransactionManager");
            Class[] classTemplate = {c};

            manager = (TransactionManager) reg.lookup(new ServiceTemplate(null, classTemplate, null));
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e);
        }
        return manager;
    }

    public static TransactionManager getManager()
    {
        return getManager("Aaron-PC");
    }

}
