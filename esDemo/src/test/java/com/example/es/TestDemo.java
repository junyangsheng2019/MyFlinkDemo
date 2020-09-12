package com.example.es;

import com.example.es.esutlis.InitRestHighLevelClient;
import com.example.es.movie.MovieIndexTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = EsApplication.class)
@RunWith(SpringRunner.class)
public class TestDemo {

    @Resource
    private RestHighLevelClient client;

    public static void main(String[] args) throws IOException {
        // 1 获取连接客户端

        RestHighLevelClient client = InitRestHighLevelClient.getClient();

        // 2构建请求    PUT /book/_doc/1

        GetRequest request = new GetRequest("book","1");

        // 3执行

        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        // 4获取结果

        System.out.println(response.getIndex());

        System.out.println(response.getType());

        System.out.println(response.getId());

        System.out.println(response.getVersion());

        System.out.println(response.getSeqNo());

        System.out.println(response.getPrimaryTerm());

        System.out.println(response.isExists());

        System.out.println(response.getSourceAsString());

    }
    /**
     * 查询
     */
    @Test
    public void testSearch(){
//
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        boolQueryBuilder.must(
//                QueryBuilders.multiMatchQuery("name,area"),"喜剧之王","内地")
//
//        )


    }

    /**  测试该索引movie_index
     * UDHaWmgBBHn7EfncqRNC
     * @throws UnknownHostException
     */
    @Test
    public void testSearchOne() throws UnknownHostException {

       org.elasticsearch.action.get.GetResponse
        response =  InitRestHighLevelClient.getESData("movie_index","_doc","UDHaWmgBBHn7EfncqRNC",null);
        System.out.println(response.getIndex());

        System.out.println(response.getType());

        System.out.println(response.getId());

        System.out.println(response.getVersion());

        System.out.println(response.getSeqNo());

        System.out.println(response.getPrimaryTerm());

        System.out.println(response.isExists());

        System.out.println(response.getSourceAsString());
    }
    /**
     * 查询药神 movie_index
     * @throws IOException
     */
//    @Test
//    public void test() throws IOException {
////        # 创建一个Search 对象
//        SearchRequest searchRequest = new SearchRequest();
//
////        # 创建一个Builder 对象 对条件进行封装
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
////        # 查询条件: 字段名为text  内容含有19的数据
//        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "我不是药神"));
////        # 查询条件: 在上面条件的基础上 加上字段 jiage 内容含有329的数据
//        searchSourceBuilder.query(QueryBuilders.matchQuery("release", "2018"));
//        //        # 从搜索结果中取第0条开始的10条数据，数据量最多不要超过10000 会报错，有解决方案百度
//        searchSourceBuilder.from(0);
//        searchSourceBuilder.size(10);
//
////        # 排序根据字段id 按照正序排列
//        searchSourceBuilder.sort(new FieldSortBuilder("id").order(SortOrder.ASC));
//
//        searchSourceBuilder.fetchSource(false);
//
////        # 参数，用于是否需要过滤
//        String[] includeFields = new String[]{"name", "actors","label","introduction"};
////        # 第1个参数是 需要显示的字段，第2个参数是需要过滤的字段
//        searchSourceBuilder.fetchSource(includeFields, null);
//
////        # 进行构建
//        searchRequest.source(searchSourceBuilder);
//        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
//
//        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
//
////        # 获取数据 数据可以是多种类型的
//        SearchHits hits = searchResponse.getHits();
//        SearchHit[] hits1 = hits.getHits();
//
//        List list = new ArrayList();
//        for (SearchHit hit : hits1) {
//            list.add(hit.getSourceAsString());
//        }
//        System.out.println(list);
//
//}

    @Test
    public void bulkOption() throws JsonProcessingException {
        MovieIndexTemplate indexTemplate = new MovieIndexTemplate();
        indexTemplate.setName("喜剧之王");
        indexTemplate.setAlias("喜剧之王");
        indexTemplate.setActors("周星驰，莫文蔚，张柏芝，吴孟达，林子善，田启文");
        indexTemplate.setDirectors("周星驰  李力持");
        indexTemplate.setIntroduction("《喜剧之王》是星辉海外有限公司出品的一部喜剧电影，由李力持、周星驰执导，周星驰、 莫文蔚、张柏芝等主演。该片于1999年2月13日在香港上映。影片讲述对喜剧情有独钟的尹天仇与舞女柳飘飘逐渐产生感情，之后在杜娟儿的帮助下，尹天仇终于获得机会演主角，但又陷入与柳飘飘、杜娟儿的三角恋漩涡之中");
        indexTemplate.setArea("内地");
        indexTemplate.setScore(9.4f);
        indexTemplate.setLabel("喜剧 爱情");
        indexTemplate.setRelease("1999");

        String id = "UDHaWmgBBHn7EfncqRNC";
//        movieSearchService.buldOption(indexTemplate,id);
    }

}
