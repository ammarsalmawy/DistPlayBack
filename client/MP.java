import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MP  extends Remote {

    public final static String SERVICENAME = "ArithService";
    void playSong(String songName, String outputDevice) throws RemoteException;
    List<String> GetAllSongs()  throws RemoteException;
    List<String> GetAvailableSpeakers() throws RemoteException;
    String UploadSong(String name, byte[] songData) throws RemoteException;
    void Pause(String SongName)  throws RemoteException;
    void resume(String SongName)  throws RemoteException;
    void Stop(String songName)  throws RemoteException;
    String ChangeVolume(String songName, boolean increase) throws RemoteException;
}
