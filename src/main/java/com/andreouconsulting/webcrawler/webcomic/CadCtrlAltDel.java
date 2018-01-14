package com.andreouconsulting.webcrawler.webcomic;

import com.andreouconsulting.webcrawler.webcomic.crawlers.BaseWebComicCrawler;
import com.andreouconsulting.webcrawler.webcomic.crawlers.CadWebComicCrawler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CadCtrlAltDel {

    public static void main(String... args) throws IOException {

        Map<String, String> cadCrawlers = new LinkedHashMap<>();
        cadCrawlers.put("http://cad-comic.com/comic/nice-melon/",
                "Ctrl+Alt+Del");
        cadCrawlers.put("http://cad-comic.com/comic/the-starcaster-chronicles-01-01/",
                "The Starcaster Chronicles");
        cadCrawlers.put("http://cad-comic.com/comic/analog-and-d-pad-01-01/",
                "Analog and D-Pad");
        cadCrawlers.put("http://cad-comic.com/comic/the-campaign-characters/",
                "The Campaign");
        cadCrawlers.put("http://cad-comic.com/comic/the-console-war-of-2013-p-1/",
                "The Console Wars");
        cadCrawlers.put("http://cad-comic.com/comic/25864/",
                "Sillies");

        for (Map.Entry<String, String> crawlerEntry : cadCrawlers.entrySet()) {
            BaseWebComicCrawler cadCrawler = new CadWebComicCrawler(crawlerEntry.getKey(), crawlerEntry.getValue());
            cadCrawler.crawl();
        }
    }
}
