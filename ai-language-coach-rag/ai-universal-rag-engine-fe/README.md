# AI Language Coach - Frontend

Next.js 15 (React 19) frontend for the AI Language Coach platform.

## Features

- **Authentication**: JWT-based login via Java backend
- **Configuration**: Set target language and proficiency level
- **Ingestion**: Upload content to RAG engine
- **Performance**: Real-time metrics dashboard with charts

## Tech Stack

- Next.js 15 (React 19)
- TypeScript
- Tailwind CSS
- TanStack Query v5
- Recharts

## Quick Start

```bash
npm install
npm run dev
```

## Deployment

```bash
npm run build
npm start
```

Or use Docker:

```bash
podman build -t ai-frontend .
podman run -d -p 3000:3000 ai-frontend
```

## Environment Variables

| Variable | Description |
|----------|-------------|
| `NEXT_PUBLIC_JAVA_API_URL` | Java backend API URL |
| `NEXT_PUBLIC_RAG_API_URL` | RAG engine URL |
| `NEXT_PUBLIC_METRICS_REFRESH_INTERVAL` | Metrics refresh interval (ms) |

## License

MIT