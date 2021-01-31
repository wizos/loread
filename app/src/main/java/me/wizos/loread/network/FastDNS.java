package me.wizos.loread.network;

import org.jetbrains.annotations.NotNull;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Dns;

public class FastDNS implements Dns {
    @NotNull
    @Override
    public List<InetAddress> lookup(@NotNull String hostname) throws UnknownHostException {
        try {
            List<InetAddress> mInetAddressesList = new ArrayList<>();
            InetAddress[] mInetAddresses = InetAddress.getAllByName(hostname);
            for (InetAddress address : mInetAddresses) {
                if (address instanceof Inet4Address) {
                    mInetAddressesList.add(0, address);
                } else {
                    mInetAddressesList.add(address);
                }
            }
            return mInetAddressesList;
        } catch (NullPointerException var4) {
            UnknownHostException unknownHostException = new UnknownHostException("Broken system behaviour");
            unknownHostException.initCause(var4);
            throw unknownHostException;
        }
    }
}
