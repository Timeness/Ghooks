import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TelegramSender {
    private static final Logger logger = LoggerFactory.getLogger(TelegramSender.class)
    
    static void sendMessage(String botToken, String chatId, String message) {
        def client = HttpClients.createDefault()
        try {
            def post = new HttpPost("https://api.telegram.org/bot${botToken}/sendMessage")
            def params = new JSONObject([
                chat_id: chatId,
                text: message,
                parse_mode: 'HTML'
            ])
            post.setEntity(new StringEntity(params.toString(), 'UTF-8'))
            post.setHeader('Content-Type', 'application/json')
            def response = client.execute(post)
            if (response.statusLine.statusCode != 200) {
                logger.error("Failed to send Telegram message: ${response.statusLine}")
            }
        } catch (Exception e) {
            logger.error("Error sending Telegram message: ${e.message}")
        } finally {
            client.close()
        }
    }
}
