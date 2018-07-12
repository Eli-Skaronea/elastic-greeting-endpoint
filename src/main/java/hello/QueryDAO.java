package hello;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component

public class QueryDAO {

    private final RestHighLevelClient client;
    private final SearchSourceBuilder sourceBuilder;
    private final ConfigProps props;
    private final ObjectMapper mapper;

    @Autowired
    public QueryDAO(RestHighLevelClient client, SearchSourceBuilder sourceBuilder,
                    ConfigProps props, ObjectMapper mapper) {
        this.client = client;
        this.sourceBuilder = sourceBuilder;
        this.props = props;
        this.mapper = mapper;
    }

    /**
     * @param greeting
     * @return
     */
    public String createIndex(Greeting greeting) {

        try {
            IndexRequest request = new IndexRequest(props.getIndex().getName(), props.getIndex().getType(), greeting.getId().toString());
//            String jsonString = "{" +
//                    "\"user\":\"kimchy\"," +
//                    "\"postDate\":\"2013-01-30\"," +
//                    "\"message\":\"trying out Elasticsearch\"" +
//                    "}";
            request.source(mapper.writeValueAsString(greeting), XContentType.JSON);
            //request.source(mapper.writeValueAsString(greeting.getContent()), XContentType.JSON);
            IndexResponse response = client.index(request);
            System.out.println("I sent something!");
            return response.getId();
        } catch (Exception ex) {
            System.out.println("The exception was thrown in createIndex method.: " + ex);
            //ex.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @param greeting
     * @return
     */
    public String updateDocument(Greeting greeting){

        try {
            UpdateRequest request = new UpdateRequest(props.getIndex().getName(),
                    props.getIndex().getType(), greeting.getContent())
                    .doc(mapper.writeValueAsString(greeting), XContentType.JSON);

            UpdateResponse response = client.update(request);
            return response.getId();
        } catch (Exception ex){
            System.out.println("The exception was thrown in updateDocument method.: " + ex);
        }

        return null;
    }

    /**
     *
     * @return
     */
    public List<Greeting> matchAllQuery() {

        List<Greeting> result = new ArrayList<>();

        try {
            flush();
            result = getGreeting(QueryBuilders.matchAllQuery());
        } catch (Exception ex){
            System.out.println("The exception was thrown in matchAllQuery method.: " + ex);
        }

        return result;
    }


    /**
     *
     * @return
     */
    private SearchRequest getSearchRequest(){
        SearchRequest searchRequest = new SearchRequest(props.getIndex().getName());
        searchRequest.source(sourceBuilder);
        return searchRequest;
    }

    /**
     *
     * @param builder
     * @return
     * @throws IOException
     */
    private List<Greeting> getGreeting(AbstractQueryBuilder builder) throws IOException {
        List<Greeting> result = new ArrayList<>();

        sourceBuilder.query(builder);
        SearchRequest searchRequest = getSearchRequest();

        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            Greeting greeting = mapper.readValue(hit.getSourceAsString(), Greeting.class);
            greeting.setContent(hit.getId());
            result.add(greeting);
        }

        return result;
    }

    public void flush() throws IOException {
        String endPoint = String.join("/", props.getIndex().getName(), "_flush");
        client.getLowLevelClient().performRequest("POST", endPoint);
    }

}
