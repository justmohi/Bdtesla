# BD TESLA Cloudflare API

This worker is the secure API gateway layer for BD TESLA.

## Current endpoints

- `GET /health` — public health check.
- `GET /v1/me` — validates a Firebase Auth ID token and returns the authenticated UID/phone/admin claim.

## Architecture

Android app → Cloudflare Worker → authenticated backend services → Firebase/Firestore.

The Worker verifies Firebase Auth ID tokens before protected API work is added. Ride matching, admin actions, payment operations, and other privileged operations must remain server-side and must never trust role values supplied by the Android client.

## Configuration

Set the Firebase project ID as a Worker secret:

```bash
wrangler secret put FIREBASE_PROJECT_ID
```

Do not commit Firebase credentials, service-account private keys, or other production secrets.
