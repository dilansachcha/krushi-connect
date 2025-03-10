package lk.fortyfourss.krushiconnect;

public class Dispute {

    private String disputeId;
    private String orderId;
    private String userId;
    private String issue;
    private String description;
    private String imageUrl;
    private long timestamp;

    public Dispute() {
    }

    public Dispute(String disputeId, String orderId, String userId, String issue, String description, String imageUrl, long timestamp) {
        this.disputeId = disputeId;
        this.orderId = orderId;
        this.userId = userId;
        this.issue = issue;
        this.description = description;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // **Getters and Setters**

    public String getDisputeId() {
        return disputeId;
    }

    public void setDisputeId(String disputeId) {
        this.disputeId = disputeId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
