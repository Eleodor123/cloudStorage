package utils;

import io.netty.channel.ChannelHandlerContext;
import messages.FileFragmentMessage;
import messages.FileMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class FileUtils {
    private static FileUtils ownObject = new FileUtils();

    public static FileUtils getOwnObject() {
        return ownObject;
    }

    private final ItemUtils itemUtils = ItemUtils.getOwnObject();

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

    public void cutAndSendFileByFrags(Item toDirItem, Item item,
                                     long fullFileSize, Path rootPath,
                                     ChannelHandlerContext ctx, int command) throws IOException {
        long start = System.currentTimeMillis();

        int totalEntireFragsNumber = (int) fullFileSize / FileFragmentMessage.CONST_FRAG_SIZE;
        int finalFileFragmentSize = (int) fullFileSize - FileFragmentMessage.CONST_FRAG_SIZE * totalEntireFragsNumber;
        int totalFragsNumber = (finalFileFragmentSize == 0) ?
                totalEntireFragsNumber : totalEntireFragsNumber + 1;

        long startByte = 0;
        byte[] data = new byte[FileFragmentMessage.CONST_FRAG_SIZE];

        for (int i = 1; i <= totalEntireFragsNumber; i++) {
            FileFragmentMessage fileFragmentMessage = new FileFragmentMessage(
                    toDirItem, item, fullFileSize, i, totalFragsNumber,
                    FileFragmentMessage.CONST_FRAG_SIZE, data);
            fileFragmentMessage.readFileDataToFragment(
                    itemUtils.getRealPath(item.getItemPathname(), rootPath).toString(),
                    startByte);
            startByte += FileFragmentMessage.CONST_FRAG_SIZE;
            ctx.writeAndFlush(new CommandMessage(command, fileFragmentMessage));
        }

        if(totalFragsNumber > totalEntireFragsNumber){
            byte[] dataFinal = new byte[finalFileFragmentSize];
            FileFragmentMessage fileFragmentMessage = new FileFragmentMessage(
                    toDirItem, item, fullFileSize, totalFragsNumber,
                    totalFragsNumber, finalFileFragmentSize, dataFinal);
            fileFragmentMessage.readFileDataToFragment(
                    itemUtils.getRealPath(item.getItemPathname(), rootPath).toString(),
                    startByte);
            ctx.writeAndFlush(new CommandMessage(command, fileFragmentMessage));
        }

        long finish = System.currentTimeMillis() - start;
        System.out.println("FileUtils.cutAndSendFileByFrags() - duration(mc): " + finish);
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
            if(fileFragMsg.getCurrentFragNumber() == 1){
                File dir = new File(realToTempDirPath.toString());
                if(dir.exists()){
                    deleteFolder(dir);
                }
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
            transferDataFromFragsToFinalFile(realToFilePath, fragFiles);
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

    private void transferDataFromFragsToFinalFile(Path realToFilePath,
                                                  File[] fragFiles) throws IOException {
        Files.deleteIfExists(realToFilePath);
        File finalFile = new File(realToFilePath.toString());
        RandomAccessFile toFileRAF = new RandomAccessFile(finalFile, "rw");
        FileChannel toChannel = toFileRAF.getChannel();
        for (File fragFile : fragFiles) {
            FileInputStream fromFileInStream = new FileInputStream(fragFile);
            FileChannel fromChannel = fromFileInStream.getChannel();
            fromChannel.transferTo(0, fragFile.length(), toChannel);
            fromFileInStream.close();
            fromChannel.close();
        }
        toFileRAF.close();
        toChannel.close();
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
