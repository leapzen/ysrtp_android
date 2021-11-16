package ysrtp.party.app.viewarticle;

public class ShareFromNotification {
    private final String platform;
    private final int articleId;

    public ShareFromNotification(int articleId, String platform) {
        this.platform = platform;
        this.articleId = articleId;
    }

    public String getPlatform() {
        return platform;
    }

    public int getArticleId() {
        return articleId;
    }
}
