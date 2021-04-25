package co.cxip.chrec.api.model;

import java.util.Date;

public class Notification implements Comparable<Notification> {
    public long actionableNotificationId;
    public long notificationId;
    public boolean inUnread;
    public User userProfile;
    public Club club;
    public int eventId;
    public int type;
    public Date timeCreated;
    public String message;
    public String channel;

    public static final int NOTIFICATION_TYPE_USER=1;
    public static final int NOTIFICATION_TYPE_EVENT=16;

    @Override
    public int compareTo(Notification o) {
        return timeCreated.compareTo(o.timeCreated);
    }
}
