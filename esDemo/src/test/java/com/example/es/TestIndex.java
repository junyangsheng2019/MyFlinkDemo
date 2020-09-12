package com.example.es;
import com.example.es.esutlis.InitRestHighLevelClient;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestIndex {


        @Autowired
        private RestHighLevelClient client;
        private static String movieIdex ="movie_index";
        /**
         * 同步创建索引，手动创建索引  PUT localhost:9200/test_post
         *
         * @throws IOException
         */
        @Test
        public void testCreateIndex() throws IOException {
        /*    PUT /my_index
    {
        "settings": {
        "number_of_shards": 1,
                "number_of_replicas": 1
    },
        "mappings": {
        "properties": {
            "filed1": {
                "type": "text"
            },
            "filed2": {
                "type": "text"
            }
        }
    },
        "aliases": {
        "default_index": {}
    }
    }*/
            //1.获取请求
            CreateIndexRequest createIndexRequest = new CreateIndexRequest("my_index");

            //设置请求参数
            createIndexRequest.settings(Settings.builder().put("number_of_shards", "1").put("number_of_replicas", "1").build());

            //设置映射1
            createIndexRequest.mapping("{\n" +
                    "        \"properties\": {\n" +
                    "            \"filed1\": {\n" +
                    "                \"type\": \"text\"\n" +
                    "            },\n" +
                    "            \"filed2\": {\n" +
                    "                \"type\": \"text\"\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }", XContentType.JSON);
            //设置映射2
//        Map<String, Object> filed1 = new HashMap<String, Object>();
//        filed1.put("type", "text");
//        filed1.put("analyzer", "standard");
//        Map<String, Object> filed2 = new HashMap<String, Object>();
//        filed2.put("type", "text");
//        Map<String, Object> properties = new HashMap<String, Object>();
//        properties.put("filed1", filed1);
//        properties.put("filed2", filed2);
//        Map<String, Object> mapping = new HashMap<String, Object>();
//        mapping.put("properties", properties);
//        createIndexRequest.mapping(mapping);
            //设置映射3
//        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder();
//        xContentBuilder.startObject();
//        {
//            xContentBuilder.startObject("fild1");
//            {
//                xContentBuilder.field("type","text");
//            }
//            xContentBuilder.endObject();
//            xContentBuilder.startObject("fild2");
//            {
//                xContentBuilder.field("type","text");
//            }
//            xContentBuilder.endObject();
//        }
//        xContentBuilder.endObject();
//        createIndexRequest.mapping(xContentBuilder);
            //设置别名
            createIndexRequest.alias(new Alias("prod_index"));

            //==================可选参数==================
            //设置超时时间
            createIndexRequest.setTimeout(TimeValue.timeValueSeconds(5));
            //主节点超时时间
            createIndexRequest.setMasterTimeout(TimeValue.timeValueSeconds(5));
            //设置创建索引API返回响应之前等待活动分片的数量
            createIndexRequest.waitForActiveShards(ActiveShardCount.from(1));

            //2.执行
            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            //3.处理结果
            //索引
            String index = createIndexResponse.index();
            System.out.println("index：" + index);
            //得到响应（全部）
            boolean acknowledged = createIndexResponse.isAcknowledged();
            System.out.println("acknowledged：" + acknowledged);
            //得到响应 指示是否在超时前为索引中的每个分片启动了所需数量的碎片副本
            boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();
            System.out.println("shardsAcknowledged：" + shardsAcknowledged);

        }

    /**
     * 插入数据
     *
     */

    @Test
    public void testAddDocument()throws  IOException {
        String jsonString = "{\n" +
                "  \"name\": \"我不是药神2\",\n" +
                "  \"alias\": \"我不是药神2\",\n" +
                "  \"actors\": \"徐峥 王传君 周一围 谭卓 章宇\",\n" +
                "  \"directors\": \"文牧野\",\n" +
                "  \"score\": 9.3,\n" +
                "  \"area\": \"内地\",\n" +
                "  \"label\": \"国内院线 VIP电影 剧情 VIP尊享 院线 喜剧 喜剧 新片 剧情\",\n" +
                "  \"release\": \"2019\",\n" +
                "  \"introduction\": \"普通中年男子程勇（徐峥 饰）经营着一家保健品店，失意又失婚。不速之客吕受益（王传君 饰）的到来，让他开辟了一条去印度买药做“代购”的新事业，虽然困难重重，但他在这条“买药之路”上发现了商机，一发不可收拾地做起了治疗慢粒白血病的印度仿制药独家代理商。赚钱的同时，他也认识了几个病患及家属，为救女儿被迫做舞女的思慧（谭卓 饰）、说一口流利“神父腔”英语的刘牧师（杨新鸣 饰），以及脾气暴烈的“黄毛”（章宇 饰），几个人合伙做起了生意，利润倍增的同时也危机四伏。程勇昔日的小舅子曹警官（周一围 饰）奉命调查仿制药的源头，假药贩子张长林（王砚辉 饰）和瑞士正牌医药代表（李乃文 饰）也对其虎视眈眈，生意逐渐变成了一场关于救赎的拉锯战。\"\n" +
                "}";
        //方法1 success
        IndexRequest indexRequest = new IndexRequest(movieIdex);
        indexRequest.source(jsonString, XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {

            DocWriteResponse.Result result = indexResponse.getResult();

            System.out.println("CREATE：" + result);

        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {

            DocWriteResponse.Result result = indexResponse.getResult();

            System.out.println("UPDATE：" + result);

        }else{

            System.out.println(indexResponse.getResult() );

        }

    }
    /**
     * 异步创建索引
         * 创建成功
         * @throws IOException
         */
        @Test
        public void testCreateIndexAsync() throws IOException {
            //1.获取请求
            CreateIndexRequest createIndexRequest = new CreateIndexRequest("my_index");

            //设置请求参数
            createIndexRequest.settings(Settings.builder().put("number_of_shards", "1").put("number_of_replicas", "1").build());

            //设置映射1
            createIndexRequest.mapping("{\n" +
                    "        \"properties\": {\n" +
                    "            \"filed1\": {\n" +
                    "                \"type\": \"text\"\n" +
                    "            },\n" +
                    "            \"filed2\": {\n" +
                    "                \"type\": \"text\"\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }", XContentType.JSON);
            //设置别名
            createIndexRequest.alias(new Alias("prod_index"));

            //==================可选参数==================
            //设置超时时间
            createIndexRequest.setTimeout(TimeValue.timeValueSeconds(5));
            //主节点超时时间
            createIndexRequest.setMasterTimeout(TimeValue.timeValueSeconds(5));
            //设置创建索引API返回响应之前等待活动分片的数量
            createIndexRequest.waitForActiveShards(ActiveShardCount.from(1));

            //2.执行
            ActionListener<CreateIndexResponse> listener = new ActionListener<CreateIndexResponse>() {
                @Override
                public void onResponse(CreateIndexResponse createIndexResponse) {
                    //3.处理结果
                    //索引
                    String index = createIndexResponse.index();
                    System.out.println("index：" + index);
                    //得到响应（全部）
                    boolean acknowledged = createIndexResponse.isAcknowledged();
                    System.out.println("acknowledged：" + acknowledged);
                    //得到响应 指示是否在超时前为索引中的每个分片启动了所需数量的碎片副本
                    boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();
                    System.out.println("shardsAcknowledged：" + shardsAcknowledged);
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                }
            };
            client.indices().createAsync(createIndexRequest, RequestOptions.DEFAULT, listener);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 同步删除索引
         *
         * @throws IOException
         */
        @Test
        public void testDeleteIndex() throws IOException {
            //构建请求
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("my_index");
            deleteIndexRequest.timeout(TimeValue.timeValueSeconds(5));
            deleteIndexRequest.masterNodeTimeout(TimeValue.timeValueSeconds(5));

            //同步执行
            AcknowledgedResponse acknowledgedResponse = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            //处理响应
            boolean acknowledged = acknowledgedResponse.isAcknowledged();
            System.out.println(acknowledged);
        }

        /**
         * 异步删除索引
         *
         * @throws IOException
         */
        @Test
        public void testDeleteIndexAsync() throws IOException {
            //构建请求
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("my_index");
            deleteIndexRequest.timeout(TimeValue.timeValueSeconds(5));
            deleteIndexRequest.masterNodeTimeout(TimeValue.timeValueSeconds(5));

            //异步执行
            ActionListener<AcknowledgedResponse> listener = new ActionListener<AcknowledgedResponse>() {
                @Override
                public void onResponse(AcknowledgedResponse acknowledgedResponse) {
                    //处理响应
                    boolean acknowledged = acknowledgedResponse.isAcknowledged();
                    System.out.println(acknowledged);
                    System.out.println(acknowledgedResponse.toString());
                    System.out.println("删除索引成功！！！");
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println("删除索引失败！！！");
                    e.printStackTrace();
                }
            };
            client.indices().deleteAsync(deleteIndexRequest, RequestOptions.DEFAULT, listener);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        /**
         * 同步获取索引 Index exists API
         *
         * @throws IOException
         */
        @Test
        public void testExistsIndex() throws IOException {
            //构建请求
            GetIndexRequest getIndexRequest = new GetIndexRequest("my_index");
            //=========================参数=========================
            //从主节点返回本地索引信息状态
            getIndexRequest.local(false);
            //以适合人类的格式返回
            getIndexRequest.humanReadable(true);
            //是否返回每个索引的所有默认配置
            getIndexRequest.includeDefaults(false);
            //执行
            boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            //处理响应
            System.out.println(exists);
        }

        /**
         * 异步获取索引 Index exists API
         *
         * @throws IOException
         */
        @Test
        public void testExistsIndexAsync() throws IOException {
            //构建请求
            GetIndexRequest getIndexRequest = new GetIndexRequest("my_index");
            //=========================参数=========================
            //从主节点返回本地索引信息状态
            getIndexRequest.local(false);
            //以适合人类的格式返回
            getIndexRequest.humanReadable(true);
            //是否返回每个索引的所有默认配置
            getIndexRequest.includeDefaults(false);
            //执行
            ActionListener<Boolean> listener = new ActionListener<Boolean>() {

                @Override
                public void onResponse(Boolean exists) {
                    //处理响应
                    System.out.println(exists);
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                }
            };
            client.indices().existsAsync(getIndexRequest, RequestOptions.DEFAULT, listener);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 同步关闭索引
         */
        @Test
        public void testCloseIndex() throws IOException {
            //构建请求
            CloseIndexRequest closeIndexRequest = new CloseIndexRequest("my_index");
            //执行
            CloseIndexResponse closeIndexResponse = (CloseIndexResponse) client.indices().close(closeIndexRequest, RequestOptions.DEFAULT);
            //处理响应
            boolean acknowledged = closeIndexResponse.isAcknowledged();
            System.out.println(acknowledged);
        }

        /**
         * 异步关闭索引
         */
        @Test
        public void testCloseIndexAsync() {
            //构建请求
            CloseIndexRequest closeIndexRequest = new CloseIndexRequest("my_index");
            //执行
            ActionListener<CloseIndexResponse> listener = new ActionListener<CloseIndexResponse>() {
                @Override
                public void onResponse(CloseIndexResponse closeIndexResponse) {
                    boolean acknowledged = closeIndexResponse.isAcknowledged();
                    System.out.println(acknowledged);
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                }
            };
//            client.indices().closeAsync(closeIndexRequest, RequestOptions.DEFAULT, listener);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 同步打开索引
         *
         * @throws IOException
         */
        @Test
        public void testOpenIndex() throws IOException {
            //构建请求
            OpenIndexRequest openIndexRequest = new OpenIndexRequest("my_index");
            //执行
            OpenIndexResponse openIndexResponse = client.indices().open(openIndexRequest, RequestOptions.DEFAULT);
            //处理响应结果
            boolean acknowledged = openIndexResponse.isAcknowledged();
            System.out.println(acknowledged);
        }

        /**
         * 异步打开索引
         *
         * @throws IOException
         */
        @Test
        public void testOpenIndexAsync() {
            //构建请求
            OpenIndexRequest openIndexRequest = new OpenIndexRequest("my_index");
            ActionListener<OpenIndexResponse> listener = new ActionListener<OpenIndexResponse>() {
                @Override
                public void onResponse(OpenIndexResponse openIndexResponse) {
                    //处理响应结果
                    boolean acknowledged = openIndexResponse.isAcknowledged();
                    System.out.println(acknowledged);
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                }
            };
            //执行
            client.indices().openAsync(openIndexRequest, RequestOptions.DEFAULT, listener);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

}
