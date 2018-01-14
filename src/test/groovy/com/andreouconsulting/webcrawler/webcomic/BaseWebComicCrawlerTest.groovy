package com.andreouconsulting.webcrawler.webcomic

import org.apache.commons.io.FileUtils
import spock.lang.Specification

class BaseWebComicCrawlerTest extends Specification {
    private BaseWebComicCrawler webComicCrawler
    private final startUrl = "https://start.url"
    private final comicTitle = "Test Comic"

    def cleanup() {
        FileUtils.deleteDirectory(new File(comicTitle))
    }

    def 'should create directory when constructor gets called'() {
        when:
        new CadWebComicCrawler(startUrl, comicTitle)

        then:
        assert (new File(comicTitle)).exists()
        assert (new File(comicTitle)).isDirectory()
    }

    @SuppressWarnings("GroovyUnusedAssignment")
    def 'should throw exception when the constructor is called with an invalid URL'() {
        given:
        def invalidUrl = "start.url"

        when:
        new CadWebComicCrawler(invalidUrl, comicTitle)

        then:
        MalformedURLException e = thrown()
    }

    def 'should return null when creating URL with an invalid url string'() {
        given:
        def invalidUrl = "invalid.url"

        when:
        def result = BaseWebComicCrawler.createUrl(invalidUrl)

        then:
        assert result == null
    }

    def 'should save images from valid urls and return true'() {
        given:
        webComicCrawler = new CadWebComicCrawler(startUrl, comicTitle)
        def fileName = "cert.png"
        def imageUrl = Thread.currentThread().getContextClassLoader().getResource(fileName).toURI().toURL()

        when:
        def result = webComicCrawler.hasSavedImageToFile(imageUrl)

        then:
        assert result
        assert (new File(comicTitle + File.separator + fileName)).exists()
    }

    def 'should not save images and return false'() {
        given:
        webComicCrawler = new CadWebComicCrawler(startUrl, comicTitle)
        def filename = "cert.png"
        def srcUrl = Thread.currentThread().getContextClassLoader().getResource(filename).toURI()

        def srcFile = new File(srcUrl)
        def destFile = new File(srcUrl.getPath() + ".copy")

        FileUtils.copyFile(srcFile, destFile)
        def destUrl = Thread.currentThread().getContextClassLoader().getResource(filename + ".copy").toURI().toURL()
        FileUtils.forceDelete(destFile)

        when:
        def result = webComicCrawler.hasSavedImageToFile(destUrl)

        then:
        assert !result
        assert !(new File(comicTitle + File.separator + "cert.png")).exists()
    }
}
