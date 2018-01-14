package com.andreouconsulting.webcrawler.webcomic.crawlers;

import com.andreouconsulting.webcrawler.webcomic.crawlers.exceptions.UnexpectedWebsiteStructure;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class InterrobangWebComicCrawler extends BaseWebComicCrawler {

    private static final Logger logger = LoggerFactory.getLogger(InterrobangWebComicCrawler.class);

    private static final String COMIC_ELEMENT = "meta[property]";
    private static final String PROP_TITLE = "[property=\"og:title\"]";
    private static final String ATTR_CONTENT = "content";
    private static final String PROP_IMAGE = "[property=\"og:image\"]";
    private static final String NEXT_LINK = "comic-rightnav";
    private static final String CSS_ANCHOR = "a[href]";
    private static final String ATTR_HREF = "href";
    public static final String CSS_ANCHOR_INC = "a";

    public InterrobangWebComicCrawler(String startUrl, String comicTitle) throws IOException {
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
        return removeSpecialCharacters(currentPage
                .head()
                .select(PROP_TITLE)
                .attr(ATTR_CONTENT));
    }

    @Override
    final public URL[] scrapeImageUrls(final Document currentPage) {
        logger.info(String.format("[%s]: Processing [%s]", currentPage.title(), currentPage.baseUri()));
        logger.debug(String.format("[%s]: Locating [%s] element containing images", currentPage.title(), COMIC_ELEMENT));
        Elements comicElements = currentPage.head().select(COMIC_ELEMENT);
        if (comicElements.size() == 0) {
            throw new UnexpectedWebsiteStructure("Was expecting multiple element but instead found 0");
        }

        Elements images = comicElements.select(PROP_IMAGE);
        if (images.size() != 1 ) {
            throw new UnexpectedWebsiteStructure("Was expecting 1 image but didn't find any");
        }

        logger.debug(String.format("[%s]: Constructing image URLs", currentPage.title()));
        return images
                .stream()
                .map( s -> createUrl(s.absUrl(ATTR_CONTENT)))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new))
                .toArray(new URL[0]);
    }

    @Override
    public String findNextLink(final Document currentPage) {
        logger.info(String.format("[%s]: Finding next link in [%s]", currentPage.title(), currentPage.baseUri()));
        logger.debug(String.format("[%s]: Locating [%s] element containing the next link", currentPage.title(), NEXT_LINK));
        Elements nextLinks = currentPage.getElementsByClass(NEXT_LINK);
        if (nextLinks.size() == 0) {
            String message = String.format("Was expecting some elements but instead found %d", nextLinks.size());
            throw new UnexpectedWebsiteStructure(message);
        }

        Element nextLinkElement = nextLinks
                .first()
                .child(0);
        if (nextLinkElement == null ) {
            throw new UnexpectedWebsiteStructure("Was expecting an element");
        }
        if (!nextLinkElement.is(CSS_ANCHOR) && nextLinkElement.is(CSS_ANCHOR_INC)) {
            String message = String.format("[%s]: Found next link element at [%s] but there was no href attribute.",
                    currentPage.title(), currentPage.baseUri());
            throw new UnexpectedWebsiteStructure(message);
        }
        if (!nextLinkElement.is(CSS_ANCHOR_INC)) {
            String message = String.format("Was expecting an anchor with a link but instead found [%s]", nextLinkElement);
            logger.info("There is no next link. Probably reached the end for now...");
            return null;
        }

        String nextLink = nextLinkElement.absUrl(ATTR_HREF);
        if (nextLink.trim().length() == 0) {
            String message = String.format("[%s]: Found next link element at [%s] but there was no href attribute.",
                    currentPage.title(), currentPage.baseUri());
            throw new UnexpectedWebsiteStructure(message);
        }

        return nextLink;
    }
}
