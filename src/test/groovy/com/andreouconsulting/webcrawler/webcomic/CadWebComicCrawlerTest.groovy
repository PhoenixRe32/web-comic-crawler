package com.andreouconsulting.webcrawler.webcomic

import com.andreouconsulting.webcrawler.webcomic.exceptions.UnexpectedWebsiteStructure
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import spock.lang.Specification

class CadWebComicCrawlerTest extends Specification {
    private WebComicCrawler webComicCrawler;
    private final startUrl = "https://start.url"
    private final comicTitle = "Test Comic"

    def setup() {
        webComicCrawler = new CadWebComicCrawler(startUrl, comicTitle)
    }

    def cleanup() {
        FileUtils.deleteDirectory(new File(comicTitle));
    }

    def 'should throw exception when the constructor is called with an invalid URL'() {
        given:
        def startUrl = "start.url"
        def comicTitle = "Test Comic"

        when:
        new CadWebComicCrawler(startUrl, comicTitle)

        then:
        MalformedURLException e = thrown()
    }

    def 'should throw exception when the html has no comicpage element'() {
        given:
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("cad-noelement.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null);

        when:
        webComicCrawler.scrapeImageUrls(document);

        then:
        UnexpectedWebsiteStructure e = thrown()
    }

    def 'should throw exception when the html has multiple comicpage element'() {
        given:
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("cad-multielement.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null);

        when:
        webComicCrawler.scrapeImageUrls(document);

        then:
        UnexpectedWebsiteStructure e = thrown()
    }

    def 'should return a list with correct number of URLs when scraping image urls'() {
        given:
        def expectedUrl = "http://cad-comic.com/wp-content/uploads/2017/03/cad-20021024-edecf.jpg"
        def testPageUri = Thread.currentThread().getContextClassLoader().getResource("cad.html").toURI()
        Document document = Jsoup.parse(new File(testPageUri), null);

        when:
        def result = webComicCrawler.scrapeImageUrls(document);

        then:
        assert result.length == 1
        assert result[0].equals(expectedUrl);
    }
    
}
