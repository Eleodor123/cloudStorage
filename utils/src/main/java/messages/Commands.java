package messages;

public class Commands {
    public static final int REQUEST_SERVER_AUTH = 911101;
    public static final int SERVER_RESPONSE_AUTH_OK = 911202;
    public static final int REQUEST_SERVER_FILE_UPLOAD = 101101;
    public static final int CLIENT_RESPONSE_FILE_UPLOAD_OK = 101102;
    public static final int CLIENT_RESPONSE_FILE_UPLOAD_ERROR = 101109;
    public static final int SERVER_RESPONSE_FILE_UPLOAD_OK = 101202;
    public static final int SERVER_RESPONSE_FILE_UPLOAD_ERROR = 101909;
    public static final int REQUEST_SERVER_FILE_DOWNLOAD = 202101;
    public static final int CLIENT_RESPONSE_FILE_DOWNLOAD_OK = 202102;
    public static final int CLIENT_RESPONSE_FILE_DOWNLOAD_ERROR = 202109;
    public static final int SERVER_RESPONSE_FILE_DOWNLOAD_OK = 202202;
    public static final int SERVER_RESPONSE_FILE_DOWNLOAD_ERROR = 202209;
    public static final int REQUEST_SERVER_FILES_LIST = 303101;
    public static final int REQUEST_SERVER_RENAME_FILE = 404101;
    public static final int REQUEST_SERVER_DELETE_FILE = 505101;
    public static final int REQUEST_SERVER_MOVE_FILE = 606101;
}
