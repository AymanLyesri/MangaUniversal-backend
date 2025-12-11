# Manga Universal Backend - API Reference

Base URL: `http://localhost:8080`

## Table of Contents

- [Manga Endpoints](#manga-endpoints)
- [Response Models](#response-models)
- [Error Handling](#error-handling)

---

## Manga Endpoints

### 1. Search Manga

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

### 2. Get Manga Details

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

### 3. Get Manga Chapters

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

### 4. Get Chapter Pages

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

**Success Response (200 OK):**

```json
{
  "pages": [
    "https://uploads.mangadex.org/data/abc123/x1-page1.jpg",
    "https://uploads.mangadex.org/data/abc123/x2-page2.jpg",
    "https://uploads.mangadex.org/data/abc123/x3-page3.jpg"
  ]
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

## Response Models

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
