package com.andreouconsulting.webcrawler.webcomic

import com.andreouconsulting.webcrawler.webcomic.exceptions.UnexpectedWebsiteStructure
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import spock.lang.Specification

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

    def 'should save images from valid urls and return correct number of images saved'() {
        given:
        def filename = "cert.png"
        def imageUrl = Thread.currentThread().getContextClassLoader().getResource(filename).toURI().toURL()

        when:
        def numberOfSuccessfulSaves = webComicCrawler.downloadImages([imageUrl].toArray(new URL[0]))

        then:
        assert numberOfSuccessfulSaves == 1
        assert (new File(comicTitle + File.separator + filename)).exists()
    }

    def 'should return correct number of images saved (will force failure, expect 0)'() {
        given:
        def filename = "cert.png"
        def srcUrl = Thread.currentThread().getContextClassLoader().getResource(filename).toURI()

        def srcFile = new File(srcUrl)
        def destFile = new File(srcUrl.getPath() + ".copy")

        FileUtils.copyFile(srcFile, destFile)
        def destUrl = Thread.currentThread().getContextClassLoader().getResource(filename + ".copy").toURI().toURL()
        FileUtils.forceDelete(destFile)

        when:
        def numberOfSuccessfulSaves = webComicCrawler.downloadImages([destUrl].toArray(new URL[0]))

        then:
        assert numberOfSuccessfulSaves == 0
        assert !(new File(comicTitle + File.separator + "cert.png")).exists()
    }
}
