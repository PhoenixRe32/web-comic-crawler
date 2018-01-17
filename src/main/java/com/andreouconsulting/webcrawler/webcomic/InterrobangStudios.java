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
        cadCrawlers.put("Crisis of Infinite Sues", "http://www.interrobangstudios.com/comics-display.php?strip_id=989");
        cadCrawlers.put("Trigger Star", "http://www.interrobangstudios.com/comics-display.php?strip_id=60");
        cadCrawlers.put("The Dark Intruder", "http://www.interrobangstudios.com/comics-display.php?comic_id=1");


        for (Map.Entry<String, String> crawlerEntry : cadCrawlers.entrySet()) {
            BaseWebComicCrawler cadCrawler = new InterrobangWebComicCrawler(crawlerEntry.getValue(), crawlerEntry.getKey());
            cadCrawler.crawl();
        }
    }
}
