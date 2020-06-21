package utils;

public class Commands {
    public static final int SERVER_NOTIFICATION_CLIENT_CONNECTED = 911000;

    public static final int SERVER_REQUEST_AUTH = 911101;
    public static final int SERVER_RESPONSE_AUTH_OK = 911202;
    public static final int SERVER_RESPONSE_AUTH_ERROR = 911209;

    public static final int SERVER_REQUEST_FILE_UPLOAD = 101101;
    public static final int SERVER_RESPONSE_FILE_UPLOAD_OK = 101202;
    public static final int SERVER_RESPONSE_FILE_UPLOAD_ERROR = 101209;

    public static final int SERVER_REQUEST_FILE_FRAG_UPLOAD = 111101;
    public static final int SERVER_RESPONSE_FILE_FRAG_UPLOAD_OK = 111202;
    public static final int SERVER_RESPONSE_FILE_FRAG_UPLOAD_ERROR = 111209;
    public static final int SERVER_RESPONSE_FILE_FRAGS_UPLOAD_OK = 111222;
    public static final int SERVER_RESPONSE_FILE_FRAGS_UPLOAD_ERROR = 111299;

    public static final int SERVER_REQUEST_DOWNLOAD_FILE = 202101;
    public static final int SERVER_RESPONSE_DOWNLOAD_FILE_OK = 202202;
    public static final int SERVER_RESPONSE_DOWNLOAD_FILE_ERROR = 202209;

    public static final int SERVER_RESPONSE_DOWNLOAD_FILE_FRAG_OK = 222202;
    public static final int SERVER_RESPONSE_DOWNLOAD_FILE_FRAG_ERROR = 222209;
    public static final int CLIENT_RESPONSE_DOWNLOAD_FILE_FRAG_OK = 202102;
    public static final int CLIENT_RESPONSE_DOWNLOAD_FILE_FRAG_ERROR = 202109;

    public static final int SERVER_REQUEST_ITEMS_LIST = 303101;
    public static final int SERVER_RESPONSE_ITEMS_LIST_OK = 303102;

    public static final int SERVER_REQUEST_RENAME_ITEM = 404101;
    public static final int SERVER_RESPONSE_RENAME_ITEM_OK = 404102;
    public static final int SERVER_RESPONSE_RENAME_ITEM_ERROR = 404109;

    public static final int SERVER_REQUEST_DELETE_ITEM = 505101;
    public static final int SERVER_RESPONSE_DELETE_ITEM_OK = 505102;
    public static final int SERVER_RESPONSE_DELETE_ITEM_ERROR = 505109;

    public static final int REQUEST_SERVER_MOVE_FILE = 606101;

}
