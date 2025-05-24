import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WebhookHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebhookHandler.class)
    
    static String processEvent(String event, JSONObject data, String botToken, String chatId) {
        def repoName = data.getJSONObject('repository')?.getString('name') ?: 'Unknown'
        def repoUrl = data.getJSONObject('repository')?.getString('html_url') ?: '#'
        def sender = data.getJSONObject('sender')?.getString('login') ?: 'Unknown'
        def senderUrl = data.getJSONObject('sender')?.getString('html_url') ?: '#'
        
        def message
        switch (event) {
            case 'ping':
                message = "<b>Webhook Ping</b> for <a href='${repoUrl}'>${repoName}</a> by <a href='${senderUrl}'>${sender}</a>!"
                break
            case 'push':
                def commits = data.getJSONArray('commits') ?: []
                def branch = data.getString('ref').split('/')[-1]
                def commitText = commits.collect { commit ->
                    def msg = commit.getString('message').split('\n')[0].replaceAll('<', '&lt;').replaceAll('>', '&gt;')
                    if (msg.length() > 300) msg = msg.substring(0, 300) + '...'
                    "<b><a href='${commit.getString('url')}'>${commit.getString('id')[0..6]}</a></b>: ${msg} by ${commit.getJSONObject('author').getString('name')}"
                }.join('\n')
                def commitCount = commits.length()
                message = "<b>✨ ${commitCount} New Commit${commitCount > 1 ? 's' : ''} Pushed to <a href='${repoUrl}'>${repoName}</a> (${branch})</b>\n\n${commitText}"
                if (commitCount > 10) message += "\n<i>And ${commitCount - 10} other commits</i>"
                break
            case 'pull_request':
                def pr = data.getJSONObject('pull_request')
                def action = data.getString('action').capitalize()
                def prTitle = pr.getString('title').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
                def prBody = pr.getString('body')?.replaceAll('<', '&lt;').replaceAll('>', '&gt;') ?: 'No description'
                message = "<b>❗ ${action} Pull Request for <a href='${repoUrl}'>${repoName}</a></b>\n<b>${prTitle}</b> (${pr.getString('state')})\n${prBody}\n<a href='${pr.getString('html_url')}'>Pull Request #${pr.getInt('number')}</a>"
                break
            case 'issues':
                def issue = data.getJSONObject('issue')
                def action = data.getString('action').capitalize()
                def issueTitle = issue.getString('title').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
                def issueBody = issue.getString('body')?.replaceAll('<', '&lt;').replaceAll('>', '&gt;') ?: 'No description'
                message = "<b>⚠️ ${action} Issue in <a href='${repoUrl}'>${repoName}</a></b>\n<b>${issueTitle}</b>\n${issueBody}\n<a href='${issue.getString('html_url')}'>Issue #${issue.getInt('number')}</a>"
                break
            case 'issue_comment':
                def comment = data.getJSONObject('comment')
                def issue = data.getJSONObject('issue')
                def commentBody = comment.getString('body').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
                message = "<b>💬 New Comment in <a href='${repoUrl}'>${repoName}</a></b>\nIssue: ${issue.getString('title')}\n${commentBody}\n<a href='${comment.getString('html_url')}'>Comment on Issue #${issue.getInt('number')}</a>"
                break
            case 'star':
                def action = data.getString('action')
                if (action == 'created') {
                    message = "<b>🌟 <a href='${senderUrl}'>${sender}</a> Gave a Star to <a href='${repoUrl}'>${repoName}</a>!</b>\n<b>Total Stars:</b> <i>${data.getJSONObject('repository').getInt('stargazers_count')}</i>"
                }
                break
            case 'release':
                def release = data.getJSONObject('release')
                def action = data.getString('action').capitalize()
                def releaseName = release.getString('name')?.replaceAll('<', '&lt;').replaceAll('>', '&gt;') ?: release.getString('tag_name')
                def releaseBody = release.getString('body')?.replaceAll('<', '&lt;').replaceAll('>', '&gt;') ?: 'No description'
                message = "<b><a href='${senderUrl}'>${sender}</a> ${action} <a href='${repoUrl}'>${repoName}</a>!</b>\n\n<b>${releaseName}</b> (${release.getString('tag_name')})\n${releaseBody}\n<a href='${release.getString('tarball_url')}'>Download Tar</a> | <a href='${release.getString('zipball_url')}'>Download Zip</a>"
                break
            case 'create':
                if (data.getString('ref_type') == 'branch') {
                    def branch = data.getString('ref')
                    message = "<b>Branch ${branch} Created on <a href='${repoUrl}'>${repoName}</a> by <a href='${senderUrl}'>${sender}</a>!</b>"
                }
                break
            case 'delete':
                if (data.getString('ref_type') == 'branch') {
                    def branch = data.getString('ref')
                    message = "<b>Branch ${branch} Deleted on <a href='${repoUrl}'>${repoName}</a> by <a href='${senderUrl}'>${sender}</a>!</b>"
                }
                break
            case 'fork':
                def forkee = data.getJSONObject('forkee')
                message = "<b>🍴 <a href='${forkee.getString('svn_url')}'>${forkee.getString('full_name')}</a> Forked <a href='${repoUrl}'>${repoName}</a></b>\n<b>Total Forks:</b> <i>${data.getJSONObject('repository').getInt('forks_count')}</i>"
                break
            case 'gollum':
                def pages = data.getJSONArray('pages')
                def text = "<b><a href='${repoUrl}'>${repoName}</a> Wiki Pages Updated by <a href='${senderUrl}'>${sender}</a>!</b>\n\n"
                pages.each { page ->
                    def summary = page.getString('summary') ? "${page.getString('summary').replaceAll('<', '&lt;').replaceAll('>', '&gt;')}\n" : ''
                    text += "<b>${page.getString('title').replaceAll('<', '&lt;').replaceAll('>', '&gt;')}</b> (${page.getString('action')})\n${summary}<a href='${page.getString('html_url')}'>${page.getString('page_name')}</a> - ${page.getString('sha')[0..6]}\n"
                    if (pages.length() > 1) text += "=====================\n"
                }
                message = text
                break
            case 'status':
                def state = data.getString('state')
                def emo = state == 'pending' ? '⏳' : state == 'success' ? '✔️' : state == 'failure' ? '❌' : '🌀'
                def commitMsg = data.getJSONObject('commit')?.getJSONObject('commit')?.getString('message')?.replaceAll('<', '&lt;').replaceAll('>', '&gt;') ?: 'No message'
                message = "${emo} <a href='${data.getString('target_url')}'>${data.getString('description').replaceAll('<', '&lt;').replaceAll('>', '&gt;')}</a> on <a href='${repoUrl}'>${repoName}</a> by <a href='${senderUrl}'>${sender}</a>!\nLatest Commit:\n<a href='${data.getJSONObject('commit')?.getJSONObject('commit')?.getString('url')}'>${commitMsg}</a>"
                break
            default:
                message = null
                logger.info("Unsupported event: ${event}")
        }
        
        if (message) {
            logger.info("Sending message for event ${event}, size: ${message.bytes.length}")
            TelegramSender.sendMessage(botToken, chatId, message)
            return 'ok'
        } else {
            return 'tf'
        }
    }
}
