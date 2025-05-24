import static spark.Spark.*
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GitGramApp {
    private static final Logger logger = LoggerFactory.getLogger(GitGramApp.class)
    
    static void main(String[] args) {
        def host = Config.getHost()
        def port = Config.getPort()
        def botToken = Config.getBotToken()
        def chatId = Config.getChatId()
        def webhookSecret = Config.getWebhookSecret()
        
        System.setProperty('org.slf4j.simpleLogger.logFile', 'logs/logs.txt')
        System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', 'info')
        
        ipAddress(host)
        port(port)
        
        get('/', { req, res ->
            new JSONObject([
                status: 'API Is Running',
                server_alive: true,
                version: '0.0.2',
                host: host,
                port: port,
                git_bot: '@GitGramBot',
                docs: '/info'
            ]).toString()
        })
        
        get('/info', { req, res ->
            res.redirect('https://graph.org/file/48d7247ff3553c47b5c1e.jpg')
            return null
        })
        
        post('/ghook', { req, res ->
            new JSONObject([status: 'Error Occurred', type: 'Chat ID Not Found']).toString()
        })
        
        post('/ghook/:chat', { req, res ->
            def chat = req.params('chat')
            def headers = req.headers()
            
            if (!headers.get('User-Agent')?.startsWith('GitHub-Hookshot')) {
                logger.info('Invalid User-Agent, not from GitHub')
                return 'Please use this webhook URL to your repository & updates will be sent to chat given as parameter.'
            }
            
            if (headers.get('Content-Type') != 'application/json') {
                logger.info("Invalid Content-Type: ${headers.get('Content-Type')}")
                return 'Invalid data type or error occurred!'
            }
            
            def payload = req.body()
            def signature = headers.get('X-Hub-Signature-256')
            if (!SignatureVerifier.verifySignature(payload, signature, webhookSecret)) {
                res.status(401)
                return 'Invalid signature'
            }
            
            def event = headers.get('X-GitHub-Event')
            def data = new JSONObject(payload)
            def repoName = data.getJSONObject('repository')?.getString('full_name') ?: data.getJSONObject('organization')?.getString('login') ?: 'Unknown'
            if (!repoName) {
                logger.info('No organization or repository found')
                return 'Invalid data type or error occurred!'
            }
            
            logger.info("Received ${payload.bytes.length} bytes of data")
            def initialMsg = "<b>Received ${payload.bytes.length} Bytes Of Data. Now Verifying...</b>"
            TelegramSender.sendMessage(botToken, chat, initialMsg)
            
            def response = WebhookHandler.processEvent(event, data, botToken, chat)
            return response
        })
        
        logger.info("GitGram started on ${host}:${port}")
    }
}
