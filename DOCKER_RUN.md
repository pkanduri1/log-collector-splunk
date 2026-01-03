# Running Log Collector in Docker

This guide explains how to build and run the application using Docker Compose.

## Prerequisites
- [Docker](https://docs.docker.com/get-docker/) installed on your machine.
- [Splunk](https://www.splunk.com/) instance running locally or accessible via network.

## Configuration

The application requires an OpenAI API Key. You can pass this as an environment variable when running the container.

## Steps

### 1. Build and Run
Navigate to the project root and run:

```bash
OPENAI_API_KEY=your-api-key-here docker-compose up --build
```
> Replace `your-api-key-here` with your actual OpenAI API Key.

### 2. Access the Application
- **Frontend (UI)**: Open [http://localhost:5173](http://localhost:5173) in your browser.
- **Backend (API)**: Accessible at [http://localhost:8080](http://localhost:8080).

### 3. Verify Connection
1.  Open the Chat Interface.
2.  Type a question like "Show me lines with error".
3.  If successful, the bot will return a generated SPL query and a summary.

## Troubleshooting

### Connectivity to Splunk
The container uses `host.docker.internal` to connect to Splunk running on your host machine.
If you are on Linux, you might need to add the following to `docker-compose.yml` under `extra_hosts`:
```yaml
extra_hosts:
  - "host.docker.internal:host-gateway"
```

### Rebuilding
If you make code changes, ensure you rebuild the images:
```bash
docker-compose up --build --force-recreate
```
