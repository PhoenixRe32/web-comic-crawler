package com.andreouconsulting.webcrawler.webcomic;

import com.andreouconsulting.webcrawler.webcomic.crawlers.BaseWebComicCrawler;
import com.andreouconsulting.webcrawler.webcomic.crawlers.InterrobangWebComicCrawler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class InterrobangStudios {

    public static void main(String... args) throws IOException {

        Map<String, String> cadCrawlers = new LinkedHashMap<>();
        cadCrawlers.put("It Sucks to be Weegie", "http://www.interrobangstudios.com/comics-display.php?strip_id=941");


        for (Map.Entry<String, String> crawlerEntry : cadCrawlers.entrySet()) {
            BaseWebComicCrawler cadCrawler = new InterrobangWebComicCrawler(crawlerEntry.getValue(), crawlerEntry.getKey());
            cadCrawler.crawl();
        }
    }
}
