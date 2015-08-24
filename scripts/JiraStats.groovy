import groovy.util.slurpersupport.NodeChildren

import java.text.DecimalFormat

def url = '''https://jira.autorola.org/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=filter+in+%28%22Team+FM+%26+MP+2%22%29+AND+status+%3D+closed+AND+labels+%3D+release-2.54&tempMax=1000''';

//def searchResult = new XmlSlurper().parse("2_54-release.xml");
def searchResult = new XmlSlurper().parse("20140101_.xml");
//println "version = ${searchResult.@version}";

Map<Float,List<Integer>> pointsAndHours = new TreeMap<>();
def hourForm = new DecimalFormat("###.00");
def pointForm = new DecimalFormat("0.0#");
hourForm.setDecimalSeparatorAlwaysShown(true);
pointForm.setDecimalSeparatorAlwaysShown(true);


searchResult.channel.item.each{
    NodeChildren estimateNode = it.customfields.depthFirst().find { it.name() == 'customfield' && it.@id == 'customfield_10004'; }.customfieldvalues.customfieldvalue
    NodeChildren timeSpentNode = it.timespent.@seconds;

    Float estimate = estimateNode.toFloat();
    Integer seconds = timeSpentNode != null && !timeSpentNode.text().equals("")  ? timeSpentNode.toInteger() : 0;
    //println "${it.key}: estimate=${estimate} timeSpent=${seconds}";

    if(estimate != 0.0 && seconds != 0){
        if(pointsAndHours.containsKey(estimate)){
            def listOfIntegers = pointsAndHours.get(estimate)
            listOfIntegers.add(seconds);
        }
        else{
            pointsAndHours.put(estimate,new ArrayList(Arrays.asList(seconds)));
        }
    }
};

def hourDivider = 1;
def separator = ";";
println("sep=${separator}");
println "key${separator}average${separator}q1${separator}median${separator}q3";

for(Float key = 0.0;key<13.25;key+=0.25){
    List<Integer> values = pointsAndHours.get(key);
    float median, q1, q3, average;
    if(values!=null) {
        values.sort();
        average = values.sum() / values.size();
        if (values.size() % 2 == 0) {
            int low = values.size() / 2 - 1
            int high = values.size() / 2
            //println("key:${key} lower:${low} higher:${high}");
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


    println "${pointForm.format(key)}${separator}${hourForm.format(average / hourDivider)}${separator}${hourForm.format(q1/ hourDivider)}${separator}${hourForm.format(median / hourDivider)}${separator}${hourForm.format(q3/ hourDivider)}";
}

for(Float key : pointsAndHours.keySet()){
    StringBuffer buffer = new StringBuffer("${pointForm.format(key)}");
    pointsAndHours.get(key).each {buffer.append("${separator}${it}")}
    println(buffer.toString())
}

