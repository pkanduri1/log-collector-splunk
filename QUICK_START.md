# Quick Start Guide - Running with Docker

## The Issue You Encountered

The application was trying to connect to **Ollama** (a local AI service) which wasn't running, causing the `Network is unreachable` error. I've fixed the configuration to use **OpenAI** by default.

## ✅ Fixed Configuration

Updated `docker-compose.yml` to use OpenAI as the default AI provider instead of Gemini/Ollama.

## 🚀 How to Run

### Option 1: Using the docker-run.sh script (Recommended)

```bash
# Set your OpenAI API key
export OPENAI_API_KEY='sk-your-actual-key-here'

# Build and run
./docker-run.sh -b
```

### Option 2: Using docker compose directly

```bash
# Set your OpenAI API key
export OPENAI_API_KEY='sk-your-actual-key-here'

# Build and run
docker compose up --build
```

### Option 3: One-liner (without setting environment variable)

```bash
OPENAI_API_KEY='sk-your-actual-key-here' docker compose up --build
```

## 📍 Access the Application

Once running successfully, you'll see:
- **Frontend**: http://localhost:5173
- **Backend**: http://localhost:8081

## ✅ Success Indicators

You should see these log messages:
```
✓ Wait... disabling SSL verification for development environment.
✓ Tomcat started on port 8080 (http) with context path ''
✓ Started LogCollectorApplication in X.XXX seconds
```

**No more Ollama connection errors!**

## 🔧 Troubleshooting

### If you still see errors:

1. **Verify your API key is set:**
   ```bash
   echo $OPENAI_API_KEY
   ```
   Should output your key starting with `sk-`

2. **Check which AI provider is being used:**
   Look for this in the logs when the container starts

3. **Use a different AI provider:**
   ```bash
   # Use Gemini instead
   AI_PROVIDER=gemini GOOGLE_AI_GEMINI_API_KEY='your-key' docker compose up --build
   
   # Use Anthropic instead
   AI_PROVIDER=anthropic ANTHROPIC_API_KEY='your-key' docker compose up --build
   ```

## 🛑 Stop the Application

```bash
./docker-run.sh -s
# or
docker compose down
```

## 📋 Useful Commands

```bash
# View logs
./docker-run.sh -l

# Run in background
./docker-run.sh -d

# Clean up everything
./docker-run.sh -c

# Force rebuild without cache
./docker-run.sh --no-cache
```
