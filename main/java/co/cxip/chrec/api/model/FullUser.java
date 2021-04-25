package co.cxip.chrec.api.model;

import java.util.Date;
import java.util.List;

public class FullUser extends User{
	public String displayname, bio, twitter, instagram;
	public int numFollowers, numFollowing;
	public boolean followsMe, isBlockedByNetwork;
	public Date timeCreated;
	public User invitedByUserProfile;
	// null = not following
	// 2 = following
	// other values = ?
	public int notificationType;
	public boolean has_unread_notifications;
	public boolean notifications_enabled;
	public User user_profile;
	public List<Club> clubs;
	public boolean has_verified_email;
	public boolean can_edit_username;
	public boolean can_edit_name;
	public boolean can_edit_displayname;
	public String url;
	public List<Topic> topics;

	public boolean isFollowed(){
		return notificationType==2;
	}
}
