import java.rmi.Naming;

public class Server {
    public static void main(String[] args) throws Exception {
        // creates an instance of the MediaPlayerImp
        MediaPlayerImp planningImp = new MediaPlayerImp();
        // bind the instance to rmiregistry using service name of MP interface
        Naming.rebind(MP.SERVICENAME, planningImp);

        System.out.println("Server ready");
    }
}
