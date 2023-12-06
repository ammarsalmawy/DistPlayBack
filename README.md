#Distributed Playback System
The Distributed Playback System is a Java-based application that allows users to remotely play, control, and manage audio playback on different speakers. The system uses Remote Method Invocation (RMI) to facilitate communication between a server and multiple clients.

##System Overview\
The system consists of two main components: the Server and the Client.\

The Server is responsible for managing the audio playback. It utilizes the MediaPlayerImp class, which implements the MP (MediaPlayer) interface. The server exposes methods for playing songs, pausing, resuming, adjusting volume, uploading new songs, and retrieving information about available songs and speakers.\

The Client is the user interface that communicates with the server to interact with audio playback. It presents users with options to choose a song, speaker, control playback, adjust volume, and upload new songs.\

##how to use \
1. **Compile Code:**\

-Navigate to the [server](./server) and [client](./client) directories separately.\
-Execute "javac *.java" in each directory to compile the Java code\

2.**Run RMI Registry:**\

-Go to the server directory and start the RMI registry "rmiregistry"\

3.**Run the Server:**\

-In the server directory, run the server using "java Server"\

4.**Run the Client(s):**\

-In the client directory, run the server using "java Client"\

5.**User Interaction:**\
-follow on terminal instructions/options\

##Additional Information\
**Song location and format:** \

Songs are expected to be in the "songs" directory. \
while the system supports many audio file formats it is recomended to use wav files  \

**Concurrency:**\

The system handles concurrency using threads, allowing multiple songs to be played simultaneously.\

**Error Handling:** \

The system provides basic error handling. Any errors or exceptions will be printed to the console\
