# Manga Universal Backend

A **Spring Boot** serverless backend deployed on Vercel that wraps the MangaDex API into clean, simplified endpoints for an Angular manga reader application.

## 📋 Features

- **Spring Boot 3.2**: Modern Java framework with auto-configuration
- **RESTful API**: Clean REST controllers with proper HTTP methods
- **Dependency Injection**: Spring's IoC container for better architecture
- **JSON Processing**: Jackson integration via Spring Boot
- **CORS Enabled**: All endpoints include proper CORS headers
- **Error Handling**: Comprehensive error handling with meaningful messages
- **Easy Testing**: Built-in Spring Boot testing support

## 🏗️ Project Structure

```
manga-universal-backend/
├── src/
│   └── main/
│       ├── java/com/mangareader/
│       │   ├── MangaUniversalBackendApplication.java  # Main application
│       │   ├── controller/
│       │   │   ├── MangaController.java               # Manga endpoints
│       │   │   └── ChapterController.java             # Chapter endpoints
│       │   └── util/
│       │       ├── HttpClient.java                    # HTTP client
│       │       └── MangaDexParser.java                # JSON parser
│       └── resources/
│           └── application.properties                  # Configuration
├── pom.xml                                             # Maven + Spring Boot
├── vercel.json                                         # Vercel deployment
└── README.md
```

## 🚀 API Endpoints

### 1. Search Manga

**Endpoint**: `GET /api/manga/search?q=<title>`

Search for manga by title.

**Example Request**:

```
GET /api/manga/search?q=one%20piece
```

**Response**:

```json
{
  "results": [
    {
      "id": "a1c7c817-4e59-43b7-9365-09675a149a6f",
      "title": "One Piece",
      "description": "Gol D. Roger was known as the Pirate King...",
      "cover": "https://uploads.mangadex.org/covers/a1c7c817-4e59-43b7-9365-09675a149a6f/cover.jpg",
      "tags": ["Action", "Adventure", "Comedy", "Drama", "Fantasy"]
    }
  ]
}
```

---

### 2. Get Manga Details

**Endpoint**: `GET /api/manga/:id`

Fetch detailed information about a specific manga.

**Example Request**:

```
GET /api/manga/a1c7c817-4e59-43b7-9365-09675a149a6f
```

**Response**:

```json
{
  "id": "a1c7c817-4e59-43b7-9365-09675a149a6f",
  "title": "One Piece",
  "description": "Gol D. Roger was known as the Pirate King...",
  "cover": "https://uploads.mangadex.org/covers/a1c7c817-4e59-43b7-9365-09675a149a6f/cover.jpg",
  "tags": ["Action", "Adventure", "Comedy", "Drama", "Fantasy"]
}
```

---

### 3. List Chapters

**Endpoint**: `GET /api/manga/:id/chapters`

Get all chapters for a manga (English only, sorted ascending).

**Example Request**:

```
GET /api/manga/a1c7c817-4e59-43b7-9365-09675a149a6f/chapters
```

**Response**:

```json
{
  "chapters": [
    {
      "id": "e1f3c817-4e59-43b7-9365-09675a149a6f",
      "number": "1",
      "title": "Romance Dawn"
    },
    {
      "id": "f2a4d928-5f60-44c8-0476-10786b250b7g",
      "number": "2",
      "title": "They Call Him Straw Hat Luffy"
    }
  ]
}
```

---

### 4. Get Chapter Pages

**Endpoint**: `GET /api/chapter/:id/pages`

Get all page image URLs for a specific chapter.

**Example Request**:

```
GET /api/chapter/e1f3c817-4e59-43b7-9365-09675a149a6f/pages
```

**Response**:

```json
{
  "pages": [
    "https://uploads.mangadex.org/data/abc123/x1.jpg",
    "https://uploads.mangadex.org/data/abc123/x2.jpg",
    "https://uploads.mangadex.org/data/abc123/x3.jpg"
  ]
}
```

---

## 🛠️ Setup & Development

### Prerequisites

- **Java 17** (JDK 17+)
- **Maven 3.6+**
- **Vercel CLI** (for deployment)

### Local Setup

1. **Clone the repository**:

   ```bash
   git clone <your-repo-url>
   cd manga-universal-backend
   ```

2. **Install dependencies**:

   ```bash
   mvn clean install
   ```

3. **Run the application locally**:

   ```bash
   mvn spring-boot:run
   ```

   The server will start at `http://localhost:8080`

### Testing Locally

Test the endpoints using curl or any HTTP client:

```bash
# Search manga
curl "http://localhost:8080/api/manga/search?q=naruto"

# Get manga details
curl "http://localhost:8080/api/manga/MANGA_ID"

# List chapters
curl "http://localhost:8080/api/manga/MANGA_ID/chapters"

# Get chapter pages
curl "http://localhost:8080/api/chapter/CHAPTER_ID/pages"
```

