package utils;

import messages.FileFragmentMessage;
import messages.FileMessage;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class FileUtils {
    private static FileUtils ownObject = new FileUtils();

    public static FileUtils getOwnObject() {
        return ownObject;
    }

    private String msg;

    public boolean readFile(Path realItemPath, FileMessage fileMessage) {
        try {
            fileMessage.readFileData(realItemPath.toString());
            fileMessage.setFileSize(Files.size(realItemPath));
            if(fileMessage.getFileSize() != fileMessage.getData().length){
                msg = "FileUtils.downloadFile() - Wrong the read file size!";
                return false;
            }
        } catch (IOException e) {
            msg = "FileUtils.downloadFile() - Something wrong with the directory or the file!";
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean saveFile(Path realItemPath, byte[] data, long fileSize) {
        try {
            Files.write(realItemPath, data, StandardOpenOption.CREATE);
            if(Files.size(realItemPath) != fileSize){
                msg = "FileUtils.saveFile() - Wrong the saved file size!";
                return false;
            }
        } catch (IOException e) {
            msg = "FileUtils.saveFile() - Something wrong with the directory or the file!";
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean saveFileFragment(Path realToTempDirPath, Path realToFragPath,
                                    FileFragmentMessage fileFragMsg) {
        try {
            File dir = new File(realToTempDirPath.toString());
            if(!dir.exists()){
                System.out.println("FileUtils.saveFileFragment() - " +
                        "dir." + dir.getPath() +
                        ", dir.mkdir(): " + dir.mkdir());
            }
            Files.write(realToFragPath, fileFragMsg.getData(), StandardOpenOption.CREATE);
            if(Files.size(realToFragPath) != fileFragMsg.getFileFragmentSize()){
                msg = "FileUtils.saveFileFragment() - " +
                        "Wrong the saved file fragment size!";
                return false;
            }
        } catch (IOException e) {
            msg = "FileUtils.saveFileFragment() - " +
                    "Something wrong with the directory or the file!";
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean compileFileFragments(Path realToTempDirPath, Path realToFilePath,
                                        FileFragmentMessage fileFragMsg) {
        long start = System.currentTimeMillis();

        try {
            File tempDirFileObject = new File(realToTempDirPath.toString());
            File[] fragFiles = tempDirFileObject.listFiles();
            assert fragFiles != null;
            if(fragFiles.length != fileFragMsg.getTotalFragsNumber()){
                msg = ("FileUtils.compileFileFragments() - " +
                        "Wrong the saved file fragments count!");
                return false;
            }
            Files.deleteIfExists(realToFilePath);
            Files.createFile(realToFilePath);
            for (File fragFile : fragFiles) {
                ReadableByteChannel source = Channels.newChannel(
                        Files.newInputStream(Paths.get(fragFile.getPath())));
                WritableByteChannel destination = Channels.newChannel(
                        Files.newOutputStream(realToFilePath, StandardOpenOption.APPEND));
                copyData(source, destination);

                System.out.println("FileUtils.compileFileFragments() - " +
                        "fragFiles[i].getName(): " + fragFile.getName() +
                        "FileFragSize: " + Files.size(Paths.get(fragFile.getPath())) +
                        ". Files.size(realToFilePath): " + Files.size(realToFilePath));

                source.close();
                destination.close();
            }

            if(Files.size(realToFilePath) != fileFragMsg.getFullFileSize()){
                msg = "FileUtils.compileFileFragments() - " +
                        "Wrong a size of the saved entire file!";
                return false;
            } else {
                if(!deleteFolder(tempDirFileObject)){
                    msg = "FileUtils.compileFileFragments() - " +
                            "Something wrong with the temp folder deleting!!";
                    return false;
                }
            }
        } catch (IOException e) {
            msg = "FileUtils.compileFileFragments() - " +
                    "Something wrong with the directory or the file!";
            e.printStackTrace();
            return false;
        }

        long finish = System.currentTimeMillis() - start;
        System.out.println("FileUtils.compileUploadedFileFragments() - duration(mc): " + finish);

        return true;
    }

    private void copyData(ReadableByteChannel source, WritableByteChannel destination) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        while (source.read(buffer) != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                destination.write(buffer);
            }
            buffer.clear();
        }
    }

    public boolean deleteFileObject(File fileObject) {
        boolean result;
        if(fileObject.isDirectory()){
            result = deleteFolder(fileObject);
        } else{
            result = fileObject.delete();
        }
        return result;
    }

    private boolean deleteFolder(File folder) {
        for (File f : Objects.requireNonNull(folder.listFiles())) {
            if(f.isDirectory()){
                deleteFolder(f);
            } else{
                System.out.println("FileUtils.deleteFolder() - f.delete(): " + f.delete());
            }
        }
        return Objects.requireNonNull(folder.listFiles()).length == 0 && folder.delete();
    }

    public String getMsg() {
        return msg;
    }
}
