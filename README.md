# GitGram

A Groovy-based API to send GitHub webhook notifications to a Telegram chat.

## Setup
1. **Prerequisites**:
   - Java 11+ and Gradle.
   - A Telegram bot token from BotFather.
   - A GitHub repository with webhook configured.

2. **Configuration**:
   - Update `src/main/resources/config.properties` with:
     - `telegram.bot.token`: Your Telegram bot token.
     - `telegram.chat.id`: Your Telegram chat ID.
     - `github.webhook.secret`: (Optional) Your GitHub webhook secret.
     - `server.host` and `server.port`: Server settings.

3. **Build and Run**:
   ```bash
   gradle build
   gradle runApp
