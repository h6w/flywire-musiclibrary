package org.bentokit;

public interface FileDownloadListener {
    public void downloadStart(long length);      //The download started, and the file to be downloaded is length bytes
    public void downloadFinish();               //The download was complete
    public void downloadUpdate(long downloaded); //We downloaded some more, and the total amount downloaded so far
}
