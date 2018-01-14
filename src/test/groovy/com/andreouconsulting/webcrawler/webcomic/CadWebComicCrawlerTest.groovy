package com.andreouconsulting.webcrawler.webcomic

import com.andreouconsulting.webcrawler.webcomic.exceptions.UnexpectedWebsiteStructure
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import spock.lang.Specification
import spock.lang.Unroll

class CadWebComicCrawlerTest extends Specification {
    private CadWebComicCrawler webComicCrawler
    private final startUrl = "https://start.url"
    private final comicTitle = "Test Comic"

    def setup() {
        webComicCrawler = new CadWebComicCrawler(startUrl, comicTitle)
    }

    def cleanup() {
        FileUtils.deleteDirectory(new File(comicTitle))
    }

    def 'should create directory when constructor gets called'() {
        when:
        webComicCrawler.toString()

        then:
        assert (new File(comicTitle)).exists()
        assert (new File(comicTitle)).isDirectory()
    }

    @SuppressWarnings("GroovyUnusedAssignment")
    def 'should throw exception when the constructor is called with an invalid URL'() {
        given:
        def startUrl = "start.url"
        def comicTitle = "Test Comic"

        when:
        new CadWebComicCrawler(startUrl, comicTitle)

        then:
        MalformedURLException e = thrown()
    }

    @SuppressWarnings("GroovyUnusedAssignment")
    def 'should throw exception when the html has no comicpage element'() {
        given:
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("cad-noelement.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null)

        when:
        webComicCrawler.scrapeImageUrls(document)

        then:
        UnexpectedWebsiteStructure e = thrown()
    }

    @SuppressWarnings("GroovyUnusedAssignment")
    def 'should throw exception when the html has multiple comicpage element'() {
        given:
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("cad-multielement.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null)

        when:
        webComicCrawler.scrapeImageUrls(document)

        then:
        UnexpectedWebsiteStructure e = thrown()
    }

    def 'should return an array with correct number of URLs when scraping image urls'() {
        given:
        def expectedUrl = new URL("http://cad-comic.com/wp-content/uploads/2017/03/cad-20021024-edecf.jpg")
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("cad.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null)

        when:
        def result = webComicCrawler.scrapeImageUrls(document)

        then:
        assert result.length == 1
        assert result[0].toString() == expectedUrl.toString()
    }

    def 'should return an array with only the valid URLs when scraping image urls (ignore invalid ones)'() {
        given:
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("cad-invalid.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null)

        when:
        def result = webComicCrawler.scrapeImageUrls(document)

        then:
        assert result.length == 0
    }

    @Unroll
    def 'should save images from valid urls and return correct number of images saved'() {
        given:
        def imageUrl = Thread.currentThread().getContextClassLoader().getResource(filename).toURI().toURL()

        when:
        def result = webComicCrawler.downloadImages([imageUrl].toArray(new URL[0]))

        then:
        assert result == expectedSuccesses
        assert (new File(comicTitle + File.separator + filename)).exists() == expectedExistensee

        where:
        filename   | expectedSuccesses | expectedExistensee
        "cert.png" | 1                 | true
    }

    @Unroll
    def 'should return correct number of images saved (will force failure, expect 0)'() {
        given:
        def imageUrl = Thread.currentThread().getContextClassLoader().getResource("cert.png").toURI()
        def srcFile = new File(imageUrl)
        def destFile = new File(imageUrl.getPath() + ".copy")
        FileUtils.copyFile(srcFile, destFile)
        def copyImageUrl = Thread.currentThread().getContextClassLoader().getResource(filename).toURI().toURL()
//        FileUtils.deleteQuietly()
        FileUtils.forceDelete(destFile)

        when:
        def result = webComicCrawler.downloadImages([copyImageUrl].toArray(new URL[0]))

        then:
        assert result == expectedSuccesses
        assert (new File(comicTitle + File.separator + "cert.png")).exists() == expectedExistensee

        where:
        filename        | expectedSuccesses | expectedExistensee
        "cert.png.copy" | 0                 | false
    }
}
