package hello;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Greeting {

    private Integer id;
    private String content;

    public Greeting(Integer id, String content) {
        this.id = id;
        this.content = content;

    }

    public Integer getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(Integer id){
        this.id = id;
    }
}