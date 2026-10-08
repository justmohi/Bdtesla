import { createRemoteJWKSet, jwtVerify } from "jose";

interface Env {
  FIREBASE_PROJECT_ID: string;
  API_ENV?: string;
}

const firebaseKeys = createRemoteJWKSet(
  new URL("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com")
);

async function authenticate(request: Request, env: Env) {
  const header = request.headers.get("Authorization");
  if (!header?.startsWith("Bearer ")) {
    throw new Response("Unauthorized", { status: 401 });
  }

  const token = header.slice("Bearer ".length).trim();
  const { payload } = await jwtVerify(token, firebaseKeys, {
    issuer: `https://securetoken.google.com/${env.FIREBASE_PROJECT_ID}`,
    audience: env.FIREBASE_PROJECT_ID,
  });

  return payload;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);

    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Headers": "Authorization, Content-Type",
          "Access-Control-Allow-Methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
        },
      });
    }

    if (url.pathname === "/health") {
      return Response.json({ ok: true, service: "bdtesla-api", env: env.API_ENV ?? "production" });
    }

    if (url.pathname === "/v1/me" && request.method === "GET") {
      try {
        const user = await authenticate(request, env);
        return Response.json({
          uid: user.sub,
          phone: user.phone_number ?? null,
          admin: user.admin === true,
        });
      } catch (error) {
        if (error instanceof Response) return error;
        return Response.json({ error: "Unauthorized" }, { status: 401 });
      }
    }

    return Response.json({ error: "Not found" }, { status: 404 });
  },
} satisfies ExportedHandler<Env>;
