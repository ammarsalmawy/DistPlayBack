import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.rmi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try{
            Client client = new Client();
            // look up the remote object from the rmiregistry
            MP mediaPlayerC = (MP) Naming.lookup(MP.SERVICENAME);
            // print options and handle inputs
            Scanner scanner = new Scanner(System.in);
            System.out.println("Welcome to Distributed Playback System");
            System.out.println("1 choose a song to play");
            System.out.println("2 choose a song to upload");
            String option = scanner.nextLine();
            if(option.equals("1"))
            {chooseSpeakerAndSong(mediaPlayerC);}
            else if(option.equals("2"))
            {
                updateSong(mediaPlayerC);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    // choose a playback device and a song
    static void chooseSpeakerAndSong(MP mediaPlayerC) throws RemoteException{
        try{
            // handle playback device selection
            System.out.println("please select a playback device:");
            Scanner scanner = new Scanner(System.in);
            for (int i=0 ; i< mediaPlayerC.GetAvailableSpeakers().size();i++)
            {
                System.out.println(i + " " +mediaPlayerC.GetAvailableSpeakers().get(i));
            }
            String speaker =  scanner.nextLine();
            String chosenSpeaker = "";
            for (int i=0 ; i<mediaPlayerC.GetAvailableSpeakers().size();i++)
            {
                if(speaker.equals(String.valueOf(i)))
                {
                    System.out.println("the chosen speaker "+mediaPlayerC.GetAvailableSpeakers().get(i));
                    chosenSpeaker = mediaPlayerC.GetAvailableSpeakers().get(i);
                }
            }
            // handle song selection
            for (int j = 0; j < mediaPlayerC.GetAllSongs().size() ; j++) {
                System.out.println(j + " " +mediaPlayerC.GetAllSongs().get(j));

            }
            String songname =  scanner.nextLine();
            String chosenSong = "";
            for (int j=0 ; j<mediaPlayerC.GetAllSongs().size();j++)
            {
                if(songname.equals(String.valueOf(j)))
                {
                    System.out.println("the chosen song "+mediaPlayerC.GetAllSongs().get(j));
                    chosenSong = mediaPlayerC.GetAllSongs().get(j);
                }
            }
            System.out.println(chosenSong);
            mediaPlayerC.playSong(chosenSong, chosenSpeaker);
            while (true) {
                // Offer options to the user
                System.out.println("Press 'p' to pause, 'r' to resume,'+' to increase volume, '-' to decrease volume, 'u' to upload a song, or 'c' to choose a different speaker. Press 'q' to quit.");

                String choice = scanner.nextLine();
                // handle user choice and call appropriate methods
                switch (choice) {
                    case "p":
                        mediaPlayerC.Pause(chosenSong);
                        break;
                    case "r":
                        mediaPlayerC.resume(chosenSong);
                        break;
                    case "c":
                        chooseSpeakerAndSong(mediaPlayerC);

                        break;
                    case "+":
                        mediaPlayerC.ChangeVolume(chosenSong,true);
                        break;
                    case "-":
                        mediaPlayerC.ChangeVolume(chosenSong,false);
                        break;
                    case "u":
                        updateSong(mediaPlayerC);
                        break;
                    case "q":
                        mediaPlayerC.Stop(chosenSong);
                        System.out.println("Exiting the program.");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] readSongData(String filePath) throws IOException {
        File file = new File(filePath);
        return Files.readAllBytes(file.toPath());
    }
    // Method to upload a song to the server
    static void updateSong(MP mediaPlayerC){
        Scanner scanner = new Scanner(System.in);
        String audioFilePath = "songs";
        File folder = new File(audioFilePath);
        List<String> allSongs = new ArrayList<>();

        try {
            // display the songs on the client side to upload
            System.out.println("please choose one of the songs you have: ");
            int counter = 0;
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            System.out.println(counter + " " + file.getName() + "\n");
                            allSongs.add(file.getName());
                            counter++;
                        }
                    }
                } else {
                    System.err.println("Error reading folder contents.");
                }
            } else {
                System.err.println("Invalid folder path.");
            }
            // get user selection
            String choosensongUpdate = scanner.nextLine();
            for (int i = 0; i < allSongs.size(); i++) {
                if (choosensongUpdate.equals(String.valueOf(i))) {
                    byte[] songData = readSongData("songs/" + allSongs.get(i));
                    // call the upload method
                    System.out.println(mediaPlayerC.UploadSong(allSongs.get(i), songData));
                    // forward user to choose a playback device and song
                    chooseSpeakerAndSong(mediaPlayerC);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}