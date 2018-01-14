package com.andreouconsulting.webcrawler.webcomic.crawlers;

import com.sun.istack.internal.NotNull;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

abstract public class BaseWebComicCrawler {

    private static final Logger logger = LoggerFactory.getLogger(BaseWebComicCrawler.class);

    public final String startUrl;
    private final String saveLocation;
    public final String comicTitle;
    protected List<String> history;

    public BaseWebComicCrawler(@NotNull final String startUrl, @NotNull final String comicTitle) throws IOException {
        new URL(startUrl);
        this.startUrl = startUrl;

        createDirectory(comicTitle);
        saveLocation = comicTitle;
        this.comicTitle = comicTitle;

        history = new ArrayList<>();
    }

    public Document getPage(final String url) throws IOException {
        logger.info(String.format("Retrieving %s", url));
        Document document = Jsoup.connect(url).get();
        logger.debug(String.format("Retrieved %s successfully", url));

        history.add(document.title());
        return document;
    }

    public static URL createUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            logger.warn(String.format("[%s] was not a valid url. Image URL ignored.", url));
            return null;
        }
    }

    final public int downloadImages(final URL[] imageUrls) {
        logger.info(String.format("Downloading images: \n%s",
                String.join("\n", Arrays.stream(imageUrls).map(URL::toString).collect(Collectors.toList()))));
        int numberOfSuccesses = 0;
        for (URL imageUrl : imageUrls) {
            numberOfSuccesses = hasSavedImageToFile(imageUrl) ? numberOfSuccesses + 1 : numberOfSuccesses;
        }
        return numberOfSuccesses;
    }

    public boolean hasSavedImageToFile(final URL imageUrl) {
        File image = new File(buildImageFilePath(imageUrl));

        logger.debug(String.format("Trying to download [%s]", image));
        try {
            URLConnection connection = imageUrl.openConnection();
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
            FileUtils.copyInputStreamToFile(connection.getInputStream(), image);
        } catch (IOException e) {
            String message = String.format("Could not save [%s] from [%s] because [%s]",
                    image, imageUrl, e.getMessage());
            logger.warn(message);
            return false;
        }
        logger.info(String.format("Saved [%s]", image));
        return true;
    }

    public String buildImageFilePath(final URL imageUrl) {
        String imageUrlString = imageUrl.toString();
        int lastSlash = imageUrlString.lastIndexOf('/');

        String fileName = buildFileName(imageUrlString.substring(lastSlash + 1));

        return saveLocation + File.separator + fileName;
    }

    public String buildFileName(String name) {
        return removeSpecialCharacters(name);
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

    private static void createDirectory(final String name) throws IOException {
        File dir = new File(name);
        FileUtils.forceMkdir(dir);
    }

    abstract public void crawl();

    abstract public URL[] scrapeImageUrls(final Document currentPage);

    abstract public String findNextLink(final Document currentPage);
}
