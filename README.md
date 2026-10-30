# My Homelab

I started building a home server because I wanted more control over the services and files I use every day. What started as wanting my own media server has turned into a much bigger project involving Docker, networking, storage, remote access, and figuring out how to keep everything running when something goes wrong.

I'm documenting the setup here as I go, including what worked, what broke, and what I still need to fix. This is very much an ongoing project.

## The setup

| Hardware | What I'm using it for |
| --- | --- |
| ThinkPad running ZimaOS | Main server and Docker host |
| Raspberry Pi | Pi-hole DNS filtering |
| Toshiba N300 10TB drive | Media storage |
| APC Back-UPS | Power monitoring and shutdown protection |
| Multi-bay DAS | Storage expansion |

## What I'm running

- **Jellyfin:** My media server.
- **Sonarr and Radarr:** Organizing and managing TV shows and movies.
- **qBittorrent:** Download management.
- **Gluetun + WireGuard:** VPN networking for selected containers.
- **Tailscale:** Getting to my services remotely without exposing everything to the internet.
- **Pi-hole:** Network-wide DNS filtering.
- **NUT:** Monitoring my UPS and working toward reliable shutdowns during outages.

Most of this is running in Docker through ZimaOS.

## How it's connected

See the [architecture diagram](docs/architecture.md) for the general layout.

## What I've learned so far

A lot of this project has been troubleshooting. Getting containers running is one thing. Making sure they can communicate with the right services, keeping traffic where it belongs, managing storage, and planning for power outages has been the more interesting part.

I've also learned to check the simple things first. Sometimes Jellyfin just needs another minute to scan a new file.

## What I'm working on next

- Finish migrating storage to the 10TB drive.
- Verify VPN isolation and kill-switch behavior.
- Add Bazarr for automatic English subtitles.
- Look into Tdarr for cleaning up extra audio and subtitle tracks.
- Finish testing UPS shutdown behavior.
- Work out a backup and restore process.

I'm keeping a more detailed list in the [roadmap](docs/roadmap.md).

## Notes and documentation

- [Architecture](docs/architecture.md)
- [Operations and troubleshooting](docs/operations.md)
- [Security decisions](docs/security.md)
- [Roadmap](docs/roadmap.md)

I liked the idea of keeping this documented in GitHub after coming across [its-a-homelab](https://github.com/ponchofreedo/its-a-homelab). This is my own setup and I'll keep updating it as I make changes.
