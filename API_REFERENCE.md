# Manga Universal Backend - API Reference

**Base URL:** 
- Local: `http://localhost:8080`
- Production: Set via `APP_PROXY_BASE_URL` environment variable (e.g., `https://your-domain.com`)

> **Note:** In production, make sure to set the `APP_PROXY_BASE_URL` environment variable to your server's public URL. This ensures that proxy URLs for images are generated correctly.

## Table of Contents

- [Health Check Endpoints](#health-check-endpoints)
- [Manga Endpoints](#manga-endpoints)
- [Response Models](#response-models)
- [Error Handling](#error-handling)

---

## Health Check Endpoints

### Health Check

Simple health check endpoint used for monitoring and keeping the server awake.

**Endpoint:** `GET /healthcheck`

**Example Request:**

```bash
curl http://localhost:8080/healthcheck
```

**Success Response (200 OK):**

```json
{
  "status": "OK",
  "message": "Server is running",
  "timestamp": "2025-12-11 10:30:00",
  "uptime": "healthy"
}
```

**Purpose:** This endpoint is automatically pinged every 10 minutes by GitHub Actions to prevent the server from sleeping on free-tier hosting platforms.

---

### Detailed Health Status

Get detailed server health information including memory usage and system info.

**Endpoint:** `GET /healthcheck/status`

**Example Request:**

```bash
curl http://localhost:8080/healthcheck/status
```

**Success Response (200 OK):**

```json
{
  "status": "OK",
  "timestamp": "2025-12-11 10:30:00",
  "server": "manga-universal-backend",
  "version": "1.0.0",
  "memory": {
    "used": "128 MB",
    "max": "512 MB"
  },
  "system": {
    "javaVersion": "21.0.9",
    "osName": "Linux"
  }
}
```

---

## Manga Endpoints

### 1. Get Popular Manga

Get a paginated list of popular manga sorted by follower count.

**Endpoint:** `GET /api/manga/popular`

**Query Parameters:**
| Parameter | Type | Default | Required | Description |
|-----------|------|---------|----------|-------------|
| limit | integer | 20 | No | Number of results per page (1-100) |
| offset | integer | 0 | No | Pagination offset (≥0) |
| order | string | "desc" | No | Sort order: "asc" or "desc" |
| sortBy | string | "followedCount" | No | Field to sort by (e.g., "followedCount", "createdAt") |

**Example Request:**

```javascript
// Get top 10 popular manga
fetch("http://localhost:8080/api/manga/popular?limit=10&offset=0")
  .then((response) => response.json())
  .then((data) => console.log(data));

// Get next page with custom sorting
fetch("http://localhost:8080/api/manga/popular?limit=20&offset=20&order=asc")
  .then((response) => response.json())
  .then((data) => console.log(data));
```

**Success Response (200 OK):**

```json
{
  "total": 89195,
  "limit": 10,
  "offset": 0,
  "results": [
    {
      "id": "32d76d19-8a05-4db0-9fc2-e0b0648fe9d0",
      "title": "Na Honjaman Level-Up",
      "description": "10 years ago, after \"the Gate\" that connected the real world...",
      "followers": 316046,
      "coverUrl": "http://localhost:8080/proxy/mangadex/cover/32d76d19-8a05-4db0-9fc2-e0b0648fe9d0/e6583e52-1125-4c50-8db4-e8d6cf3fb144"
    },
    {
      "id": "aa6c76f7-5f5f-46b6-a800-911145f81b9b",
      "title": "Sono Bisque Doll wa Koi o Suru",
      "description": "Wakana Gojou is a fifteen year old high-school boy...",
      "followers": 245847,
      "coverUrl": "http://localhost:8080/proxy/mangadex/cover/aa6c76f7-5f5f-46b6-a800-911145f81b9b/6ce2e9a4-deb7-4646-b479-cd658985a3e8"
    }
  ]
}
```

**Error Responses:**

**400 Bad Request - Invalid Limit:**

```json
{
  "error": "Limit must be between 1 and 100",
  "status": 400
}
```

**400 Bad Request - Invalid Offset:**

```json
{
  "error": "Offset must be 0 or greater",
  "status": 400
}
```

**400 Bad Request - Invalid Order:**

```json
{
  "error": "Order must be 'asc' or 'desc'",
  "status": 400
}
```

**500 Internal Server Error:**

```json
{
  "error": "Error fetching popular manga: <error details>",
  "status": 500
}
```

**Features:**

- ✅ Fetches data from MangaDex API with follower statistics
- ✅ Spring WebClient with reactive HTTP calls
- ✅ 30-second timeout with proper error handling
- ✅ Graceful handling of missing fields (description, cover)
- ✅ Proxy URL support for cover images
- ✅ Clean layered architecture (Controller → Service → DTOs)

---

### 2. Search Manga

Search for manga by title.

**Endpoint:** `GET /api/manga/search`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| q | string | Yes | The manga title to search for |

**Example Request:**

```javascript
fetch("http://localhost:8080/api/manga/search?q=One%20Piece")
  .then((response) => response.json())
  .then((data) => console.log(data));
```

**Success Response (200 OK):**

```json
{
  "results": [
    {
      "id": "a1c7c817-4e59-43b7-9365-09675a149a6f",
      "title": "One Piece",
      "description": "Gol D. Roger was known as the Pirate King...",
      "status": "ongoing",
      "year": 1997,
      "cover": "https://uploads.mangadex.org/covers/a1c7c817-4e59-43b7-9365-09675a149a6f/cover.jpg"
    }
  ]
}
```

**Error Response (400 Bad Request):**

```json
{
  "error": "Missing query parameter 'q'",
  "status": 400
}
```

---

### 3. Get Manga Details

Get detailed information about a specific manga.

**Endpoint:** `GET /api/manga/{id}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string | Yes | The MangaDex manga ID |

**Example Request:**

```javascript
const mangaId = "a1c7c817-4e59-43b7-9365-09675a149a6f";
fetch(`http://localhost:8080/api/manga/${mangaId}`)
  .then((response) => response.json())
  .then((data) => console.log(data));
```

**Success Response (200 OK):**

```json
{
  "id": "a1c7c817-4e59-43b7-9365-09675a149a6f",
  "title": "One Piece",
  "description": "Gol D. Roger was known as the Pirate King...",
  "status": "ongoing",
  "year": 1997,
  "cover": "https://uploads.mangadex.org/covers/a1c7c817-4e59-43b7-9365-09675a149a6f/cover.jpg",
  "tags": ["Action", "Adventure", "Comedy", "Drama", "Fantasy"],
  "authors": ["Oda Eiichiro"],
  "artists": ["Oda Eiichiro"]
}
```

**Error Response (404 Not Found):**

```json
{
  "error": "Manga not found",
  "status": 404
}
```

---

### 4. Get Manga Chapters

Get the list of chapters for a specific manga.

**Endpoint:** `GET /api/manga/{id}/chapters`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string | Yes | The MangaDex manga ID |

**Query Parameters:**

- Filters English translations only
- Orders chapters in ascending order
- Limit: 500 chapters

**Example Request:**

```javascript
const mangaId = "a1c7c817-4e59-43b7-9365-09675a149a6f";
fetch(`http://localhost:8080/api/manga/${mangaId}/chapters`)
  .then((response) => response.json())
  .then((data) => console.log(data));
```

**Success Response (200 OK):**

```json
{
  "chapters": [
    {
      "id": "e199f8e6-4eb4-4553-9b9e-0e3f2d7e7c5d",
      "chapter": "1",
      "title": "Romance Dawn",
      "volume": "1",
      "pages": 53,
      "translatedLanguage": "en",
      "publishAt": "2021-04-20T12:00:00+00:00",
      "scanlationGroup": "Example Scans"
    },
    {
      "id": "f3a9d2c1-5eb4-4553-9b9e-1e4f3d8e8d6e",
      "chapter": "2",
      "title": "The Man with the Straw Hat",
      "volume": "1",
      "pages": 20,
      "translatedLanguage": "en",
      "publishAt": "2021-04-21T12:00:00+00:00",
      "scanlationGroup": "Example Scans"
    }
  ]
}
```

---

### 5. Get Chapter Pages

Get the page URLs for a specific chapter.

**Endpoint:** `GET /api/manga/chapter/{chapterId}/pages`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| chapterId | string | Yes | The MangaDex chapter ID |

**Example Request:**

```javascript
const chapterId = "e199f8e6-4eb4-4553-9b9e-0e3f2d7e7c5d";
fetch(`http://localhost:8080/api/manga/chapter/${chapterId}/pages`)
  .then((response) => response.json())
  .then((data) => console.log(data));
```

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| useProxy | boolean | true | If true, returns proxy URLs; if false, returns direct MangaDex URLs |

**Success Response (200 OK) - With Proxy (Recommended):**

```json
{
  "pages": [
    "http://localhost:8080/proxy/mangadex/e199f8e6-4eb4-4553-9b9e-0e3f2d7e7c5d/x1-abc123.jpg",
    "http://localhost:8080/proxy/mangadex/e199f8e6-4eb4-4553-9b9e-0e3f2d7e7c5d/x2-def456.jpg",
    "http://localhost:8080/proxy/mangadex/e199f8e6-4eb4-4553-9b9e-0e3f2d7e7c5d/x3-ghi789.jpg"
  ],
  "useProxy": true
}
```

**Success Response (200 OK) - Direct URLs:**

```json
{
  "pages": [
    "https://uploads.mangadex.org/data/abc123/x1-page1.jpg",
    "https://uploads.mangadex.org/data/abc123/x2-page2.jpg",
    "https://uploads.mangadex.org/data/abc123/x3-page3.jpg"
  ],
  "useProxy": false
}
```

**Error Response (400 Bad Request):**

```json
{
  "error": "Missing chapter ID",
  "status": 400
}
```

---

### 6. Proxy Manga Page Image

Proxy endpoint to fetch manga page images server-side, bypassing MangaDex anti-hotlinking. This prevents the "Read on MangaDex" placeholder from appearing.

**Endpoint:** `GET /proxy/mangadex/{chapterId}/{filename}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| chapterId | string | Yes | The MangaDex chapter ID |
| filename | string | Yes | The image filename (e.g., "x1-abc123.jpg") |

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| dataSaver | boolean | false | If true, fetches compressed images for lower bandwidth |

**Example Request:**

```javascript
const chapterId = "e199f8e6-4eb4-4553-9b9e-0e3f2d7e7c5d";
const filename = "x1-abc123.jpg";

// Display in an <img> tag
<img
  src={`http://localhost:8080/proxy/mangadex/${chapterId}/${filename}`}
  alt="Page"
/>;

// Or fetch directly
fetch(`http://localhost:8080/proxy/mangadex/${chapterId}/${filename}`)
  .then((response) => response.blob())
  .then((blob) => {
    const imageUrl = URL.createObjectURL(blob);
    document.getElementById("manga-page").src = imageUrl;
  });
```

**Success Response (200 OK):**

Returns the actual image bytes with appropriate headers:

- `Content-Type: image/jpeg`, `image/png`, or `image/webp`
- `Cache-Control: public, max-age=86400` (24 hours browser cache)
- `Content-Length: <size>`

**Error Response (404 Not Found):**

```json
{
  "error": "Filename not found in chapter data",
  "status": 404
}
```

**Error Response (502 Bad Gateway):**

```json
{
  "error": "Failed to connect to MangaDex servers",
  "status": 502
}
```

**Features:**

- ✅ Server-side image fetching with proper headers (`Referer`, `User-Agent`)
- ✅ Bypasses MangaDex anti-hotlinking protection
- ✅ At-Home server data cached for 3 minutes
- ✅ Image bytes streamed directly (not cached)
- ✅ Browser-side caching enabled (24 hours)
- ✅ Automatic content-type detection
- ✅ Support for data-saver mode (compressed images)

---

## Response Models

### Popular Manga Response

```typescript
interface PopularMangaResponse {
  total: number; // Total number of manga in database
  limit: number; // Number of results returned
  offset: number; // Pagination offset
  results: MangaItem[]; // Array of manga items
}

interface MangaItem {
  id: string;
  title: string;
  description: string | null; // May be null if not available
  followers: number | null; // Number of followers (may be null)
  coverUrl: string | null; // Proxy URL for cover image (may be null)
}
```

### Manga Search Result

```typescript
interface MangaSearchResult {
  id: string;
  title: string;
  description: string;
  status: "ongoing" | "completed" | "hiatus" | "cancelled";
  year: number;
  cover: string; // Full cover image URL
}
```

### Manga Detail

```typescript
interface MangaDetail {
  id: string;
  title: string;
  description: string;
  status: "ongoing" | "completed" | "hiatus" | "cancelled";
  year: number;
  cover: string; // Full cover image URL
  tags: string[];
  authors: string[];
  artists: string[];
}
```

### Chapter

```typescript
interface Chapter {
  id: string;
  chapter: string; // Chapter number
  title: string;
  volume: string;
  pages: number;
  translatedLanguage: string; // "en"
  publishAt: string; // ISO 8601 date
  scanlationGroup: string;
}
```

---

## Error Handling

All endpoints return consistent error responses:

```typescript
interface ErrorResponse {
  error: string; // Human-readable error message
  status: number; // HTTP status code
}
```

**Common Status Codes:**

- `200` - Success
- `400` - Bad Request (missing or invalid parameters)
- `404` - Not Found (resource doesn't exist)
- `500` - Internal Server Error (server-side error)

---

## Usage Examples

### React/TypeScript Example

```typescript
// services/mangaApi.ts
const BASE_URL = "http://localhost:8080/api";

export const mangaApi = {
  // Get popular manga
  getPopularManga: async (
    limit: number = 20,
    offset: number = 0,
    order: "asc" | "desc" = "desc",
    sortBy: string = "followedCount"
  ) => {
    const response = await fetch(
      `${BASE_URL}/manga/popular?limit=${limit}&offset=${offset}&order=${order}&sortBy=${sortBy}`
    );
    if (!response.ok) throw new Error("Failed to fetch popular manga");
    return response.json();
  },

  // Search manga
  searchManga: async (query: string) => {
    const response = await fetch(
      `${BASE_URL}/manga/search?q=${encodeURIComponent(query)}`
    );
    if (!response.ok) throw new Error("Search failed");
    return response.json();
  },

  // Get manga details
  getMangaDetails: async (mangaId: string) => {
    const response = await fetch(`${BASE_URL}/manga/${mangaId}`);
    if (!response.ok) throw new Error("Failed to fetch manga details");
    return response.json();
  },

  // Get manga chapters
  getMangaChapters: async (mangaId: string) => {
    const response = await fetch(`${BASE_URL}/manga/${mangaId}/chapters`);
    if (!response.ok) throw new Error("Failed to fetch chapters");
    return response.json();
  },

  // Get chapter pages
  getChapterPages: async (chapterId: string) => {
    const response = await fetch(`${BASE_URL}/chapter/${chapterId}/pages`);
    if (!response.ok) throw new Error("Failed to fetch pages");
    return response.json();
  },
};
```

### Vanilla JavaScript Example

```javascript
// Get popular manga
async function getPopularManga(limit = 20, offset = 0) {
  try {
    const response = await fetch(
      `http://localhost:8080/api/manga/popular?limit=${limit}&offset=${offset}`
    );
    const data = await response.json();

    if (response.ok) {
      console.log(`Total manga: ${data.total}`);
      console.log(`Showing ${data.results.length} results`);
      return data.results;
    } else {
      console.error(data.error);
      return [];
    }
  } catch (error) {
    console.error("Failed to fetch popular manga:", error);
    return [];
  }
}

// Search for manga
async function searchManga(query) {
  try {
    const response = await fetch(
      `http://localhost:8080/api/manga/search?q=${encodeURIComponent(query)}`
    );
    const data = await response.json();

    if (response.ok) {
      return data.results;
    } else {
      console.error(data.error);
      return [];
    }
  } catch (error) {
    console.error("Error searching manga:", error);
    return [];
  }
}

// Get manga details and chapters
async function loadMangaReader(mangaId) {
  try {
    // Fetch manga details
    const detailsResponse = await fetch(
      `http://localhost:8080/api/manga/${mangaId}`
    );
    const mangaDetails = await detailsResponse.json();

    // Fetch chapters
    const chaptersResponse = await fetch(
      `http://localhost:8080/api/manga/${mangaId}/chapters`
    );
    const chaptersData = await chaptersResponse.json();

    return {
      manga: mangaDetails,
      chapters: chaptersData.chapters,
    };
  } catch (error) {
    console.error("Error loading manga:", error);
    return null;
  }
}

// Get chapter pages
async function loadChapterPages(chapterId) {
  try {
    const response = await fetch(
      `http://localhost:8080/api/chapter/${chapterId}/pages`
    );
    const data = await response.json();

    if (response.ok) {
      return data.pages;
    } else {
      console.error(data.error);
      return [];
    }
  } catch (error) {
    console.error("Error loading chapter pages:", error);
    return [];
  }
}
```

### Axios Example

```javascript
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

// Search manga
export const searchManga = (query) =>
  api.get("/manga/search", { params: { q: query } });

// Get manga details
export const getMangaDetails = (mangaId) => api.get(`/manga/${mangaId}`);

// Get manga chapters
export const getMangaChapters = (mangaId) =>
  api.get(`/manga/${mangaId}/chapters`);

// Get chapter pages
export const getChapterPages = (chapterId) =>
  api.get(`/chapter/${chapterId}/pages`);
```

---

## CORS Configuration

The API has CORS enabled for all origins (`*`). This means you can call the API from any frontend application during development.

**Allowed:**

- Origins: `*` (all)
- Methods: `GET`, `POST`, `OPTIONS`
- Headers: `*` (all)

---

## Notes

1. **Rate Limiting**: The backend proxies requests to MangaDex API. Be mindful of their rate limits.
2. **Language**: Chapters are filtered to English only (`translatedLanguage[]=en`).
3. **Chapter Limit**: Maximum 500 chapters per manga.
4. **Cover Images**: Cover URLs are fully constructed and ready to use in `<img>` tags.
5. **Encoding**: Always URL-encode search queries using `encodeURIComponent()`.
