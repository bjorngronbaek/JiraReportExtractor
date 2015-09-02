package net.gronbaek.jira

import groovy.json.JsonSlurper

import java.text.DecimalFormat

abstract class JiraCalculator extends Script {

    DecimalFormat df = new DecimalFormat("###,##0.00");
    Map<Float,List<JiraIssue>> issuesMap;
    float totalHours = 0, totalPoints = 0;
    def parser;

    JiraCalculator(){

    }

    void calculate(propertiesFilePath){
        def jiraIssuesGrabber = new JiraIssuesGrabber(propertiesFilePath)
        parser = jiraIssuesGrabber.getFullJiraSearchResult();
        issuesMap = mapAndCleanIssues(parser);
    }

    Map<Float,List<JiraIssue>> mapAndCleanIssues(parser){
        Map<Float,List<Float>> map = new TreeMap<>();
        parser.issues.each{
            float storyPoints = it.fields.customfield_10004
            float hours = it.fields.timespent / 3600
            String key = it.key

            totalPoints += storyPoints
            totalHours += hours

            def issue = new JiraIssue(key,storyPoints,hours)
            if(map.containsKey(storyPoints)){
                map.get(storyPoints).add(issue)
            }
            else{
                map.put(storyPoints,new ArrayList<Float>(Arrays.asList(issue)))
            }
        }

        return map;
    }

    void printMap(){
        issuesMap.each {k,v ->
            println "Story Points: ${k} -> Hours: ${v.hours}"
        }
    }

    void printStatsMap() {
        issuesMap.each { k, v ->
            float median, q1, q3, average;
            def values  = v.collect{it.hours}
            if (values != null) {
                values.sort();
                average = values.sum() / values.size();
                if (values.size() % 2 == 0) {
                    int low = values.size() / 2 - 1
                    int high = values.size() / 2
                    median = (values.get(low) + values.get(high)) / 2;
                    def q1values = values.subList(0, high);
                    q1 = q1values.sum() / q1values.size();
                    def q2values = values.subList(high, values.size());
                    q3 = q2values.sum() / q2values.size();
                } else {
                    int middle = values.size() / 2;
                    median = values.get(middle);
                    if (middle > 0) {
                        def q1values = values.subList(0, middle);
                        q1 = q1values.sum() / q1values.size();
                        def q2values = values.subList(middle, values.size());
                        q3 = q2values.sum() / q2values.size();
                    } else {
                        q1 = q2 = median;
                    }
                }
            }

            println "Story Points: ${k} -> Average: ${average}, Q1: ${q1}, Median: ${median}, Q3: ${q3}"
        }
    }

    void printHour2PointRatio() {
        println "Total issues: ${parser.issues.size()}, Total points: ${totalPoints}, Total hours: ${totalHours}, Hours pr. point: ${totalHours/totalPoints}"
    }

    void printEstimationRates(boolean matchTShirtSizes){
        issuesMap.each { k, v ->
            def points = v.collect{it.hours * totalPoints/totalHours};
            println "${k} ${df.format(points.sum() / points.size())}"
        }
    }

    class JiraIssue{
        String key;
        Float storyPoints;
        Float hours;

        JiraIssue(String key, Float storyPoints, Float hours) {
            this.key = key
            this.storyPoints = storyPoints
            this.hours = hours
        }
    }

    enum JiraTShirtSize{
        XS(0,0.25f,0.5f),
        S(0.5f,1,2),
        M(2,3,5),
        L(5,8,13),
        XL(13,20,40)

        private float low,rate,high;

        JiraTShirtSize(float low, float rate, float high) {
            this.low = low
            this.rate = rate
            this.high = high
        }

        JiraTShirtSize fromValue(value){
            for(JiraTShirtSize size: JiraTShirtSize.values()){
                if(value >= size.low && value < size.high){
                    size; //implicit return, noob!
                }
            }
        }
    }
}
