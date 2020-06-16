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

public class FileUtils {
    private String msg;

    public boolean saveFile(String toDir, FileMessage fileMessage) {
        try {
            Path path = Paths.get(toDir, fileMessage.getFilename());
            Files.write(path, fileMessage.getData(), StandardOpenOption.CREATE);
            if(Files.size(path) != fileMessage.getFileSize()){
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

    public boolean readFile(String fromDir, FileMessage fileMessage) {
        try {
            fileMessage.readFileData(fromDir);

            Path path = Paths.get(fromDir, fileMessage.getFilename());
            fileMessage.setFileSize(Files.size(path));
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

    public boolean saveFileFragment(String toTempDir, FileFragmentMessage fileFragmentMessage) {
        try {
            Path path = Paths.get(toTempDir,
                    fileFragmentMessage.getFragsNames()[fileFragmentMessage.getCurrentFragNumber() - 1]);

            File dir = new File(toTempDir);
            if(!dir.exists()){
                dir.mkdir();
            }
            Files.write(path, fileFragmentMessage.getData(), StandardOpenOption.CREATE);

            System.out.println("FileUtils.saveUploadedFileFragment() - " +
                    "Files.size(path): " + Files.size(path) +
                    ". fileFragmentMessage.getFileFragmentSize(): " +
                    fileFragmentMessage.getFileFragmentSize());

            if(Files.size(path) != fileFragmentMessage.getFileFragmentSize()){
                msg = "FileUtils.saveUploadedFileFragment() - " +
                        "Wrong the saved file fragment size!";
                return false;
            }
        } catch (IOException e) {
            msg = "FileUtils.saveUploadedFileFragment() - " +
                    "Something wrong with the directory or the file!";
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean compileFileFragments(
            String toTempDir, String toDir, FileFragmentMessage fileFragmentMessage
    ) {
        long start = System.currentTimeMillis();

        try {
            Path pathToFile = Paths.get(toDir, fileFragmentMessage.getFilename());
            Files.deleteIfExists(pathToFile);
            Files.createFile(pathToFile);

            for (int i = 1; i <= fileFragmentMessage.getFragsNames().length; i++) {
                ReadableByteChannel source = Channels.newChannel(
                        Files.newInputStream(Paths.get(toTempDir, fileFragmentMessage.getFragsNames()[i - 1])));
                WritableByteChannel destination = Channels.newChannel(
                        Files.newOutputStream(pathToFile, StandardOpenOption.APPEND));
                copyData(source, destination);
                source.close();
                destination.close();
            }

            if(Files.size(pathToFile) != fileFragmentMessage.getFullFileSize()){
                msg = "FileUtils.compileUploadedFileFragments() - " +
                        "Wrong the saved entire file size!";
                return false;
            } else {
                for (String fragName : fileFragmentMessage.getFragsNames()) {
                    Files.delete(Paths.get(toTempDir, fragName));
                }
                Files.delete(Paths.get(toTempDir));
            }
        } catch (IOException e) {
            msg = "FileUtils.compileUploadedFileFragments() - " +
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

    public String getMsg() {
        return msg;
    }
}