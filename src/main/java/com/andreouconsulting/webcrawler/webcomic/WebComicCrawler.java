package com.andreouconsulting.webcrawler.webcomic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public interface WebComicCrawler {
    Logger logger = LoggerFactory.getLogger(WebComicCrawler.class);

    default Document getPage(String url) throws IOException {
        logger.debug(String.format("Retrieving %s", url));
        Document document = Jsoup.connect(url).get();
        logger.debug(String.format("Retrieved %s successfully", url));
        return document;
    }

    URL[] scrapeImageUrls(Document document);
}
