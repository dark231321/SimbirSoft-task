package danil.teterin.service.impl;

import danil.teterin.model.Url;
import danil.teterin.model.Word;
import danil.teterin.repository.UrlRepository;
import danil.teterin.repository.WordRepository;
import danil.teterin.service.interfaces.IParseUrl;
import danil.teterin.util.Regex;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ParseUrlImpl implements IParseUrl {

    private final UrlRepository urlRepository;
    private final WordRepository wordRepository;

    @Autowired
    public ParseUrlImpl(UrlRepository urlRepository,
                        WordRepository wordRepository){
        this.urlRepository = urlRepository;
        this.wordRepository = wordRepository;
    }

    private Map<String, Integer> calculateUniqueWords(String text) {
        log.debug("ParseUrlImpl.calculateUniqueWords(String text) was invoked");
        return Arrays.stream(text.split(Regex.DELIMITERS)).collect(Collectors
                .toMap( s->s,
                        s->1,
                        Integer::sum));
    }


    private String parseHtml(URL url) throws IOException {
        log.debug("ParseUrlImpl.parseHtml(URL url) was invoked");
        Document document = Jsoup.connect(url.toExternalForm()).get();
        return document.text();
    }

    private void saveWordList(Map<String, Integer> calculatedUniqueWords, Url url){
        log.debug("ParseUrlImpl.saveWordList(Map<String, Integer> calculatedUniqueWords, Url url) was invoked");
        List<Word> wordList = calculatedUniqueWords.entrySet().stream()
                .map(entry -> Word.builder()
                        .url(url)
                        .word(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        for (Word word : wordList) {
            wordRepository.save(word);
        }
    }

    private Optional<Url> saveUrlWords(URL url){
        log.debug("ParseUrlImpl.saveUrlWords(URL url) was invoked");
        Url newUrl = Url.builder()
                .urlPath(url.toExternalForm())
                .words(new ArrayList<>())
                .build();
        try {
            Url finalNewUrl = newUrl;
            urlRepository.findByUrlPath(url.toExternalForm()).ifPresent(urlDB ->
                    {
                        finalNewUrl.setId(urlDB.getId());
                        this.urlRepository.delete(urlDB);
                    }
            );
            newUrl = urlRepository.save(newUrl);
        }catch (Exception e){
            log.error("Got exception while save url", e);
            System.out.println("Got exception while save url");
            return Optional.empty();
        }
        return  Optional.of(newUrl);

    }

    @Override
    public void parseUrl(String urlString) {
        log.debug("ParseUrlImpl.parseUrl(String url) was invoked");
        try {
            URL url = new URL(urlString);
            Map<String, Integer> calculateUniqueWords = calculateUniqueWords(parseHtml(url));
            Optional<Url> savedUrl = saveUrlWords(url);
            savedUrl.ifPresent(saveUrl -> {
                this.saveWordList(calculateUniqueWords, saveUrl);
                Optional<Url> test = this.urlRepository.findById(savedUrl.get().getId());
                this.urlRepository.findById(savedUrl.get().getId())
                        .ifPresent(newUrlFromDB -> newUrlFromDB.getWords().forEach(System.out::println));
            });
            System.out.println("End parse");
        } catch (MalformedURLException ex){
            log.error("Invalid url {}", urlString, ex);
            System.out.println("Invalid url");
        } catch (IOException ex) {
            log.error("Error parse html body {}", urlString , ex);
            System.out.println("Error parse html body");
        }
    }
}
