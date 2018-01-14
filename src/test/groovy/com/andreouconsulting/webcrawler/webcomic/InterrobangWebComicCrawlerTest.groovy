package com.andreouconsulting.webcrawler.webcomic

import com.andreouconsulting.webcrawler.webcomic.crawlers.InterrobangWebComicCrawler
import com.andreouconsulting.webcrawler.webcomic.crawlers.exceptions.UnexpectedWebsiteStructure
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import spock.lang.Ignore
import spock.lang.Specification

class InterrobangWebComicCrawlerTest extends Specification {
    private InterrobangWebComicCrawler webComicCrawler
    private final startUrl = "https://start.url"
    private final comicTitle = "Test Comic"

    def setup() {
        webComicCrawler = new InterrobangWebComicCrawler(startUrl, comicTitle)
    }

    def cleanup() {
        FileUtils.deleteDirectory(new File(comicTitle))
    }

    @SuppressWarnings("GroovyUnusedAssignment")
    def 'should throw exception when the html has no comicpage element'() {
        given:
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("interrobang-noelement.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null)

        when:
        webComicCrawler.scrapeImageUrls(document)

        then:
        UnexpectedWebsiteStructure e = thrown()
    }

    def 'should return an array with correct number of URLs when scraping image urls'() {
        given:
        def expectedUrl = new URL("http://www.interrobangstudios.com/images/comics/4c11ae87aa32f.jpg")
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("interrobang.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null)

        when:
        def result = webComicCrawler.scrapeImageUrls(document)

        then:
        assert result.length == 1
        assert result[0].toString() == expectedUrl.toString()
    }

    def 'should return an array with only the valid URLs when scraping image urls (ignore invalid ones)'() {
        given:
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("interrobang-invalid.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null)

        when:
        def result = webComicCrawler.scrapeImageUrls(document)

        then:
        assert result.length == 0
    }

    @Ignore
    def 'should return the link to the next page'() {
        given:
        def expectedLink = "comics-display.php?strip_id=943"
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("interrobang.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null)

        when:
        def result = webComicCrawler.findNextLink(document)

        then:
        assert result == expectedLink
    }

    def 'should return null if there is not next link'() {
        given:
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("interrobang-noelement.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null)

        when:
        def result = webComicCrawler.findNextLink(document)

        then:
        assert result == null
    }

    @SuppressWarnings("GroovyUnusedAssignment")
    def 'should throw exception if there is next link but no href attribute'() {
        given:
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("interrobang-invalid.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null)

        when:
        webComicCrawler.findNextLink(document)

        then:
        UnexpectedWebsiteStructure e = thrown()
    }
}
