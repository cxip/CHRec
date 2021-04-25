package co.cxip.chrec;

import androidx.annotation.Nullable;

import java.util.Objects;

import co.cxip.chrec.api.model.Channel;

class DataProvider {
    private static Channel channelCache = null;

    @Nullable
    public static Channel getChannel(String id) {
        if (channelCache == null) return null;
        return Objects.equals(channelCache.channel, id) ? channelCache : null;
    }

    public static void saveChannel(Channel channel) {
        channelCache = channel;
    }
}
