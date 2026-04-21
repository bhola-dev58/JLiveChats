# JLiveChats Deployment Guide

This guide covers deploying JLiveChats to various platforms.

## Cloud Deployment Options

### 1. Render (Recommended - Free)

**Features:**
- Free tier ($0/month)
- Automatic deployments from GitHub
- Docker support
- Environment variables management
- SSL certificate included

**Steps:**

1. **Create a Render Account**
   - Go to [render.com](https://render.com)
   - Sign up with GitHub (recommended)

2. **Create New Web Service**
   - Click **"New +"** → **"Web Service"**
   - Select **"Deploy existing repository"**
   - Choose `bhola-dev58/JLiveChats`
   - Select **Branch**: `main`

3. **Configure Service**
   - **Name**: `jlivechats`
   - **Environment**: `Docker`
   - **Plan**: Free
   - **Auto-deploy**: ON

4. **Add Environment Variables**
   - Go to **Settings** → **Environment**
   - Add the following variables:
     ```
     GOOGLE_CLIENT_ID=<your-google-client-id>
     GOOGLE_CLIENT_SECRET=<your-google-client-secret>
     PORT=10000
     ```
   - Get these from [Google Cloud Console](https://console.cloud.google.com/)

5. **Deploy**
   - Click **"Create Web Service"**
   - Render will automatically build and deploy
   - Build takes ~3-5 minutes (Maven compilation)
   - Your app will be live at: `https://jlivechats.onrender.com`

**Notes:**
- Free tier services spin down after 15 min of inactivity
- Takes ~30 sec to wake up on first request
- Perfect for testing and development

---

### 2. Railway (Alternative - Free Tier)

**Features:**
- $5/month free credit
- Very fast deployments
- GitHub integration
- Easy Docker support

**Steps:**

1. Go to [railway.app](https://railway.app)
2. Click **"New Project"** → **"Deploy from GitHub repo"**
3. Select `JLiveChats` repository
4. Configure environment variables (same as above)
5. Deploy automatically

---

### 3. Docker Local Testing

**Build locally:**

```bash
cd JLiveChats
docker build -t jlivechats:latest .
```

**Run the container:**

```bash
docker run -p 8080:8080 \
  -e GOOGLE_CLIENT_ID="your-client-id" \
  -e GOOGLE_CLIENT_SECRET="your-client-secret" \
  jlivechats:latest
```

Access at `http://localhost:8080`

---

## Environment Variables

### Required (for Google OAuth)
- `GOOGLE_CLIENT_ID` - Your Google OAuth client ID
- `GOOGLE_CLIENT_SECRET` - Your Google OAuth client secret

### Optional
- `PORT` - Server port (default: 8080)
- `SPRING_PROFILES_ACTIVE` - Active Spring profiles

---

## Monitoring & Logs

### On Render Dashboard:
1. Go to your service page
2. **Logs** tab - View real-time logs
3. **Metrics** tab - CPU, memory, disk usage

### Check Service Status:
```bash
curl https://jlivechats.onrender.com/health
```

---

## Troubleshooting

### Build Fails
- Check logs in Render dashboard
- Verify environment variables are set
- Ensure `pom.xml` is valid

### App Won't Start
- Check environment variables match expected format
- Verify database connection (H2 is in-memory)
- Check logs for Spring Boot errors

### Docker Build Issues
- Multi-stage build requires Maven (installed in Docker)
- Build takes 3-5 minutes due to dependency downloads
- First build is slowest; subsequent builds are cached

---

## File Structure for Deployment

```
.
├── Dockerfile              # Multi-stage Docker build
├── render.yaml            # Render deployment config
├── .dockerignore          # Files to exclude from Docker
├── .env.example           # Environment variables template
├── pom.xml                # Maven configuration
├── src/
│   ├── main/java/         # Java source code
│   └── main/resources/    # Application resources
└── README.md              # This file
```

---

## After Deployment

1. **Test the app:**
   - Navigate to your deployed URL
   - Try logging in with test credentials

2. **Monitor logs:**
   - Check Render/Railway dashboard regularly
   - Set up email alerts for build failures

3. **Custom domain (Optional):**
   - On Render: Settings → Custom Domain
   - Add your domain and configure DNS

---

## Support

For issues:
- Check application logs
- Review Render/Railway documentation
- Check Spring Boot logs for stack traces
