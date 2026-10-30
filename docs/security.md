# Security and disclosure

Never commit credentials, real `.env` files, private keys, VPN configuration, actual IP addresses, Tailscale node identities, admin URLs, private logs, media inventories, or personal photos.

Before each publication, review files and metadata and scan the full Git history for secrets. Removing a secret in a later commit does not eliminate exposure. Use a private deployment repository and a dedicated secret manager for real credentials.

## Publishing checklist
- [ ] Review for sensitive identifiers
- [ ] Scan for secrets
- [ ] Verify all examples use fictional values
- [ ] Ensure no live deployment configuration is included
