package ysrtp.party.app.network;

public class NetworkConnection {
    private boolean isNetworkConnected;

    public NetworkConnection(boolean isNetworkConnected) {
        this.isNetworkConnected = isNetworkConnected;
    }

    public boolean isNetworkConnected() {
        return isNetworkConnected;
    }
}
