package com.andreouconsulting.webcrawler.webcomic.crawlers;

import com.andreouconsulting.webcrawler.webcomic.crawlers.exceptions.UnexpectedWebsiteStructure;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class CadWebComicCrawler extends BaseWebComicCrawler {

    private static final Logger logger = LoggerFactory.getLogger(CadWebComicCrawler.class);

    private static final String CSS_DIRECT_ANCHOR = ">a";
    private static final String CSS_DIRECT_IMG = ">img";
    private static final String ATTR_SRC = "src";
    private static final String ATTR_HREF = "href";
    private static final String COMIC_ELEMENT = "comicpage";
    private static final String ATTR_NEXT = "[rel=\"next\"]";

    public CadWebComicCrawler(String startUrl, String comicTitle) throws IOException {
        super(startUrl, comicTitle);
    }

    @Override
    public void crawl() {
        String nextLink = startUrl;
        int imagesFound = 0;
        int imagesDownloaded = 0;
        String warning = "";

        try {
            while (nextLink!= null && nextLink.trim().length() != 0) {
                Document currentPage = getPage(nextLink);

                URL[] imagesUrls = scrapeImageUrls(currentPage);
                imagesFound += imagesUrls.length;

                imagesDownloaded += downloadImages(imagesUrls, getTitle(currentPage));

                nextLink = findNextLink(currentPage);
            }
        } catch (IOException e) {
            warning = String.format("EXITED ABRUPTLY! (%s)", e.getMessage());
        }


        String header = String.format("\n================ [%s] ================\n", comicTitle);
        String footer = String.format("\n================ [%s] ================\n", comicTitle);
        String summaryStructure =
                warning +
                "Visited %d pages.\n" +
                "Found %d images.\n" +
                "Downloaded %d images.\n" +
                "Finished operation. Exiting...";
        String summary = String.format(summaryStructure, history.size(), imagesFound, imagesDownloaded);
        System.out.println(header + summary + footer);
    }

    @Override
    public String getTitle(Document currentPage) {
        return removeSpecialCharacters(
                currentPage.title()
                        .substring(0, currentPage.title().lastIndexOf('|'))
                        .trim());
    }

    @Override
    final public URL[] scrapeImageUrls(final Document currentPage) {
        logger.info(String.format("[%s]: Processing [%s]", currentPage.title(), currentPage.baseUri()));
        logger.debug(String.format("[%s]: Locating [%s] element containing images", currentPage.title(), COMIC_ELEMENT));
        Elements comicElements = currentPage.getElementsByClass(COMIC_ELEMENT);
        if (comicElements.size() != 1) {
            String message = String.format("Was expecting 1 element but instead found %d", comicElements.size());
            throw new UnexpectedWebsiteStructure(message);
        }

        Elements images = comicElements
                .first()
                .select(CSS_DIRECT_ANCHOR)
                .select(CSS_DIRECT_IMG);
        if (images.size() != 1) {
            String message = String.format("Was expecting 1 image but instead found %d", images.size());
            throw new UnexpectedWebsiteStructure(message);
        }

        logger.debug(String.format("[%s]: Constructing image URLs", currentPage.title()));
        return images
                .stream()
                .map( s -> createUrl(s.absUrl(ATTR_SRC)))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new))
                .toArray(new URL[0]);
    }

    @Override
    public String findNextLink(final Document currentPage) {
        logger.info(String.format("[%s]: Finding next link in [%s]", currentPage.title(), currentPage.baseUri()));
        logger.debug(String.format("[%s]: Locating [%s] element containing the next link", currentPage.title(), COMIC_ELEMENT));
        Elements comicElements = currentPage.getElementsByClass(COMIC_ELEMENT);
        if (comicElements.size() != 1) {
            String message = String.format("Was expecting 1 element but instead found %d", comicElements.size());
            throw new UnexpectedWebsiteStructure(message);
        }

        Elements nextLinks = comicElements
                .first()
                .select(CSS_DIRECT_ANCHOR)
                .select(ATTR_NEXT);
        if (nextLinks.size() > 1) {
            String message = String.format("Was expecting 1 link but instead found %d", nextLinks.size());
            throw new UnexpectedWebsiteStructure(message);
        }
        if (nextLinks.size() == 0) {
            logger.info("There is no next link. Probably reached the end for now...");
            return null;
        }

        String nextLink = nextLinks.first().absUrl(ATTR_HREF);
        if (nextLink.trim().length() == 0) {
            String message = String.format("[%s]: Found next link element at [%s] but there was no href attribute.",
                    currentPage.title(), currentPage.baseUri());
            throw new UnexpectedWebsiteStructure(message);
        }

        return nextLink;
    }
}
