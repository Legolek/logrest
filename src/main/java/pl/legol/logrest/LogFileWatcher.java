package pl.legol.logrest;

import java.io.File;
import java.io.RandomAccessFile;

public class LogFileWatcher implements Runnable {

    private boolean debug = false;

    private int crunchifyRunEveryNSeconds = 2000;
    private LogController logController;
    private long lastKnownPosition = 0;
    private boolean shouldIRun = true;
    private File crunchifyFile = null;
    private static int crunchifyCounter = 0;

    public LogFileWatcher(String myFile, int myInterval, LogController logController) {
        crunchifyFile = new File(myFile);
        this.crunchifyRunEveryNSeconds = myInterval;
        this.logController = logController;
    }

    private void printLine(String message) {
        System.out.println(message);
    }

    public void stopRunning() {
        shouldIRun = false;
    }

    public void run() {
        try {
            while (shouldIRun) {
                Thread.sleep(crunchifyRunEveryNSeconds);
                long fileLength = crunchifyFile.length();
                if (fileLength > lastKnownPosition) {

                    // Reading and writing file
                    RandomAccessFile readWriteFileAccess = new RandomAccessFile(crunchifyFile, "rw");
                    readWriteFileAccess.seek(lastKnownPosition);
                    String crunchifyLine = null;
                    while ((crunchifyLine = readWriteFileAccess.readLine()) != null) {
                        logController.onLogAdded(crunchifyLine);
                        crunchifyCounter++;
                    }
                    lastKnownPosition = readWriteFileAccess.getFilePointer();
                    readWriteFileAccess.close();
                } else {
                    if (debug)
                        this.printLine("Hmm.. Couldn't found new line after line # " + crunchifyCounter);
                }
            }
        } catch (Exception e) {
            stopRunning();
        }
        if (debug)
            this.printLine("Exit the program...");
    }

}
