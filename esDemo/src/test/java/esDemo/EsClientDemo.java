package esDemo;

import ch.qos.logback.core.util.COWArrayList;
import com.example.es.esDemo.EsClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.util.IOUtils;
import org.assertj.core.util.Lists;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.io.stream.ByteBufferStreamInput;
import org.elasticsearch.common.io.stream.InputStreamStreamInput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.translog.BufferedChecksumStreamInput;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedValueCount;
import org.elasticsearch.search.aggregations.pipeline.BucketSelectorPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

/**
 * @description: new class
 * @author: shengjunyang
 * @date:2020/9/27 10:27 下午
 */
@SpringBootTest
@Slf4j
public class EsClientDemo {
    private static final String INDEX_PAGEACCESS ="label_usertrait" ;
    /**
     * 添加文档
     * @throws IOException
     *
     */
    private RestHighLevelClient client;

    @Before
    public void initClient() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
    }

    @After
    public void destroy() throws IOException {
        IOUtils.close(client);
    }

    /**
     *  测试scroll
     *  index bank type:account
     */
    @Test
    public void testScroll()throws IOException{
        //Elasticsearch 需要保持搜索的上下文环境多长时间
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        SearchRequest searchRequest = new SearchRequest("bank");
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchAllQuery());//查询条件
        searchSourceBuilder.size(15);//每次查询返回的数据条数
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();//获取scrollId
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        while (searchHits != null && searchHits.length > 0) {
            //创建新的scrollSearch
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            log.info("print scrollId:{}",scrollId);
            searchHits = searchResponse.getHits().getHits();
            //获取数据searchHits
            System.out.println("searchHits Num:"+ Arrays.stream(searchResponse.getHits().getHits()).findFirst());
        }
        //清除滚屏
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        boolean succeeded = clearScrollResponse.isSucceeded();
    }
    /**
     *  测试普通搜索
     *  index bank type:account
     */
    @Test
    public void testSearch()throws IOException{
        GetRequest getRequest = new GetRequest(
                "bank","account","1");
        getRequest.refresh(true);//刷新
//        getRequest.version(1);//版本
        GetResponse response = client.get(getRequest,RequestOptions.DEFAULT);
        System.out.println(response);
    }

    @Test
    public void testCreateIndex() throws IOException {
        //Low Level Client init
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
        IndexRequest request = new IndexRequest(
                "index_test", //index name
                "doc",  // type
                "1");   // doc id
        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        request.source(jsonString, XContentType.JSON);
        request.version(1);//版本
        //同步执行
//        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
//        System.out.println(indexResponse.toString());

        //通过Map的方式提供文档Source
        Map<String,Object> jsonMap = new HashMap<>();
        jsonMap.put("user","Jack");
        jsonMap.put("postDate",new Date());
        jsonMap.put("message","I am try to learn ES");
        IndexRequest indexRequest = new IndexRequest(
                "index_test","doc","2").source(jsonMap);
        client.index(indexRequest,RequestOptions.DEFAULT);
        //XcondeteBuilder
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        { builder.field("user","Tom");
        builder.field("postDate",new Date());
        builder.field("message","trying to learn ES");
        }
        builder.endObject();
        IndexRequest indexBuilderRequest = new IndexRequest(
                "index_test","doc","2").source(builder);
    }
    @Test
    public void testGetDocument()throws Exception{
            GetRequest getRequest = new GetRequest(
                    "index_test","doc","1");
            getRequest.refresh(true);//刷新
            getRequest.version(1);//版本
            GetResponse response = client.get(getRequest,RequestOptions.DEFAULT);
            System.out.println(response);

        }

        @Test
        public void testDelete() throws Exception {
            DeleteRequest request = new DeleteRequest(
                    "index_test", "doc", "1");
            DeleteResponse deleteResponse = client.delete(request,RequestOptions.DEFAULT);
            System.out.println(deleteResponse.toString());
        }

        @Test
       public void testAggregation() throws IOException {
            //查询时间范围在dataRange
            RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder("purchase_details.date").format("yyyyMMdd").from(20200409).to(20200910);
            NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("purchase_details", rangeQueryBuilder, ScoreMode.Avg);

            TermsQueryBuilder filterQB =  new TermsQueryBuilder("purchase_details.action_type","2");
            NestedQueryBuilder actionTypeNestedQuery = QueryBuilders.nestedQuery("purchase_details",filterQB,ScoreMode.Avg);

            System.out.println(rangeQueryBuilder.toString());
            //聚合筛选userIdAggs
            TermsAggregationBuilder userIdAggs = AggregationBuilders.terms("userId_aggs").field("user_id").size(10000);
            //声明bucketPath用于后面的bucket筛选
            Map bucketsPathMap = new HashMap<>();
            bucketsPathMap.put("sumPrice", "subA>sum_price");
            //设置脚本
            Script script = new Script("params.sumPrice>300");
            //构建bucket二次聚合选择器
            BucketSelectorPipelineAggregationBuilder subA_selector = PipelineAggregatorBuilders.bucketSelector("subA_selector", bucketsPathMap, script);
            userIdAggs.subAggregation(subA_selector);
            //构建和bucket聚合同级的subA
            NestedAggregationBuilder subA = AggregationBuilders.nested("subA", "purchase_details")
//                    .subAggregation(AggregationBuilders.filter("filter_aggs",
//                            QueryBuilders.termQuery("purchase_details.action_type", 2))
                            .subAggregation(AggregationBuilders.sum("sum_price").field("purchase_details.price"));

            //汇总1：rangeQUeryBuilder
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(rangeQueryBuilder);
            //汇总2：复杂aggs
            TermsAggregationBuilder aggsQuery = userIdAggs.subAggregation(subA);

            // 1、创建search请求
            //SearchRequest searchRequest = new SearchRequest();
            SearchRequest searchRequest = new SearchRequest(INDEX_PAGEACCESS);
            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.size(0);
            //3.添加简单查询
            BoolQueryBuilder noAggQuery = QueryBuilders.boolQuery().filter(nestedQuery).filter(filterQB);
            sourceBuilder.query(noAggQuery);

            //4.添加嵌套聚合查询
            sourceBuilder.aggregation(userIdAggs);
            //5.把builder加入到SearchRequest
            searchRequest.source(sourceBuilder);
            System.out.println("dsl"+sourceBuilder.toString());
            SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
            //4、处理响应
            //搜索结果状态信息
            log.info("searchResponse:",searchResponse.toString());
            if(RestStatus.OK.equals(searchResponse.status())) {
                // 获取聚合结果
                Aggregations aggregations = searchResponse.getAggregations();
                Terms byAgeAggregation = aggregations.get("userId_aggs");

            }
        }


            //测试业兵老师的query提前到外层
    @Test
    public void testAggregation2() throws IOException {
        //查询时间范围在dataRange
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.nestedQuery("purchase_details", QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("purchase_details.date").gt("2020-02-05 00:00:00").lt("2020-08-15 00:00:00"))
                        .must(QueryBuilders.termQuery("purchase_details.action_type", "2")), ScoreMode.None));
        //                        .must(QueryBuilders.termsQuery("purchase_details.product_type","purchase_details_product_type_775ab3b9","purchase_details_product_type_bde4430d")), ScoreMode.None));
        //聚合筛选aggQueryBuilder  查询的时候使用scrollAPI
        AggregationBuilder aggQueryBuilder = AggregationBuilders.terms("userId_aggs").field("user_id").size(10000);
        //声明bucketPath用于后面的bucket筛选
        Map<String, String> bucketsPathMap = new HashMap<>();
        bucketsPathMap.put("sumPrice", "subA>sum_price");
        bucketsPathMap.put("sumTimes", "subA>sum_times");

        //设置脚本
        Script script = new Script("params.sumPrice>300 && params.sumTimes>1");

        //构建bucket二次聚合选择器
        BucketSelectorPipelineAggregationBuilder subA_selector = PipelineAggregatorBuilders
                .bucketSelector("subA_selector", bucketsPathMap, script);
        //构建和bucket聚合同级的subA
        NestedAggregationBuilder subA = AggregationBuilders.nested("subA", "purchase_details")
                .subAggregation(AggregationBuilders.sum("sum_price").field("purchase_details.price"))
                //增加times的聚合sum操作
                .subAggregation(AggregationBuilders.sum("sum_times").field("purchase_details.times"));

        //汇总2：复杂aggs
        aggQueryBuilder.subAggregation(subA_selector);
        aggQueryBuilder.subAggregation(subA);

        // 1、创建search请求
        //SearchRequest searchRequest = new SearchRequest();
        SearchRequest searchRequest = new SearchRequest(INDEX_PAGEACCESS);
        // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(1);
        //3.添加简单查询
        sourceBuilder.query(queryBuilder);
        //4.添加嵌套聚合查询
        sourceBuilder.aggregation(aggQueryBuilder);
        //5.把builder加入到SearchRequest
        searchRequest.source(sourceBuilder);
        System.out.println("dsl" + sourceBuilder.toString());
        //结果：{"took":143,"timed_out":false,"_shards":{"total":1,"successful":1,"skipped":0,"failed":0},"hits":{"total":{"value":2,"relation":"eq"},"max_score":null,"hits":[]},"aggregations":{"lterms#userId_aggs":{"doc_count_error_upper_bound":0,"sum_other_doc_count":0,"buckets":[{"key":91597005,"doc_count":1,"nested#subA":{"doc_count":1,"sum#sum_times":{"value":3.0},"sum#sum_price":{"value":600.0}}},{"key":91639474,"doc_count":1,"nested#subA":{"doc_count":1,"sum#sum_times":{"value":3.0},"sum#sum_price":{"value":400.0}}}]}}}
        //创建Scroll设定滚动时间间隔,60秒,不是处理查询结果的所有文档的所需时间
        final Scroll scroll = new Scroll(TimeValue.timeValueSeconds(1));
        searchRequest.scroll(scroll);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        searchResponse.getAggregations().getAsMap();
        String scrollId = null;
        List<String> userIdList = new ArrayList<>();
        log.info("sssss"+searchResponse.toString());
        while (searchResponse.getAggregations() != null && searchResponse.getAggregations().getAsMap().size() > 0) {
            // handle data
            for (Aggregation aggregation : searchResponse.getAggregations()) {
                Terms byAgeAggregation = searchResponse.getAggregations().get("userId_aggs");
                for (Terms.Bucket buck : byAgeAggregation.getBuckets()) {
                    userIdList.add(buck.getKeyAsString());
                }
                log.info("userIdList size:{},userList:{}", userIdList.size(),userIdList.toString());
            }
            //每次循环完后取得scrollId,用来记录下次从这个游标开始取数
            scrollId = searchResponse.getScrollId();
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
//            SearchRequest searchRequest1 = new SearchRequest(INDEX_PAGEACCESS);
            try {
                //进行下次查询
                searchResponse = client.searchScroll(scrollRequest, RequestOptions.DEFAULT);
//                log.info("searchResp:" + searchResponse.getAggregations().getAsMap());
            } catch (IOException e) {
                log.error("数据查询错误", e);
            }
        }

        //清除滚屏
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = null;
        try {
            clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.warn("清楚滚屏错误", e);
        }
        boolean successed = false;
        if (clearScrollResponse != null) {
            successed = clearScrollResponse.isSucceeded();
        }

        // 当有大量不同值时，ES只返回数量最多的项。这个数字表示有多少文档的统计数量没有返回。
    }

    /**
     * 批量查询
     * @throws IOException
     */
    @Test
    public void testDSLByAfter()throws IOException{
        //查询时间范围在dataRange
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.nestedQuery("purchase_details",
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("purchase_details.date").gt("2020-02-05 00:00:00")
                                .lt("2021-02-05 00:00:00"))
                        .must(QueryBuilders.termQuery("purchase_details.action_type", "2")), ScoreMode.None));

        //声明bucketPath用于后面的bucket筛选
        Map<String, String> bucketsPathMap = new HashMap<>();
        bucketsPathMap.put("sumPrice", "subA>sum_price");
        bucketsPathMap.put("sumTimes", "subA>sum_times");

        Script script = new Script("params.sumPrice>300 && params.sumTimes>1");
        BucketSelectorPipelineAggregationBuilder subA_selector = PipelineAggregatorBuilders
                .bucketSelector("subA_selector", bucketsPathMap, script);

        //构建和bucket聚合同级的subA
        NestedAggregationBuilder subA = AggregationBuilders.nested("subA", "purchase_details")
                .subAggregation(AggregationBuilders.sum("sum_price").field("purchase_details.price"))
                .subAggregation(AggregationBuilders.sum("sum_times").field("purchase_details.times"));

        // aggQueryBuilder.subAggregation(subA_selector);
        // aggQueryBuilder.subAggregation(subA);

        // 1、创建search请求
        //SearchRequest searchRequest = new SearchRequest();
        SearchRequest searchRequest = new SearchRequest(INDEX_PAGEACCESS);
        // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(0);
        sourceBuilder.trackTotalHits(true);
        //3.添加简单查询
        sourceBuilder.query(queryBuilder);

        // composite aggs
        List<CompositeValuesSourceBuilder<?>> compositeValuesList = Lists.newArrayList();

        TermsValuesSourceBuilder termValBuilder = new TermsValuesSourceBuilder("user_id").field("user_id");
        compositeValuesList.add(termValBuilder);

        CompositeAggregationBuilder compositeAggs = AggregationBuilders
                .composite("composite_buckets", compositeValuesList);
        compositeAggs.subAggregation(subA);
        compositeAggs.subAggregation(subA_selector);
        compositeAggs.size(100);

        //4.添加嵌套聚合查询
        sourceBuilder.aggregation(compositeAggs);

        //5.把builder加入到SearchRequest
        searchRequest.source(sourceBuilder);
        log.info("dsl:{}", sourceBuilder.toString());
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        //4、处理响应
        Aggregations aggregations = searchResponse.getAggregations();
        System.out.println(aggregations.asList().toString());
        List<String> userIdList= new ArrayList<>();
        for (Aggregation agg : searchResponse.getAggregations()) {
            Aggregation aggregation = searchResponse.getAggregations().get("composite_buckets");
            Map<String, Object> meta = aggregation.getMetaData();
            log.info("userIdList size:{},list:{}", userIdList.size(),userIdList.toString());
        }
        //搜索结果状态信息
    }
    //用dsl查询
    @Test
    public void testSearchDocument() throws IOException {
        String dsl = "{\"query\":{\"nested\":{\"path\":\"purchase_details\",\"score_mode\":\"avg\",\"query\":{\"bool\":{\"must\":[{\"range\":{\"purchase_details.date\":{\"gte\":\"2020-04-09 00:00:00\",\"lte\":\"2020-09-10 00:00:00\"}}}]}}}},\"aggs\":{\"userId_aggs\":{\"terms\":{\"size\":10000,\"field\":\"user_id\"},\"aggs\":{\"subA_selector\":{\"bucket_selector\":{\"buckets_path\":{\"sumPrice\":\"subA>filter_aggs>sum_price \"},\"script\":\"params.sumPrice > 1\"}},\"subA\":{\"nested\":{\"path\":\"purchase_details\"},\"aggs\":{\"filter_aggs\":{\"filter\":{\"term\":{\"purchase_details.action_type\":2}},\"aggs\":{\"sum_price\":{\"sum\":{\"field\":\"purchase_details.price\"}}}}}}}}},\"size\":0}\n";
        dsl = "{\n" +
                "    \"size\":0,\n" +
                "    \"query\": {\n" +
                "        \"bool\": {\n" +
                "            \"should\": [\n" +
                "                 {\n" +
                "                    \"match_all\": {\n" +
                "                        \"boost\":1\n" +
                "                    }\n" +
                "                }\n" +
                "            ],\n" +
                "            \"adjust_pure_negative\":true,\n" +
                "            \"boost\":1\n" +
                "        }\n" +
                "    },\n" +
                "    \"aggregations\": {\n" +
                "        \"purchase_details\": {\n" +
                "            \"nested\": {\n" +
                "                \"path\":\"purchase_details\"\n" +
                "            },\n" +
                "            \"aggregations\": {\n" +
                "                \"filterDate\": {\n" +
                "                    \"date_range\": {\n" +
                "                        \"field\":\"purchase_details.date\",\n" +
                "                        \"format\":\"yyyyMMdd\",\n" +
                "                        \"ranges\": [\n" +
                "                             {\n" +
                "                                \"from\":20200409,\n" +
                "                                \"to\":20200910\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"keyed\":false\n" +
                "                    }\n" +
                "                },\n" +
                "                \"userId_aggs\": {\n" +
                "                    \"terms\": {\n" +
                "                        \"field\":\"user_id\",\n" +
                "                        \"size\":2147483647,\n" +
                "                        \"min_doc_count\":1,\n" +
                "                        \"shard_min_doc_count\":0,\n" +
                "                        \"show_term_doc_count_error\":false,\n" +
                "                        \"order\": [\n" +
                "                             {\n" +
                "                                \"_count\":\"desc\"\n" +
                "                            },\n" +
                "                             {\n" +
                "                                \"_key\":\"asc\"\n" +
                "                            }\n" +
                "                        ]\n" +
                "                    },\n" +
                "                    \"aggregations\": {\n" +
                "                        \"subA\": {\n" +
                "                            \"nested\": {\n" +
                "                                \"path\":\"purchase_details\"\n" +
                "                            },\n" +
                "                            \"aggregations\": {\n" +
                "                                \"filter_aggs\": {\n" +
                "                                    \"filters\": {\n" +
                "                                        \"filters\": [\n" +
                "                                             {\n" +
                "                                                \"term\": {\n" +
                "                                                    \"purchase_details.action_type\": {\n" +
                "                                                        \"value\":1,\n" +
                "                                                        \"boost\":1\n" +
                "                                                    }\n" +
                "                                                }\n" +
                "                                            }\n" +
                "                                        ],\n" +
                "                                        \"other_bucket\":false,\n" +
                "                                        \"other_bucket_key\":\"_other_\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"sum_price\": {\n" +
                "                                    \"sum\": {\n" +
                "                                        \"field\":\"purchase_details.price\"\n" +
                "                                    }\n" +
                "                                }\n" +
                "                            }\n" +
                "                        },\n" +
                "                        \"subA_selector\": {\n" +
                "                            \"bucket_selector\": {\n" +
                "                                \"buckets_path\": {\n" +
                "                                    \"sumPrice\":\"subA>sum_price\"\n" +
                "                                },\n" +
                "                                \"script\": {\n" +
                "                                    \"source\":\"params.sumPrice>500\",\n" +
                "                                    \"lang\":\"painless\"\n" +
                "                                },\n" +
                "                                \"gap_policy\":\"skip\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n";
        Request request = new Request("POST", "/label_usertrait/_doc/_search");
        request.setJsonEntity(dsl);
        Response response = client.getLowLevelClient().performRequest(request);
        String jsonResponse = EntityUtils.toString(response.getEntity());
        log.info("result:", jsonResponse.toString());
        /**
         * {"took":8,"timed_out":false,"_shards":{"total":1,"successful":1,"skipped":0,"failed":0},"hits":{"total":{"value":2,"relation":"eq"},"max_score":null,"hits":[]},"aggregations":{"userId_aggs":{"doc_count_error_upper_bound":0,"sum_other_doc_count":0,"buckets":[{"key":91597005,"doc_count":1,"subA":{"doc_count":1,"filter_aggs":{"doc_count":1,"sum_price":{"value":600.0}}}},{"key":91639474,"doc_count":1,"subA":{"doc_count":1,"filter_aggs":{"doc_count":1,"sum_price":{"value":400.0}}}}]}}}
         */
    }

    /**
     * POST /posts/doc/1/_update?pretty
     * {
     *   "script" : "ctx._source.age += 5"
     * }
     * @throws Exception
     */
    @Test
    public void testUpdate()throws Exception{
            UpdateRequest updateRequest = new UpdateRequest("index_test","doc","1");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("age", 4);
        Script inline = new Script(ScriptType.INLINE, "painless", "ctx._source.age += params.age", parameters);
        updateRequest.script(inline);
        UpdateResponse updateResponse = client.update(updateRequest,RequestOptions.DEFAULT);
        System.out.println(updateResponse);
        }

        @Test
    public void testUpsert()throws Exception{
        UpdateRequest request = new UpdateRequest(
                "index_test2", //index name
                "doc",  // type
                "1");   // doc id
            String jsonString = "{\"created\":\"2017-01-01\"}";
            UpdateRequest resp = request.upsert(jsonString, XContentType.JSON);
            System.out.println(resp.toString());
        }

    @Test
    public void testcreateIndex()throws Exception{
        String jsonString = "{\"created\":\"2017-01-01\"}";
        boolean flag =   EsClientUtils.createIndex("label_usertrait2");
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
        GetIndexRequest getIndexRequest = new GetIndexRequest("label_usertrait");
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
}
