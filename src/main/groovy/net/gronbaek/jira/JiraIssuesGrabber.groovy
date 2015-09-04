package net.gronbaek.jira

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
@Grapes(
        @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
)
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

class JiraIssuesGrabber{

    def props = new Properties()
    JiraIssuesGrabber(String _propertyFilePath){
        new File(_propertyFilePath).withInputStream {
            stream -> props.load(stream)
        }
    }

    def getFullJiraSearchResult() {
        def maxResults = 50
        def json = getJiraSearchResult(maxResults)

        def root = new JsonSlurper().parse(new StringReader(json))
        def total = root.total;
        def size = root.issues.size()

        if (size < total) {
            for (int startAt = size; startAt < total; startAt += maxResults) {
                def parse = new JsonSlurper().parse(new StringReader(getJiraSearchResult(maxResults, startAt)))
                root.issues.addAll(parse.issues)
            }
        }

        root
    }

    String getJiraSearchResult(_maxResults = 1000, _startAt = 0) {
        def username = props.username;
        def password = props.password;
        def url = props.url;
        def searchpath = props.searchpath;

        println "Querying ${url+searchpath} for issues"

        /*
         * Perform GET request against API.
         * Setting content type to TEXT to prevent HTTPBuilder's parser from running.
         */
        def jira = new HTTPBuilder(url);
        def json = jira.request(Method.GET, ContentType.TEXT) { req ->
            uri.path = searchpath;
            uri.query = [
                    jql    : "filter=13806 AND fixVersion = 2015-08-19",
                    //jql    : "filter=13806",
                    startAt: _startAt,
                    maxResults: _maxResults,
                    fields : [
                            "key",
                            "project",
                            "summary",
                            "status",
                            "reporter",
                            "customfield_10004",
                            "timespent"]
            ]
            println uri
            /* set basic header */
            headers['Authorization'] = 'Basic ' + "${username}:${password}".getBytes('iso-8859-1').encodeBase64()
            /* set accept header manually to get JSON result when using content-type TEXT */
            headers['Accept'] = 'application/json'
            response.success = { resp, reader ->
                assert resp.status == 200
                println "Got response: ${resp.statusLine}"
                def text = reader.getText()
                println("Result ${text}")
                return text;
            }

            response.failure = { resp,reader ->
                println "Got error response: ${resp.statusLine}"
                def text = reader.getText()
                println("Result ${text}")
            }
        }
    }
}