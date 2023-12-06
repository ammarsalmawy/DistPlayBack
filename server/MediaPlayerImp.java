import javax.sound.sampled.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
// define class implements the MP interface
public class MediaPlayerImp extends UnicastRemoteObject implements MP {
    private class PlaybackThread extends Thread {
        private final AudioInputStream audioInputStream;
        private final SourceDataLine sourceDataLine;
        private boolean isPaused = false;

        //playbackThread constructor
        public PlaybackThread(AudioInputStream audioInputStream, SourceDataLine sourceDataLine) {
            this.audioInputStream = audioInputStream;
            this.sourceDataLine = sourceDataLine;
        }
        // set the playback state for pause/resume
        public void setPaused(boolean paused) {  // Add this method
            isPaused = paused;
        }
        // override threads run method
        @Override
        public void run() {
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                // reading from the audio file and writing to the data line
                while (!Thread.interrupted() && (bytesRead = audioInputStream.read(buffer)) != -1) {
                    // check if paused
                    if (!isPaused) {
                        sourceDataLine.write(buffer, 0, bytesRead);
                    } else {
                        Thread.sleep(10);
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                // Close the data line when finished
                sourceDataLine.drain();
                sourceDataLine.stop();
                sourceDataLine.close();
                isPaused = false;
            }
        }
    }
    // map to store playback threads to allow for control
    private Map<String, PlaybackThread> playbackThreads = new HashMap<>();

    private SourceDataLine sourceDataLine;
    AudioInputStream audioInputStream;
    // MediaPlayerImp constructor
    protected MediaPlayerImp() throws RemoteException {
        super();
    }
    // override method to play a song to a specific playback device
    @Override
    public void playSong(String songName, String outputDevice) throws RemoteException {
        try {
            // specify the path to the audio file
            String audioFilePath = "songs/" + songName ;
            System.out.println("Loading audio file: " + audioFilePath);
            // open the audio file
            File audioFile = new File(audioFilePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            // get available playback devices
            Mixer.Info[] mixerInfoArray = AudioSystem.getMixerInfo();
            Mixer desiredMixer = null;
            for (Mixer.Info mixerInfo : mixerInfoArray) {
                if (mixerInfo.getName().equals(outputDevice)) {
                    desiredMixer = AudioSystem.getMixer(mixerInfo);
                    System.out.println("Desired mixer  " + outputDevice);
                    break;
                }
            }
            // open an audio input stream and audio format
            audioInputStream = AudioSystem.getAudioInputStream(new File(audioFilePath));
            AudioFormat audioFormat = audioInputStream.getFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

            // check if desired mixer exists
            if (desiredMixer == null) {
                System.out.println("Desired mixer not found: " + outputDevice);
                return;
            }
            // Open the audio line with the desired mixer and start playing
            sourceDataLine = (SourceDataLine) desiredMixer.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
            // create a new playback thread
            PlaybackThread playbackThread = new PlaybackThread(audioInputStream, sourceDataLine);
            playbackThreads.put(songName, playbackThread);
            playbackThread.start();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    // method to list the songs on the server
    @Override
    public List<String> GetAllSongs() throws RemoteException {
        String audioFilePath = "songs";
        File folder = new File(audioFilePath);
        List<String> allSongs = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        // Add the file name to the song list
                        allSongs.add(file.getName());
                    }
                }
            } else {
                System.err.println("Error reading folder contents.");
            }
        } else {
            System.err.println("Invalid folder path.");
        }
        return allSongs;
    }
    // method to list all available playback devices
    @Override
    public List<String> GetAvailableSpeakers() throws RemoteException {
        List<String> msg = new ArrayList<>();
        Mixer.Info[] mixerInfoArray = AudioSystem.getMixerInfo();
        // check if the playback device support SourceDataLine
        for (Info mixerInfo : mixerInfoArray) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] sourceLineInfoArray = mixer.getSourceLineInfo();
            for (Line.Info info : sourceLineInfoArray) {
                if (info.getLineClass().equals(SourceDataLine.class)) {
                    // add devices that support SourceDataLine to the msg list
                    msg.add(mixerInfo.getName());
                    break;
                }
            }
        }
        return msg;
    }
    // method to allow clients to upload songs
    @Override
    public String UploadSong(String songName, byte[] songData) throws RemoteException {
        try {
            // specify the path to save the song file
            String filePath = "songs/" + songName ;
            String localsongs = "songs";
            File folder = new File(localsongs);
            List<String> availablesongs = new ArrayList<>();
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            // add the file name to the song list
                            availablesongs.add(file.getName());
                        }
                    }
                } else {
                    System.err.println("Error reading folder contents.");
                }
            } else {
                System.err.println("Invalid folder path.");
            }
            // check if a song already exists
            for (String s: availablesongs) {
                if(s.equals(songName))
                {
                    return "this song is already exist";
                }
            }
            //save the song data to a file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(songData);
            }
            return "Song uploaded successfully: " + filePath;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error uploading the song: " + e.getMessage());
        }
    }
    // pause method takes the song name to look up its thread in the threads map then set isPaused true
    @Override
    public void Pause(String SongName) throws RemoteException {
        PlaybackThread playbackThread = playbackThreads.get(SongName);
        if (playbackThread != null) {
            playbackThread.isPaused = true;
        }
    }

    // takes song name to look up the song thread in thread map sets isPaused to false
    @Override
    public void resume(String SongName) throws RemoteException {
        PlaybackThread playbackThread = playbackThreads.get(SongName);
        if (playbackThread != null) {
            playbackThread.setPaused(false);
        }
    }
    // Stop method takes the song name looks ip its thread and interrupt and remove the thread
    @Override
    public void Stop(String songName) throws RemoteException {
        PlaybackThread playbackThread = playbackThreads.get(songName);
        if (playbackThread != null) {
            System.out.println("stopping the song");
            playbackThread.isPaused = true;
            playbackThread.interrupt();
            playbackThreads.remove(songName);
        }
    }

    @Override
    public String ChangeVolume(String songName, boolean increase) throws RemoteException {
        PlaybackThread playbackThread = playbackThreads.get(songName);

        if (playbackThread != null) {
            try {
                // adjust the volume by changing the gain control
                FloatControl gainControl = (FloatControl) playbackThread.sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);

                // get the current volume
                float currentVolume = gainControl.getValue();
                // set the change size
                float stepSize = 5.0f;

                // calculate the new volume
                float newVolume = increase ? currentVolume + stepSize : currentVolume - stepSize;

                // ensure the new volume is within the valid range
                newVolume = Math.max(gainControl.getMinimum(), Math.min(newVolume, gainControl.getMaximum()));

                //set the new volume
                gainControl.setValue(newVolume);

                return "Volume changed successfully: " + newVolume;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return "Error changing volume: " + e.getMessage();
            }
        } else {
            return "Song not found: " + songName;
        }
    }
}
