@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
import groovyx.net.http.RESTClient


def url = "https://jira.autorola.org"
def path = "/rest/api/2/search"
def query = '''{
    "jql": "filter = 13706 AND issuetype in (Story, Bug, Spike) AND status = Closed AND resolved >= 2014-01-01 AND cf[10004] is not EMPTY AND timespent is not EMPTY ORDER BY resolved DESC",
    "startAt": 0,
    "maxResults": 15,
    "fields": [
      "key",
      "summary",
      "status",
      "reporter",
      "cf[10004]",
      "timespent"
    ]
    }'''

static def searchJira(String _url,String _path,String _query){
    def ret = null;
    def jira = new RESTClient(_url);
    jira.headers['Authorization'] = 'Basic '+"bg:535JKCqX".getBytes('iso-8859-1').encodeBase64()
    jira.headers['Accept'] = 'application/json'

    def resp = jira.post(
            path : _path,
            body : _query,
            requestContentType: 'application/json'
    )

    println resp.getData()
}

searchJira(url,path,query);