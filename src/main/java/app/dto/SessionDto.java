package app.dto;

public class SessionDto {

    private int memberId;
    private String memberRole;
    private int activeOrderId;

    public SessionDto(int memberId, String memberRole, int activeOrderId) {
        this.memberId = memberId;
        this.memberRole = memberRole;
        this.activeOrderId = activeOrderId;
    }

    public int getMemberId() {return memberId;}

    public String getMemberRole() {return memberRole;}

    public int getActiveOrderId() {return activeOrderId;}

    public void setActiveOrderId(int activeOrderId) {this.activeOrderId = activeOrderId;}

}
