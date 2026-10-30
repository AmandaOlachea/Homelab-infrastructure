# Notes from running the lab

## Jellyfin

I had a movie finish downloading and wondered why it wasn't showing up in Jellyfin. It turned out the library just needed a little time to refresh. A good reminder to check the simple things first.

## VPN

Gluetun and WireGuard are connected. I still want to test what happens to the download workload if the VPN goes down.

## Pi-hole

I set up Pi-hole on a Raspberry Pi and confirmed DNS filtering was working. I like keeping this separate from the main server.

## UPS

NUT can communicate with the APC UPS and report its status. I'm working on validating shutdown behavior so a power outage doesn't turn into a bigger problem.

I'll keep adding troubleshooting notes as I run into things.
