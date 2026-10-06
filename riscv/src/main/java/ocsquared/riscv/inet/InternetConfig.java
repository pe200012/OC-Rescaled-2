package ocsquared.riscv.inet;

import java.util.ArrayList;
import java.util.List;

/**
 * Limits for internet access, read when the {@link InternetManager} starts. The defaults are
 * those of OpenComputers II: no private or link-local ranges, no mail or file sharing ports.
 */
public final class InternetConfig {
    /**
     * Whether computers may open connections to the outside world at all.
     */
    public static boolean internetEnabled = true;

    /**
     * Addresses computers may not reach: addresses, CIDR blocks, ranges, interfaces or host names.
     * Loopback, multicast, broadcast and the cloud metadata address are always denied.
     */
    public static List<String> internetDeniedHosts = new ArrayList<>(List.of(
        "10.0.0.0/8",
        "100.64.0.0/10",
        "169.254.0.0/16",
        "172.16.0.0/12",
        "192.0.0.0/24",
        "192.168.0.0/16",
        "198.18.0.0/15",
        "192.88.99.0/24"
    ));

    /**
     * If not empty, only these addresses may be reached, minus the denied ones.
     */
    public static List<String> internetAllowedHosts = new ArrayList<>();

    /**
     * Deny every subnet this server's own network interfaces sit on.
     */
    public static boolean internetDenyLocalSubnets = true;

    /**
     * Ports computers may not connect to, as single ports or inclusive ranges.
     */
    public static List<String> internetDeniedPorts = new ArrayList<>(List.of(
        "25", "465", "587",
        "137-139", "445",
        "1900",
        "3389",
        "11211"
    ));

    /**
     * Connections to 127.0.0.1 on this server's machine that are passed on to computers, as
     * "hostPort:guestAddress:guestPort": "2222:10.0.2.15:22" lets ssh on port 2222 reach the
     * computer whose internet card has the address 10.0.2.15.
     */
    public static List<String> internetForwards = new ArrayList<>();

    public static int internetSessionsTotal = 128;
    public static int internetBytesPerSecond = 64 * 1024;
    public static int internetBytesPerSecondTotal = 512 * 1024;

    private InternetConfig() {
    }
}
