package co.cxip.chrec.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

import co.cxip.chrec.VoiceService;

public class NotificationHandlerBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_LEAVE_ROOM = "ACTION_LEAVE_ROOM";
    public static final String ACTION_MUTE = "ACTION_MUTE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), ACTION_LEAVE_ROOM)) {
            if (VoiceService.getInstance() != null) {
                VoiceService.getInstance().leaveCurrentChannel();
            }
        }else if(Objects.equals(intent.getAction(), ACTION_MUTE)) {
            if (VoiceService.getInstance() != null) {
                VoiceService.getInstance().setSpeakerOn(!VoiceService.getInstance().isSpeakerOn());
                VoiceService.getInstance().updateNotification();
            }
        }
    }
}
