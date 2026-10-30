# Security notes

Security has been one of the more interesting parts of setting up the lab, especially when it comes to remote access and Docker networking.

## Remote access

I'm using Tailscale to reach services when I'm away from home. I prefer that over opening up a bunch of ports on my router.

## VPN networking

Gluetun handles WireGuard connectivity for the download workload. One thing still on my list is verifying that the workload cannot fall back to a normal connection if the VPN goes down. I don't want to assume the kill switch works just because the container starts successfully.

## DNS

Pi-hole runs on a Raspberry Pi and handles DNS filtering for my network.

## Configuration files

I use local environment variables for settings that vary between deployments. The [example file](../examples/.env.example) is just a template, with some less-than-serious placeholder values.

## Things I want to improve

- Test the VPN kill switch properly.
- Review container permissions and exposed ports.
- Document backup and restore testing.
- Keep working toward reliable shutdown and recovery after power failures.
