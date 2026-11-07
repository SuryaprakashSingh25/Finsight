# FinSight — Institutional AI Financial Research Engine

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring_AI-1.1.8-blue.svg?style=flat-square)](https://spring.io/projects/spring-ai)
[![Angular](https://img.shields.io/badge/Angular-18-red.svg?style=flat-square&logo=angular)](https://angular.dev/)
[![pgvector](https://img.shields.io/badge/PostgreSQL-17_w%2F_pgvector-blue.svg?style=flat-square&logo=postgresql)](https://github.com/pgvector/pgvector)
[![Redis](https://img.shields.io/badge/Redis-7_Alpine-crimson.svg?style=flat-square&logo=redis)](https://redis.io/)

**FinSight** is an enterprise-grade AI financial intelligence platform that generates quantitative, grounded executive tear sheets, financial ratios, and research reports from multi-page corporate SEC 10-K disclosures, earnings filings, and real-time market data.

By pairing local document vector retrieval (RAG) with the **Model Context Protocol (MCP)**, deterministic financial mathematics, and real-time market feeds, FinSight eliminates LLM numerical hallucination while preserving sub-second analytical workflows via multi-tenant caching.

---

## Key Highlights & Architectural Pillars

- **Deterministic Financial Intelligence:** LLMs synthesize text and narrative, but all underlying quantitative formulas (Altman Z-Score, DuPont 3-way analysis, Debt-to-Equity, Margins) are strictly executed by deterministic MCP tools over port `8081`.
- **Model Context Protocol (MCP):** A dedicated, standalone MCP server exposing real-time feeds (`getLiveMarketQuote`, `getRecentCompanyNews`) and mathematical engines via standard protocol interfaces.
- **Resilient Multi-Provider Fallback Routing:** Automatic failover across OpenRouter (`openrouter/free`), Google Gemini, and Groq, handling rate limits, context saturation, and API throttling transparently.
- **Strict Multi-Tenant Isolation:** User-level document isolation at the database, embedding, and vector-search layers via PostgreSQL JSONB metadata scoping (`uploaded_by`).
- **Sub-20ms Redis Caching:** Cache-aside design for generated tear sheets keyed by tenant, company, and fiscal period (`finsight:tearsheet:{userId}:{company}:{period}`), auto-evicted on catalog modification.
- **Real-Time Streaming UX:** End-to-end token and status event streaming over Server-Sent Events (SSE) with native `ReadableStream` consumption in an Angular frontend.
- **Stateless JWT Security:** Full authentication lifecycle backed by BCrypt password hashing, Spring Security 6, and functional Angular HTTP interceptors.

---

## System Architecture

```text
                               +--------------------------------------------+
                               |              Angular Frontend              |
                               |    (Signals, SSE Stream, Workspace UI)     |
                               +--------------------------------------------+
                                                     |
                                     JWT Auth / REST / SSE Stream
                                                     v
                               +--------------------------------------------+
                               |            Spring Boot Backend             |
                               |  - Spring Security (Stateless JWT Filter)  |
                               |  - Multi-Model Router (OpenRouter/Gemini)  |
                               |  - Chunking Engine (PdfBox / TokenSplit)   |
                               |  - TearSheet & RAG Retrieval Services      |
                               +--------------------------------------------+
                                        /            |             \
                       Cache-Aside Read        Vector Search      Tool Calls
                             /                       |          (Streamable HTTP)
                            v                        v                 v
               +-------------------+    +-------------------+    +-------------------+
               |   Redis Cache     |    |   PostgreSQL 17   |    |    MCP Server     |
               | (TearSheets/TTLs) |    |   w/ pgvector     |    |   (Port 8081)     |
               |  < 20ms hits      |    |  (Tenant Chunks)  |    | - Yahoo Finance   |
               +-------------------+    +-------------------+    | - Financial RSS   |
                                                                 | - Altman Z-Score  |
                                                                 | - DuPont Engine   |
                                                                 +-------------------+
```

---

## Technology Stack

### Backend
- **Core Runtime:** Java 21 (LTS)
- **Framework:** Spring Boot 3.5, Spring Security 6
- **AI & Protocol Integration:** Spring AI 1.1.8, Spring AI MCP Client
- **Data Persistence:** Spring Data JPA / `JdbcClient`, PostgreSQL 17 with `pgvector`
- **Caching & Caching Engine:** Spring Data Redis, Redis 7 Alpine
- **PDF & Vector Pipelines:** Apache PDFBox, Rome Tools (RSS), Jackson JSR310

### Frontend
- **Framework:** Angular (Standalone Component Architecture)
- **State & Reactivity:** Angular Signals, RxJS
- **Communication:** Native Fetch with `ReadableStream` (SSE), HttpClient with functional `HttpInterceptorFn`
- **Styling:** Modular SCSS, Institutional dark/light workspace theme

### Infrastructure
- Docker & Docker Compose (`pgvector/pgvector:pg17`, `redis:7-alpine`)

---

## Directory Structure

```text
finsight/
├── backend/
│   ├── src/main/java/com/finsight/backend/
│   │   ├── agent/             # MCP Chat Client and Agent Configurations
│   │   ├── ai/                # Multi-Provider Model Router & Fallback
│   │   ├── auth/              # JWT Service, User Entity, Security Filters
│   │   ├── catalog/           # Company Catalog & Tenant Aggregations
│   │   ├── config/            # Redis & Jackson Serialization Setup
│   │   ├── mcp/               # McpAgentService & Tool Callback Bindings
│   │   ├── overview/          # Dashboard Stats & Real-Time Query Projections
│   │   ├── rag/               # Chunking, Ingestion, & User-Scoped Search
│   │   └── tearsheet/         # TearSheet Orchestrator & Cache-Aside Layer
│   └── src/main/resources/
│       └── application.yml    # App profiles, LLM options, and DB config
├── frontend/
│   ├── src/app/
│   │   ├── core/              # AuthService, Interceptors, API Clients
│   │   ├── features/          # Overview, Companies, Documents, Research
│   │   └── layout/            # Sidebar, Topbar, Modals, Shell
└── docker-compose.yml         # Container definitions for Postgres & Redis
```

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK):** Version 21 or higher
- **Node.js & npm:** Node v20+ / npm v10+
- **Docker & Docker Compose:** Installed and daemon running
- **API Keys:** An active API key from [OpenRouter](https://openrouter.ai/) (or [Google AI Studio](https://aistudio.google.com/) / [Groq Console](https://console.groq.com/))

---

### Step 1: Clone the Repository

```bash
git clone https://github.com/SuryaprakashSingh25/finsight.git
cd finsight
```

---

### Step 2: Spin Up Infrastructure Containers

Launch PostgreSQL (with `pgvector`) and Redis:

```bash
docker compose up -d
```

Verify both containers are healthy:
```bash
docker compose ps
```

---

### Step 3: Configure Environment Variables

Export the required API keys and connection parameters:

```bash
# OpenRouter (Default Primary Provider)
export OPENROUTER_API_KEY="your-openrouter-key"

# Optional Fallback Providers
export GROQ_API_KEY="your-groq-key"
export GEMINI_API_KEY="your-gemini-key"

# Database & Cache (Defaults match docker-compose.yml)
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/finsight"
export SPRING_DATASOURCE_USERNAME="finsight"
export SPRING_DATASOURCE_PASSWORD="finsight"
export REDIS_HOST="localhost"
export REDIS_PORT="6379"
```

---

### Step 4: Run the Backend

```bash
cd backend
mvn clean spring-boot:run
```
*The Spring Boot API will initialize on `http://localhost:8080`.*

---

### Step 5: Launch the Angular Frontend

```bash
cd ../frontend
npm install
npm start
```
*The web client will compile and serve at `http://localhost:4200`.*

---

## Model Context Protocol (MCP) Server Specification

FinSight delegates all real-time market data retrieval and quantitative math to a standalone MCP microservice running on port `8081` via Spring AI Community MCP annotations (`@McpTool`).

By isolating calculations from the generative model, the architecture prevents arithmetic hallucinations and ensures reproducible financial metrics.

---

### Registered Tool Catalog

| Tool Name | Parameters | Return Type | Mathematical Formula / Implementation | Purpose & Valuation Context |
| :--- | :--- | :--- | :--- | :--- |
| `getLiveMarketQuote` | `symbol: String` | `String` | Yahoo Finance Chart API (`/v8/finance/chart/{symbol}`) | Fetches real-time price, 52-week range, currency, trading exchange, and day volume. |
| `getRecentCompanyNews` | `companyOrTicker: String` | `String` | Google News Financial RSS Feed (`news.google.com/rss`) | Aggregates top 5 recent earnings and market headlines with timestamped source links. |
| `altmanZScore` | `workingCapital`, `retainedEarnings`, `ebit`, `marketValueOfEquity`, `sales`, `totalAssets`, `totalLiabilities` | `double` | $1.2X_1 + 1.4X_2 + 3.3X_3 + 0.6X_4 + 0.999X_5$ | Assesses credit risk and bankruptcy likelihood (Safe $> 2.99$, Grey $1.81\text{–}2.99$, Distress $< 1.81$). |
| `dupontAnalysis` | `netIncome`, `revenue`, `totalAssets`, `shareholdersEquity` | `String` | $\text{Margin} \times \text{Asset Turnover} \times \text{Equity Multiplier}$ | Deconstructs ROE into operating margin efficiency, asset utilization, and financial leverage. |
| `debtToEquity` | `totalLiabilities`, `totalStockholdersEquity` | `double` | $\frac{\text{Total Liabilities}}{\text{Stockholders' Equity}}$ | Evaluates capital structure solvency against sector leverage thresholds. |
| `freeCashFlow` | `operatingCashFlow`, `capitalExpenditures` | `double` | $\text{Operating Cash Flow} - \text{CapEx}$ | Computes discretionary cash available for debt repayment, dividends, or reinvestment. |
| `profitMargin` | `income`, `totalRevenue` | `double` | $\left(\frac{\text{Net Income}}{\text{Total Revenue}}\right) \times 100$ | Calculates baseline operating or net profitability percentage. |
| `percentageGrowth` | `current`, `previous` | `double` | $\left(\frac{\text{Current} - \text{Previous}}{\text{Previous}}\right) \times 100$ | Measures YoY or QoQ revenue and balance sheet item expansion. |
| `cagr` | `beginningValue`, `endingValue`, `periods` | `double` | $\left[\left(\frac{\text{Ending}}{\text{Beginning}}\right)^{\frac{1}{\text{Periods}}} - 1\right] \times 100$ | Calculates annualized multi-year compound growth across fiscal cycles. |

---

## Multi-Tenant Data Isolation Protocol

All corporate documents uploaded through the `/api/documents` endpoint undergo parsing via `PdfDocumentLoader` and `TokenTextSplitter`. Every created chunk is stamped with the authenticated user's ID before insertion into the vector space:

```json
{
  "company": "NVIDIA",
  "document": "NVDA-FY2024-10K.pdf",
  "year": 2024,
  "chunk_index": 12,
  "uploaded_by": "b8f3214e-953b-48ae-8b29-e137b7e51c89"
}
```

Every RAG vector query and catalog aggregation strictly applies:
```sql
WHERE metadata->>'uploaded_by' = :authenticatedUserId
```
Ensuring zero context leakage across different analyst accounts.

---

## Cache Invalidation Strategy

```text
Client Request: Generate Tear Sheet for NVDA (FY2024)
                           │
                           ▼
          Check Redis Key: finsight:tearsheet:{userId}:nvda:2024
                 /                     \
       [HIT (< 20ms)]              [MISS]
             │                       │
             ▼                       ▼
    Return JSON Document     Run RAG (topK=3) -> MCP Agent Synthesis
                             -> Store in Redis (TTL: 6h)
                             -> Return to Client
```

- When an analyst deletes a company filing via `CompanyCatalogService`, all matching keys matching `finsight:tearsheet:{userId}:{company}:*` are purged via Redis pattern evaluation.

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
