# SEBT Platform Backend - Docker Setup

## Prerequisites
- Docker and Docker Compose installed
- Git (for cloning the repository)

## Quick Start

### 1. Build and Run with Docker Compose
```bash
# Clone the repository (if not already done)
git clone <your-repo-url>
cd Backend-SWP391_2ndLand

# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build
```

### 2. Build Docker Image Only
```bash
# Build the backend image
docker build -t sebt-backend .

# Run the container (make sure you have SQL Server running)
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:sqlserver://localhost:1433;databaseName=SEBT_platform;encrypt=true;trustServerCertificate=true" \
  -e SPRING_DATASOURCE_USERNAME=sa \
  -e SPRING_DATASOURCE_PASSWORD=YourPassword \
  sebt-backend
```

## Environment Variables

The following environment variables can be configured:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Database connection URL | - |
| `SPRING_DATASOURCE_USERNAME` | Database username | sa |
| `SPRING_DATASOURCE_PASSWORD` | Database password | - |
| `SERVER_PORT` | Application port | 8080 |
| `CLOUDINARY_URL` | Cloudinary configuration | - |

## Services

### Backend Service
- **Port**: 8080
- **Health Check**: http://localhost:8080/actuator/health
- **Logs**: Stored in `./logs/` directory

### SQL Server Service
- **Port**: 1433
- **Username**: sa
- **Password**: YourStrong@Passw0rd (change in production)

## Useful Commands

```bash
# View logs
docker-compose logs -f backend
docker-compose logs -f sqlserver

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Rebuild only backend
docker-compose up --build backend

# Execute commands in running container
docker-compose exec backend sh

# Check container status
docker-compose ps
```

## Production Deployment

For production deployment, consider:

1. **Change default passwords**
2. **Use environment files or secrets management**
3. **Configure SSL/TLS**
4. **Set up proper logging and monitoring**
5. **Use production-grade database**

### Production Environment File (.env)
```env
SPRING_DATASOURCE_URL=jdbc:sqlserver://your-db-host:1433;databaseName=SEBT_platform
SPRING_DATASOURCE_USERNAME=your-username
SPRING_DATASOURCE_PASSWORD=your-secure-password
CLOUDINARY_URL=cloudinary://your-api-key:your-api-secret@your-cloud-name
```

## Troubleshooting

### Common Issues

1. **Port already in use**
   ```bash
   # Change ports in docker-compose.yml
   ports:
     - "8081:8080"  # Use different host port
   ```

2. **Database connection issues**
   - Ensure SQL Server is healthy: `docker-compose ps`
   - Check connection string format
   - Verify credentials

3. **Application not starting**
   ```bash
   # Check logs
   docker-compose logs backend
   
   # Check if all environment variables are set
   docker-compose exec backend env | grep SPRING
   ```

4. **Memory issues**
   ```bash
   # Increase memory limits in Dockerfile
   ENV JAVA_OPTS="-Xmx1024m -Xms512m"
   ```

## Development

For development with hot reload:

```bash
# Mount source code as volume (add to docker-compose.yml)
volumes:
  - ./src:/app/src
  - ./target:/app/target
```

## Health Checks

The application includes health checks accessible at:
- http://localhost:8080/actuator/health
- http://localhost:8080/actuator/info

## Security Notes

- Change default SQL Server password
- Use environment variables for sensitive data
- Consider using Docker secrets in production
- Review and update security settings for production use
