package gui;

import api.IServerControler;
import api.IServerGui;
import controler.ThreadedServer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

import java.util.List;

public class ServerController implements IServerGui {

    @FXML Text info;
    @FXML TextArea logContainer;
    private boolean started;
    private boolean disposed;

    public void handleSubmitButtonAction(ActionEvent actionEvent) {
        info.setText("Server has started");

        IServerControler serverControler = new ThreadedServer();
        serverControler.setServerGui(this);
        serverControler.setBroadCast(true);
        serverControler.setKeepConnection(true);
        serverControler.start(8051);

    }

    private int getPort() {
        int result = 8051;
        return result;
    }

    @Override
    public void log(final String mesg) {
        logContainer.appendText(mesg);
    }

    @Override
    public void setError(final String error) {
        logContainer.appendText(error);
    }

    @Override
    public void setHost(final String host, final int port) {
        logContainer.appendText("host: " + host + " -- port : " + port);
    }

    @Override
    public void setClients(final List<String> clients) {

    }

    @Override
    public void setMode(final boolean broadCast, final boolean keepConnection) {

    }

    @Override
    public void setStarted(boolean started) {
        this.started = started;
    }

    @Override
    public void setControler(IServerControler c) {

    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

}
