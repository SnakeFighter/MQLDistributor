import org.apache.commons.lang.SystemUtils;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class MQLDistributionForm {
    private final static Logger LOGGER = Logger.getLogger(MQLDistributionForm.class.getName());
    private ArrayList<File> terminalFolderList;
    // Filename to save our little bit of data
    final String SAVE_FILENAME = "mqldistrib.save";

    private JPanel rootPanel;
    private JTextField folderEntryField;
    private JLabel bottomLabel;
    private JPanel expertsPanel;
    private JLabel expertsLabel;
    private JLabel indicatorsLabel;
    private JPanel indicatorsPanel;
    private JPanel scriptsPanel;
    private JLabel scriptsLabel;
    private JPanel libraryPanel;
    private JLabel libraryLabel;
    private JPanel includePanel;
    private JLabel includeLabel;
    private JPanel enterFolderPanel;
    private JLabel exampleLabel;
    private JPanel dropTargetPanel;
    private JPanel bottomPanel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("MQL Distributor");
        frame.setContentPane(new MQLDistributionForm().rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public MQLDistributionForm() {

        System.out.println("Starting app");
        setupDragAndDropListeners();
        // Check to see whether a file with our saved path exists
        getSavedPath();
        checkFolderLocationIsOk();
    }

    /**
     * Checks to see whether we have a path saved already!
     */
    private void getSavedPath() {
        Path saveFilePath = Paths.get(SAVE_FILENAME);

        if (Files.exists(saveFilePath)) {
            try {
                // Get all lines in the file, even though we're just using the file.
                List<String> fileLinesList = Files.readAllLines(saveFilePath);
                folderEntryField.setText(fileLinesList.get(0));
            } catch (Exception e) {
                folderEntryField.setText("");
                bottomLabel.setText("Error reading save file");
                return;
            }
        } else {
            // Create the save file if it doesn't already exist.
            try {
                Files.createFile(Paths.get(SAVE_FILENAME));
                bottomLabel.setText("Created new data save file.");
            } catch (IOException e) {
                e.printStackTrace();
                bottomLabel.setText("File write error.");
            }
        }
    }

    /**
     * Sets up the areas on the form to listen for a drag-and-drop event
     */
    private void setupDragAndDropListeners() {
        // What to do with the Experts Panel...
        expertsPanel.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>)
                            evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        //System.out.println(file.getPath());
                        doTransfer(file, "Experts");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Include panel...
        includePanel.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>)
                            evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        //System.out.println(file.getPath());
                        doTransfer(file, "Include");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // And the others etc etc!
        indicatorsPanel.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>)
                            evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        //System.out.println(file.getPath());
                        doTransfer(file, "Indicators");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        libraryPanel.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>)
                            evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        //System.out.println(file.getPath());
                        doTransfer(file, "Libraries");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        scriptsPanel.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>)
                            evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        //System.out.println(file.getPath());
                        doTransfer(file, "Scripts");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Also set a listener on the location entry field.
        folderEntryField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkFolderLocationIsOk();
            }
        });
    }

    /**
     * Method does heavy lifting of actually copying files.
     *
     * @param file
     * @param dropTargetName
     */
    private void doTransfer(File file, String dropTargetName) {
        // First check we have specified a correct terminal folder
        checkFolderLocationIsOk();
        Path sourceFilePath = file.toPath();
        if (terminalFolderList == null) {
            System.out.println("No valid folder locations!");
            bottomLabel.setText("Check folder location!");
            return;
        }
        System.out.println("Transferring file: " + file.getPath() + dropTargetName);
        System.out.println("to: " + dropTargetName);
        // We need to generate a list of destination folders
        ArrayList<File> destinationFolderList = new ArrayList<File>();
        // Let's keep track of whether the copy operation has been successful
        boolean fileOperationsOK = true;
        // Cycle through our list of terminal folders
        for (File iFile:terminalFolderList
             ) {
            // We need to handle the paths differently for Linux and Windows.:

            String destinationStr;
            bottomLabel.setText("Constructing path.");
            if (SystemUtils.IS_OS_WINDOWS) {
                // Back-slash for Win.
                System.out.println("Windows OS.");
                destinationStr = iFile.toString()+"\\"+dropTargetName+"\\"+file.getName();
            } else {
                // Fwd slash for Lin.
                System.out.println("Linux OS. Promise!");
                destinationStr = iFile.toString()+"/"+dropTargetName+"/"+file.getName();
            }
            System.out.println("Target path: "+destinationStr);
            bottomLabel.setText("Path: "+destinationStr);
            Path destPath = Paths.get(destinationStr);
            try {
                Files.copy(sourceFilePath,destPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // The copy operation has failed.
                e.printStackTrace();
                bottomLabel.setText("Error copying file.");
                System.out.println("Error copying "+sourceFilePath.toString()+" to "+destPath.toString());
                fileOperationsOK = false;
            }
        }
        // If it's all worked OK, we can put a friendly message in our notification area.
        bottomLabel.setText("File copy OK.");
    }

    /**
     * Take a look at the entered folder, see if it makes sense!
     */
    private void checkFolderLocationIsOk() {
        Path path;
        try {
            // See if the entered path is valid.
            path = Paths.get(folderEntryField.getText());
        } catch (Exception e) {
            // If not, show an error message.
            bottomLabel.setText("Enter valid folder location.");
            return;
        }
        if (Files.exists(path)) {
            // Check to see whether the folder really is a terminal folder.
            //System.out.println("Folder exists!");
            if (checkForMT4subfolders(path) != null)
            {
                bottomLabel.setText("Folder location OK.");
            } else {
                // If not, here's another error message.
                bottomLabel.setText("Folder does not exist!");
            }
            return;
        } else {
            //System.out.println("Folder does not exist!");
            bottomLabel.setText("Check folder location.");
            return;
        }
    }

    /**
     * Check to see whether there are valid MT4 folders in the one specfied by the user.
     */
    private ArrayList<File> checkForMT4subfolders(Path specifiedPath) {
        // We can return a list of paths to return that are valid destinations for our files
        ArrayList<File> folderList = new ArrayList<>();
        File[] list = new File(specifiedPath.toString()).listFiles();
        // Return a null list if there are no folders here.
        if (list == null) return null;
        // Go through each item in the specified folder
        // We start with the assumption that the folder is not the right one
        //boolean isMT4folder = false;
        for (File iFile:list
             ) {
            if (iFile.isDirectory()) {
                String terminalPath = iFile.toString();
                //System.out.println(terminalPath);
                File []  fileList2 = new File(terminalPath).listFiles();
                for (File jFile:fileList2
                     ) {
                    // See if this is an MQL4 folder...
                    if (jFile.toString().substring(jFile.toString().length()-4,jFile.toString().length()).equals("MQL4")) {
                        folderList.add(jFile);
                        //System.out.println("Adding path: "+jFile);
                    }
                }
                //System.out.println("Folder found: "+terminalPath);
            }
        }
        // If we have found some folders, we can return them. Otherwise null.
        if (folderList.size()>0) {
            terminalFolderList = folderList;
            // As the folder is valid, we can save it to file.
            saveFolderLocation ();
            return folderList;
        } else {
            terminalFolderList = null;
            return null;
        }
    }

    /**
     * Save the terminal folder path to a file for use next time the app is used.
     */
    private void saveFolderLocation() {
        try {
            FileWriter saveFileWriter = new FileWriter(SAVE_FILENAME);
            saveFileWriter.write(folderEntryField.getText());
            saveFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            bottomLabel.setText("Error writing to save file.");
        }
    }
}
