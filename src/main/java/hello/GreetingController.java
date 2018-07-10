package hello;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@RestController
public class GreetingController {
    private QueryDAO dao;

    public GreetingController(QueryDAO dao) {
        this.dao = dao;
    }

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();


    @GetMapping(value = "/greeting", produces = "application/json; charset=utf-8")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        Greeting greetings = new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
        dao.createIndex(greetings);
        return greetings;
    }

    @PostMapping(value = "/update", consumes = "application/json; charset=utf-8")
    public String update(@RequestBody Greeting greetings){
        return dao.updateDocument(greetings);
    }

    @GetMapping(value = "/all", produces = "application/json; charset=utf-8")
    public List<Greeting> getAllGreetings() {
        return dao.matchAllQuery();
    }

}