import java.io.File;

// команды для файлов
public class FileMessage extends AbstractMessage {
    File file;
    String fName;

    public FileMessage (String fName) {
        this.fName = fName;
        this.file = new File(fName);
    }
}
