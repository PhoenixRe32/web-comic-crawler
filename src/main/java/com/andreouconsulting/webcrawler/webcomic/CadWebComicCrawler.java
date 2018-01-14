package com.andreouconsulting.webcrawler.webcomic;

import com.andreouconsulting.webcrawler.webcomic.exceptions.UnexpectedWebsiteStructure;

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

    private static final String COMIC_ELEMENT = "comicpage";

    public CadWebComicCrawler(String startUrl, String comicTitle) throws IOException {
        super(startUrl, comicTitle);
    }

    @Override
    final public URL[] scrapeImageUrls(final Document document) {
        logger.info(String.format("Processing [%s] at [%s]", document.title(), document.baseUri()));
        logger.debug(String.format("[%s]: Locating element containing images", document.title()));
        Elements comicElements = document.getElementsByClass(COMIC_ELEMENT);
        if (comicElements.size() != 1) {
            String message = String.format("Was expecting 1 element but instead found %d", comicElements.size());
            throw new UnexpectedWebsiteStructure(message);
        }

        Elements images = comicElements
                .first()
                .select(DIRECT_ANCHOR)
                .select(DIRECT_IMG);
        if (images.size() != 1) {
            String message = String.format("Was expecting 1 image but instead found %d", images.size());
            throw new UnexpectedWebsiteStructure(message);
        }

        logger.debug(String.format("[%s]: Constructing image URLs", document.title()));
        return images
                .stream()
                .map( s -> createUrl(images.first().absUrl(ATTR_SRC)))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new))
                .toArray(new URL[0]);
    }

//    @Override
    final public int downloadImages(final URL[] imageUrls) {
        int numberOfSuccesses = 0;
        for (URL imageUrl : imageUrls) {
            numberOfSuccesses = hasSavedImageToFile(imageUrl) ? numberOfSuccesses + 1 : numberOfSuccesses;
        }
        return numberOfSuccesses;
    }
}
