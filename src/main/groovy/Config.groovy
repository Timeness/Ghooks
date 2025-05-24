class Config {
    static def props = new Properties()
    
    static {
        def file = new File('src/main/resources/config.properties')
        if (!file.exists()) {
            throw new RuntimeException('config.properties not found!')
        }
        file.withInputStream { props.load(it) }
    }
    
    static String getBotToken() { props.getProperty('telegram.bot.token') }
    static String getChatId() { props.getProperty('telegram.chat.id') }
    static String getWebhookSecret() { props.getProperty('github.webhook.secret', '') }
    static String getHost() { props.getProperty('server.host', '0.0.0.0') }
    static int getPort() { props.getProperty('server.port', '4567').toInteger() }
}
