package ysrtp.party.app.common.model;

public class RedirectLogin {

    private boolean redirectToLogin;

    public RedirectLogin(boolean redirectToLogin) {
        this.redirectToLogin = redirectToLogin;
    }

    public boolean isRedirectToLogin() {
        return redirectToLogin;
    }
}
