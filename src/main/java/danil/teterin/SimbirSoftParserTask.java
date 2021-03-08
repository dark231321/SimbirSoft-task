package danil.teterin;


import danil.teterin.service.impl.ParseUrlImpl;
import danil.teterin.service.interfaces.IParseUrl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SimbirSoftParserTask {
    public static void main(String[] args){
        ApplicationContext applicationContext = SpringApplication.run(SimbirSoftParserTask.class, args);
        IParseUrl parserUrl = applicationContext.getBean(ParseUrlImpl.class);
        if(args.length != 1) {
            System.out.println("Enter one arg: link to download and count");
            System.exit(1);
        }
        parserUrl.parseUrl(args[0]);
        System.exit(-1);
    }
}
