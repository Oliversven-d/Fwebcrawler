package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

public class CustomTask extends RecursiveAction {

    private String url;
    private Instant deadline;
    private Map<String, Integer> counts;
    private Set<String> visitedUrls;
    private PageParserFactory parserFactory;
    private Clock clock;
    private int maxDepth;
    private List<Pattern> ignoredUrls;

    public CustomTask(
            String url,
            Instant deadline,
            int maxDepth,
            Map<String, Integer> counts,
            Set<String> visitedUrls,
            List<Pattern> ignoredUrls,
            PageParserFactory parserFactory, Clock clock) {
        this.url = url;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
        this.ignoredUrls = ignoredUrls;
        this.parserFactory = parserFactory;
        this.clock = clock;
    }

    @Override
    protected void compute() {
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return;
        }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }
        if (visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);
        PageParser.Result result = parserFactory.get(url).parse();
        for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            if (counts.containsKey(e.getKey())) {
                counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
            } else {
                counts.put(e.getKey(), e.getValue());
            }
        }
        List<CustomTask> subtasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            subtasks.add(new CustomTask(link, deadline, maxDepth - 1, counts, visitedUrls, ignoredUrls, parserFactory, clock));
        }
        invokeAll(subtasks);
    }
}
