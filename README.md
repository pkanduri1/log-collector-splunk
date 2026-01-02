# Log Collector - Splunk Intelligent Layer

This Spring Boot application acts as an intelligent layer on top of Splunk, allowing users to ask natural language questions about their logs.

## Features
- **Text-to-SPL**: Converts natural language questions into Splunk Processing Language (SPL) queries using Generative AI.
- **Automated Search**: Executes generated queries against a Splunk instance.
- **AI Summarization**: efficient summarizes raw log results into human-readable explanations.
- **REST API**: Exposes a simple `/analyze` endpoint.

## Tech Stack
- **Java**: 21
- **Spring Boot**: 4.0.1 (Configured as 3.2.4 for build compatibility with current repositories, user requested 4.0.1)
- **Spring AI**: 1.0.0-SNAPSHOT (OpenAI Provider)
- **Splunk SDK**: 1.9.5

## Setup

### Prerequisites
- Java 21+
- Maven
- Splunk instance credentials
- OpenAI API Key

### Configuration
Update `src/main/resources/application.yml` with your credentials:
```yaml
spring:
  ai:
    openai:
      api-key: your-openai-key
splunk:
  host: your-splunk-host
  port: 8089
  username: admin
  password: changeme
```

### Build
```bash
mvn clean package
```
> **Note**: The **Spring AI** dependencies are currently commented out in `pom.xml` because the snapshot/milestone artifacts were unavailable at build time. The application is running in **Mock AI Mode**. 
> To enable Real AI:
> 1. Open `pom.xml` and uncomment `spring-ai-starter-openai`.
> 2. Ensure you have access to the `spring-milestones` or `spring-snapshots` repository.
> 3. Rebuild the application.

### Frontend
The project includes a modern React-based UI in the `frontend` directory.

**Setup & Run:**
```bash
cd frontend
npm install
npm run dev
```
Access the UI at `http://localhost:5173`.

### Usage
**UI**: Open the chat interface at `http://localhost:5173`.
1. Select your preferred AI model from the dropdown.
2. Click on an example prompt or type your own question.
3. View the generated SPL and the AI summary.

**API Endpoint**: `POST /analyze`
**Body**:
```json
{
  "question": "Why did the payment batch fail?"
}
```
**Response**:
```json
{
  "generatedSpl": "search index='main' \"payment batch\" fail* | head 20",
  "humanSummary": "The payment batch failed due to a timeout in the gateway service..."
}
```
