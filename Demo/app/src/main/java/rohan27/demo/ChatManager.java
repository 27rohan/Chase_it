package rohan27.demo;

/**
 * Created by rohan27 on 5/10/2016.
 */
import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class ChatManager implements Runnable {
    private Socket socket = null;
    Handler handler;
    public ChatManager(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }
    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "ChatHandler";
    @Override
    public void run() {
        try {
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            handler.obtainMessage(WiFiServiceDiscoveryActivity.MY_HANDLE, this)
                    .sendToTarget();
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = iStream.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    //WORKING - LOG SEEN BUT STRING(VALUE OF BUFFER) ISN'T CHANGING
                    Log.d(TAG, "Rec:" + String.valueOf(buffer));
                    // Send the obtained bytes to the UI Activity
                    //input[0] = x & input[1] = y
                    //String input[] = String.valueOf(buffer).split(",");
                    // changing buffer to input
                    handler.obtainMessage(WiFiServiceDiscoveryActivity.MESSAGE_READ,
                            bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            //Log.d(TAG, "WRITE working!");

            oStream.write(buffer);
            //VERY IMP - RESPONSIBLE FOR SENDING 1 CO-ORDINATE AT A TIME
            oStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

}