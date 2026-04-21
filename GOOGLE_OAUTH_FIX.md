# Google OAuth Configuration Fix

## Problem
You're getting: `Error 400: redirect_uri_mismatch`

This happens because your Google OAuth credentials were configured for `localhost:8080`, but your app is now deployed on Render at a different URL.

---

## Solution: Update Google Cloud Console

### Step 1: Get Your Render URL
Your app is deployed at: **`https://jlivechats.onrender.com`**

The OAuth redirect URI should be: **`https://jlivechats.onrender.com/login/oauth2/code/google`**

### Step 2: Update Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project (or create one)
3. Go to **APIs & Services** → **Credentials**
4. Click on your OAuth 2.0 Client ID (web application)
5. Under **Authorized redirect URIs**, add these:
   ```
   http://localhost:8080/login/oauth2/code/google
   https://jlivechats.onrender.com/login/oauth2/code/google
   ```
6. Click **Save**

### Step 3: Verify Render Environment Variables

In your Render dashboard:
1. Go to your JLiveChats service
2. Click **Settings** → **Environment**
3. Verify these are set:
   - `GOOGLE_CLIENT_ID` - Your client ID (starts with long number)
   - `GOOGLE_CLIENT_SECRET` - Your client secret (starts with GOCSPX-)

### Step 4: Force Redeploy

In Render dashboard:
1. Go to your service
2. Click **"Manual Deploy"** → **"Deploy latest commit"**
3. Wait for build to complete (~3-5 minutes)
4. Try signing in again

---

## If You Don't Have Google OAuth Credentials

### Create New OAuth Credentials:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. **APIs & Services** → **Enable APIs**
   - Search and enable: **Google+ API**
4. **Credentials** → **Create Credentials**
   - Choose **OAuth 2.0 Client ID**
   - Application type: **Web application**
   - Authorized JavaScript origins: 
     ```
     http://localhost:8080
     https://jlivechats.onrender.com
     ```
   - Authorized redirect URIs:
     ```
     http://localhost:8080/login/oauth2/code/google
     https://jlivechats.onrender.com/login/oauth2/code/google
     ```
5. Copy the **Client ID** and **Client Secret**
6. Add to Render environment variables

---

## Testing Locally

Before deploying, test on localhost:

```bash
# Set environment variables
export GOOGLE_CLIENT_ID="your-client-id"
export GOOGLE_CLIENT_SECRET="your-client-secret"

# Run locally
mvn spring-boot:run
```

Then sign in at `http://localhost:8080` - Google OAuth should work!

---

## Common Issues

### Still getting redirect_uri_mismatch?
- Clear browser cookies/cache
- Wait 5 minutes for Google's cache to update
- Verify exact URL matches (no trailing slashes!)
- Check Render console logs for errors

### Blank page after signing in?
- Check Render logs
- Verify Spring Boot is running
- Check environment variables are set correctly

### Can't find my credentials?
- Check Google Cloud Console under all projects
- Ensure the project has Google+ API enabled
- Create new credentials if needed
