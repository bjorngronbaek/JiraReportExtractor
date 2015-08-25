import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import groovy.json.JsonSlurper
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.impl.client.LaxRedirectStrategy
import org.apache.http.protocol.HttpContext;

def props = new Properties()
new File("../resources/jira.properties").withInputStream {
    stream -> props.load(stream)
}

def searchpath = "/rest/api/2/search"

def jira = new HTTPBuilder(props.url);
/* this is required to bypass redirect for post request at Autorola but it seems to break the API*/
//jira.client.setRedirectStrategy(new LaxRedirectStrategy())

/* set content type to TEXT to prevent HTTPBuilder's parser from running */
jira.request(Method.GET,ContentType.TEXT){ req ->
    uri.path = searchpath;
    uri.query = [
            //jql: "filter = 13706 AND issuetype in (Story, Bug, Spike) AND status = Closed AND resolved >= 2014-01-01 AND cf[10004] is not EMPTY AND timespent is not EMPTY ORDER BY resolved DESC",
            jql : "filter=13806 AND fixVersion = 2015-08-19",
            startAt: 0,
            //maxResults: 0,
            fields: [
                    "key",
                    "project",
                    "summary",
                    "status",
                    "reporter",
                    "cf[10004]",
                    "timespent"]
    ]
    /* set basic header */
    headers['Authorization'] = 'Basic '+"${props.username}:${props.password}".getBytes('iso-8859-1').encodeBase64()
    /* set accept header manually to get JSON result when using content-tyep TEXT */
    headers['Accept'] = 'application/json'
    response.success = { resp, reader ->
        assert resp.status == 200
        println "My response handler got response: ${resp.statusLine}"

        /* feed to slurper to get GPathResult */
        def parser = new JsonSlurper().parse(reader)
        parser.issues.each{
            println it.key
        }
    }
}