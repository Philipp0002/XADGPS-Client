package de.raffaelhahn.xadgps_client.async;

public interface AsyncCallback<T> {
    public void received(T data) throws Exception;
    public void error() throws Exception;
    public void finished() throws Exception;
}
