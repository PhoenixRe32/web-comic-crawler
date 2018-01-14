package com.andreouconsulting.webcrawler.webcomic;

import com.andreouconsulting.webcrawler.webcomic.crawlers.BaseWebComicCrawler;
import com.andreouconsulting.webcrawler.webcomic.crawlers.CadWebComicCrawler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CadCtrlAltDel {

    public static void main(String... args) throws IOException {

        Map<String, String> cadCrawlers = new LinkedHashMap<>();
        cadCrawlers.put("The Starcaster Chronicles", "http://cad-comic.com/comic/the-starcaster-chronicles-01-01/");
        cadCrawlers.put("Analog and D-Pad", "http://cad-comic.com/comic/analog-and-d-pad-01-01/");
        cadCrawlers.put("The Campaign", "http://cad-comic.com/comic/the-campaign-characters/");
        cadCrawlers.put("The Console Wars", "http://cad-comic.com/comic/the-console-war-of-2013-p-1/");
        cadCrawlers.put("Ctrl+Alt+Del", "http://cad-comic.com/comic/nice-melon/");
        cadCrawlers.put("Sillies", "http://cad-comic.com/comic/25864/");

        for (Map.Entry<String, String> crawlerEntry : cadCrawlers.entrySet()) {
            BaseWebComicCrawler cadCrawler = new CadWebComicCrawler(crawlerEntry.getValue(), crawlerEntry.getKey());
            cadCrawler.crawl();
        }
    }
}
