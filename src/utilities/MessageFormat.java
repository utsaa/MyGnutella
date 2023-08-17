package utilities;

import java.io.Serializable;

public class MessageFormat implements Serializable {

    String fileToDownload;
    String msgId;
    int fromPeerId;
    int TTlValue;

    public String getFileToDownload() {
        return fileToDownload;
    }

    public String getMsgId() {
        return msgId;
    }

    public int getFromPeerId() {
        return fromPeerId;
    }

    public int getTTlValue() {
        return TTlValue;
    }

    public void setTTlValue(int TTlValue) {
        this.TTlValue = TTlValue;
    }

    public void setFname(String fileToDownload) {
        this.fileToDownload = fileToDownload;
    }

    public void setMsgId(String msgId) {
        this.msgId=msgId;
    }

    public void setFromPeerId(int fromPeerId) {
        this.fromPeerId=fromPeerId;
    }


}