---

## 📦 Deployment to Vercel

### First-time Deployment

1. **Install Vercel CLI**:

   ```bash
   npm i -g vercel
   ```

2. **Login to Vercel**:

   ```bash
   vercel login
   ```

3. **Deploy**:

   ```bash
   vercel
   ```

   Follow the prompts to link the project to your Vercel account.

4. **Deploy to Production**:
   ```bash
   vercel --prod
   ```

### Subsequent Deployments

After the initial setup, simply run:

```bash
vercel --prod
```

---

## 🔧 Configuration

### application.properties

Spring Boot configuration file:

```properties
server.port=8080
spring.application.name=manga-universal-backend

# CORS Configuration
spring.web.cors.allowed-origins=*
spring.web.cors.allowed-methods=GET,POST,OPTIONS
spring.web.cors.allowed-headers=*

# Jackson Configuration
spring.jackson.default-property-inclusion=non_null
```

### vercel.json

Vercel deployment configuration for Spring Boot:

```json
{
  "builds": [
    {
      "src": "pom.xml",
      "use": "@vercel/java"
    }
  ],
  "routes": [
    {
      "src": "/api/(.*)",
      "dest": "/"
    }
  ]
}
```

### pom.xml

Maven configuration with Spring Boot:

- Spring Boot 3.2.0
- Spring Web (REST APIs)
- Spring Boot Actuator (health checks)
- Jackson (included with Spring Boot)

---

## 📚 Architecture

### Controllers

**MangaController** (`@RestController`)

- `GET /api/manga/search?q=<query>` - Search manga
- `GET /api/manga/{id}` - Get manga details
- `GET /api/manga/{id}/chapters` - List chapters

**ChapterController** (`@RestController`)

- `GET /api/chapter/{id}/pages` - Get chapter pages

### Services/Utilities

**HttpClient** (`@Component`)

- Performs HTTP GET requests using `HttpURLConnection`
- 10-second timeout configuration
- Proper error handling

**MangaDexParser** (`@Component`)

- Parses and normalizes MangaDex API responses
- Extracts English titles and descriptions
- Builds cover URLs from relationships
- Uses Spring's Jackson ObjectMapper via DI

---

## 🌐 CORS Support

All endpoints automatically include the following CORS headers:

```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Content-Type
```

---

## 🐛 Error Handling

All endpoints return errors in a consistent format:

```json
{
  "error": "Error message describing what went wrong",
  "status": 500
}
```

Common HTTP status codes:

- `200`: Success
- `400`: Bad Request (missing parameters)
- `404`: Not Found
- `500`: Internal Server Error

---

## 📝 Notes

- **Rate Limiting**: MangaDex has rate limits. Consider implementing caching for production use.
- **Pagination**: The chapter endpoint limits to 500 chapters. For manga with more chapters, implement pagination.
- **Image Hosting**: Page URLs are served from MangaDex's CDN and may have their own rate limits.
- **Language**: Currently hardcoded to English (`en`). Can be extended to support multiple languages.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

---

## 📄 License

This project is licensed under the MIT License.

---

## 🔗 Related Links

- [MangaDex API Documentation](https://api.mangadex.org/docs/)
- [Vercel Java Runtime](https://vercel.com/docs/runtimes#official-runtimes/java)
- [Jackson Documentation](https://github.com/FasterXML/jackson-databind)

---

## 💡 Tips

### Testing with curl

```bash
# Search manga
curl "https://your-app.vercel.app/api/manga/search?q=naruto"

# Get manga details
curl "https://your-app.vercel.app/api/manga/MANGA_ID"

# List chapters
curl "https://your-app.vercel.app/api/manga/MANGA_ID/chapters"

# Get chapter pages
curl "https://your-app.vercel.app/api/chapter/CHAPTER_ID/pages"
```

### Environment Variables

Vercel automatically provides path parameters as environment variables:

- `/api/manga/[id]` → `id` is available via `System.getenv("id")`
- Query parameters are available via `System.getenv("paramName")`

---

## 🚀 Quick Start

```bash
# Clone and setup
git clone <repo-url>
cd manga-universal-backend
mvn clean install

# Run locally
mvn spring-boot:run

# Test locally
curl "http://localhost:8080/api/manga/search?q=attack%20on%20titan"

# Build JAR for production
mvn clean package

# Deploy to Vercel
vercel login
vercel --prod
```

## 🧪 Testing

Spring Boot makes testing easy:

```bash
# Run tests
mvn test

# Run with coverage
mvn clean verify
```

You can add tests in `src/test/java/com/mangareader/`:

```java
@SpringBootTest
@AutoConfigureMockMvc
class MangaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSearchManga() throws Exception {
        mockMvc.perform(get("/api/manga/search?q=naruto"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results").isArray());
    }
}
```

Happy coding! 🎉
