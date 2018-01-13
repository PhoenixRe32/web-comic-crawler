package com.andreouconsulting.webcrawler.webcomic;

import com.andreouconsulting.webcrawler.webcomic.exceptions.UnexpectedWebsiteStructure;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class CadWebComicCrawler implements WebComicCrawler {

    private static final Logger logger = LoggerFactory.getLogger(WebComicCrawler.class);
    public static final String COMIC_ELEMENT = "comicpage";
    public static final String DIRECT_ANCHOR = ">a[href]";
    public static final String DIRECT_IMG = ">img";
    public static final String ATTR_SRC = "src";

    private String startUrl;

    public CadWebComicCrawler(String startUrl, String comicTitle) throws IOException {
        new URL(startUrl);
        this.startUrl = startUrl;
        createDirectory(comicTitle);
    }

    @Override
    public String[] scrapeImageUrls(Document document) {
        Elements comicElements = document.getElementsByClass(COMIC_ELEMENT);
        if (comicElements.size() != 1) {
            String message = String.format("Was expecting 1 element but instead found %d", comicElements.size());
            throw new UnexpectedWebsiteStructure(message);
        }

        Elements links = comicElements.first().select(DIRECT_ANCHOR);
        Elements images = links.select(DIRECT_IMG);
        if (images.size() != 1) {
            String message = String.format("Was expecting 1 image but instead found %d", images.size());
            throw new UnexpectedWebsiteStructure(message);
        }

        return new String[]{images.first().attr(ATTR_SRC)};
    }

    private void createDirectory(String name) throws IOException {
        File dir = new File(name);
        FileUtils.forceMkdir(dir);
    }
}
