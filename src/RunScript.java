import java.io.*;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Iskren Iliev on 1/24/18.
 */
public class RunScript {

    private static boolean isDone = false;
    private static String database = "BADM";
    private static String fileToSaveIn = "BADM_20180123.sql";
    private static String phraseToLookFor = "PostgreSQL database dump complete";
    private static final int delay = 120;

    public static void main(String[] args) {
//        if(args.length != 2) {
//            System.out.println("Please run: RunScript <dbName> <fileName>");
//            return;
//        }

        RunScript runScript = new RunScript();

        // run backup script
        runScript.doBackup(database, fileToSaveIn);

        // check if done
        while(!runScript.backUpIsDone()) {
            System.out.println("Starting to read file");
            runScript.parseFile(fileToSaveIn, phraseToLookFor);

            if(!runScript.backUpIsDone()) {
                //wait 2 min
                System.out.println("Waiting for process to finish!");
                Runnable runnable = () -> {
                    System.out.println("Waited: "+delay+" sec, checking again...");
                };
                ScheduledExecutorService service = Executors
                        .newSingleThreadScheduledExecutor();
                service.schedule(runnable, delay, TimeUnit.SECONDS);
            }
        }

        System.out.println("Back completed! the local variables will be reset");
        isDone = false;
    }


    /**
     * Status of isDone variable
     * @return true if the required line is found in the fileName
     */
    private boolean backUpIsDone(){
        return isDone;
    }


    /**
     * Goes through each line of the backup file and checks
     * if file has a line "PostgreSQL database dump complete"
     * updates the status of isDone variable if finds it
     * @param fileName
     */
    private void parseFile(String fileName, String phrase){
        File file = new File(fileName);
        try {
            String line;
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()) {
                line = scanner.nextLine();
                if(line.contains(phrase)) {
                    isDone = true;
                    System.out.println("Done: "+line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("File has been read!");
    }


    /**
     * Runs script that creates a back up file for requested DB
     * @param dbName name of the required  database
     * @param fileName name of the file to save the backup at
     */
    private void doBackup(String dbName, String fileName){
        String command;
        //prepare and run DB back up script using provided parameters
        command = "pg_dump -U postgres "+dbName+" -f "+fileName+" -h localhost";
//        command = "pwd";
        try {
            Process proc = Runtime.getRuntime().exec(command);

            // Read the output
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line;
            while((line = reader.readLine()) != null) {
                System.out.print(line + "\n");
            }
            // waits for delay until the process represented by this object has terminated
            proc.waitFor(delay, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Back up file was created!");
    }


}