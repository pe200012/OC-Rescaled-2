/* SPDX-License-Identifier: MIT */
// Adapted from OpenComputers II (https://github.com/fnuecke/oc2), Copyright (c) Florian "Sangar" Nücke.

package li.cil.oc.riscv.inet.l4;

import li.cil.oc.riscv.inet.util.InternetUtils;

public sealed interface SessionKey {
    int destinationIpAddress();

    record Echo(int sourceIpAddress, int destinationIpAddress, short identity) implements SessionKey {
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder("icmp ");
            InternetUtils.ipv4AddressToString(builder, sourceIpAddress);
            builder.append(" -> ");
            InternetUtils.ipv4AddressToString(builder, destinationIpAddress);
            return builder.append(" id=").append(Short.toUnsignedInt(identity)).toString();
        }
    }

    record Datagram(int sourceIpAddress, short sourcePort, int destinationIpAddress,
                    short destinationPort) implements SessionKey {
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder("udp ");
            InternetUtils.socketAddressToString(builder, sourceIpAddress, sourcePort);
            builder.append(" -> ");
            InternetUtils.socketAddressToString(builder, destinationIpAddress, destinationPort);
            return builder.toString();
        }
    }

    record Stream(int sourceIpAddress, short sourcePort, int destinationIpAddress,
                  short destinationPort) implements SessionKey {
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder("tcp ");
            InternetUtils.socketAddressToString(builder, sourceIpAddress, sourcePort);
            builder.append(" -> ");
            InternetUtils.socketAddressToString(builder, destinationIpAddress, destinationPort);
            return builder.toString();
        }
    }
}
