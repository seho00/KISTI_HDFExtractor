package viewer;

import java.awt.Toolkit;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ncsa.hdf.object.FileFormat;
import ncsa.hdf.view.DefaultFileFilter;
import ncsa.hdf.view.Tools;
import ncsa.hdf.view.ViewProperties;

public class NewFileDialog  extends JFileChooser{

	private static final long serialVersionUID = 4796246032789504234L;

    /** flag if the new file is an HDF5 */
    private String fileType;

    /** The current working directory */
    private String currentDir;

    /** The view working directory */
    private String viewDir;

    private boolean fileCreated;

    private List fileList;

    private final Toolkit toolkit;

    private final JFrame viewer;

    private boolean isH5 = false;

    private boolean isH4 = false;
    
    public NewFileDialog(JFrame owner, String dir, String type, List openFiles) {
        super(dir);

        currentDir = dir;
        viewer = owner;
        viewDir = dir;
        fileType = type;
        fileCreated = false;
        fileList = openFiles;
        toolkit = Toolkit.getDefaultToolkit();
        
        if (currentDir != null) {
            currentDir += File.separator;
        }
        else {
            currentDir = "";
        }

        if (fileType == FileFormat.FILE_TYPE_HDF4) {
            isH4 = true;
            setSelectedFile(Tools.checkNewFile(currentDir, ".hdf"));
            setFileFilter(DefaultFileFilter.getFileFilterHDF4());
        }
        else if (fileType == FileFormat.FILE_TYPE_HDF5) {
            isH5 = true;
            setSelectedFile(Tools.checkNewFile(currentDir, ".h5"));
            setFileFilter(DefaultFileFilter.getFileFilterHDF5());
        }


        this.setAcceptAllFileFilterUsed(false);
        this.showSaveDialog(owner);
    }
    
    @Override
    protected void fireActionPerformed(String command) {
        super.fireActionPerformed(command);

        if (command.equals("ApproveSelection")) {
            fileCreated = createNewFile();
        }
        else {
            fileCreated = false;
        }
    }

    /** create a new HDF file with default file creation properties */
    private boolean createNewFile() {
        File f = this.getSelectedFile();
        if (f == null) {
            return false;
        }

        String fname = f.getAbsolutePath();

        if (fname == null) {
            return false;
        }

        fname = fname.trim();
        if ((fname == null) || (fname.length() == 0)) {
            toolkit.beep();
            JOptionPane.showMessageDialog(this, "Invalid file name.", viewer
                    .getTitle(), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String extensions = FileFormat.getFileExtensions();
        boolean noExtension = true;
        if ((extensions != null) && (extensions.length() > 0)) {
            java.util.StringTokenizer currentExt = new java.util.StringTokenizer(
                    extensions, ",");
            String extension = "";
            String tmpFilename = fname.toLowerCase();
            while (currentExt.hasMoreTokens() && noExtension) {
                extension = currentExt.nextToken().trim().toLowerCase();
                noExtension = !tmpFilename.endsWith("." + extension);
            }
        }

        if (noExtension) {
            if (isH4) {
                fname += ".hdf";
                f = new File(fname);
                setSelectedFile(f);
            }
            else if (isH5) {
                fname += ".h5";
                f = new File(fname);
                setSelectedFile(f);
            }
        }

        if (f.exists() && f.isDirectory()) {
            toolkit.beep();
            JOptionPane.showMessageDialog(this, "File is a directory.", viewer
                    .getTitle(), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        File pfile = f.getParentFile();
        if (pfile == null) {
            fname = viewDir + File.separator + fname;
            f = new File(fname);
        }
        else if (!pfile.exists()) {
            toolkit.beep();
            JOptionPane.showMessageDialog(this, "File path does not exist at\n"
                    + pfile.getPath(), viewer.getTitle(),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // check if the file is in use
        if (fileList != null) {
            FileFormat theFile = null;
            Iterator iterator = fileList.iterator();
            while (iterator.hasNext()) {
                theFile = (FileFormat) iterator.next();
                if (theFile.getFilePath().equals(fname)) {
                    toolkit.beep();
                    JOptionPane
                            .showMessageDialog(
                                    this,
                                    "Unable to create the new file. \nThe file is being used.",
                                    viewer.getTitle(),
                                    JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }

        int newFileFlag = -1;
        if (f.exists()) {
            newFileFlag = JOptionPane.showConfirmDialog(this,
                    "File exists. Do you want to replace it ?", viewer
                            .getTitle(), JOptionPane.YES_NO_OPTION);
            if (newFileFlag == JOptionPane.NO_OPTION) {
                return false;
            }
        }

        currentDir = f.getParent();
        try {
        	int aFlag = FileFormat.FILE_CREATE_DELETE;
        	if (ViewProperties.isEarlyLib())
        		aFlag = FileFormat.FILE_CREATE_DELETE | FileFormat.FILE_CREATE_EARLY_LIB;
            FileFormat.getFileFormat(fileType).createFile(fname, aFlag);
        }
        catch (Exception ex) {
            toolkit.beep();
            JOptionPane.showMessageDialog(this, ex.getMessage(), viewer
                    .getTitle(), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public boolean isFileCreated() {
        return fileCreated;
    }

    public String getFile() {
        String fname = null;
        File f = this.getSelectedFile();
        if (f != null) {
            fname = f.getAbsolutePath();
        }

        return fname;
    }
    
    
}
