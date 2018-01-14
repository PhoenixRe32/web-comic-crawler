package com.andreouconsulting.webcrawler.webcomic;

import com.andreouconsulting.webcrawler.webcomic.exceptions.UnexpectedWebsiteStructure;
import com.sun.istack.internal.NotNull;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class CadWebComicCrawler implements WebComicCrawler {

    private static final Logger logger = LoggerFactory.getLogger(WebComicCrawler.class);
    private static final String COMIC_ELEMENT = "comicpage";
    private static final String DIRECT_ANCHOR = ">a[href]";
    private static final String DIRECT_IMG = ">img";
    private static final String ATTR_SRC = "src";

    private final String startUrl;
    private final String saveLocation;

    public CadWebComicCrawler(@NotNull final String startUrl, @NotNull final String comicTitle) throws IOException {
        new URL(startUrl);
        this.startUrl = startUrl;
        createDirectory(comicTitle);
        this.saveLocation = comicTitle;
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

    private static URL createUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            logger.warn(String.format("[%s] was not a valid url. Image URL ignored.", url));
            return null;
        }
    }

//    @Override
    final public int downloadImages(final URL[] imageUrls) {
        int numberOfSuccesses = 0;
        for (URL imageUrl : imageUrls) {
            numberOfSuccesses = hasSavedImageToFile(imageUrl) ? numberOfSuccesses + 1 : numberOfSuccesses;
        }
        return numberOfSuccesses;
    }

    private boolean hasSavedImageToFile(final URL imageUrl) {
        String imageFilePath =  buildImageFilePath(imageUrl);
        File image = new File(imageFilePath);

        try {
            URLConnection connection = imageUrl.openConnection();
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
            FileUtils.copyInputStreamToFile(connection.getInputStream(), image);
        } catch (IOException e) {
            String message = String.format("Could not save [%s] from [%s] because [%s]",
                    imageFilePath, imageUrl, e.getMessage());
            logger.warn(message);
            return false;
        }
        logger.debug(String.format("Saved [%s]", imageFilePath));
        return true;
    }

    private String buildImageFilePath(final URL imageUrl) {
        String imageUrlString = imageUrl.toString();
        int lastSlash = imageUrlString.lastIndexOf('/');
        String imageName = removeSpecialCharacters(imageUrlString.substring(lastSlash + 1));

        return saveLocation + File.separator + imageName;
    }

    private static String removeSpecialCharacters(String name) {
        return name.replace("?", "")
                .replace("<", "")
                .replace(">", "")
                .replace("*", "")
                .replace(":", "")
                .replace("|", "")
                .replace("/", "")
                .replace("\\", "");
    }

    private void createDirectory(final String name) throws IOException {
        File dir = new File(name);
        FileUtils.forceMkdir(dir);
    }
}
