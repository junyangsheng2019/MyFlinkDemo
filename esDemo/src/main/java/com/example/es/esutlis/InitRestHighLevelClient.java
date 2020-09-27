package com.example.es.esutlis;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 获取java High Level的REST客户端
 */
public class InitRestHighLevelClient {
    static final Logger logger = LoggerFactory.getLogger(InitRestHighLevelClient.class);

    public static RestHighLevelClient getClient(){
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ));
        return client;
    }
    public static TransportClient getTransClient() throws UnknownHostException {
        Settings settings = Settings.builder().put("client.transport.sniff", true).put("cluster.name", "elasticsearch").build();
        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("192.168.123.101"),9300));
        return client;
    }
    public static GetResponse getESData(String indexName,String indexType,String id,String routing){
        GetRequest request = new GetRequest(indexName,indexType,id);
        GetResponse response = null;
        try {
            response = (GetResponse) getClient().get(request, RequestOptions.DEFAULT);
            logger.info("get data end.resp:index:{},id:{},found:{}",response.getIndex(), response.getId(),response.isExists());
        } catch (ElasticsearchStatusException  e) {
            logger.warn("get data : {},warn:{}", indexName + " " + id, e);
            if (e.status() == RestStatus.NOT_FOUND) {
                logger.warn("处理因为索引不存在而抛出的异常", e.getMessage());
                return null;
            }
            throw new RuntimeException(e);        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static void main(String[] args) throws IOException {
        String INDEX = "bigdata_dmp_ads_transformed_user_portrait_basic_labeling_20200921";
        GetRequest getRequest = new GetRequest(INDEX, findId());
        GetResponse response = getClient().get(getRequest,RequestOptions.DEFAULT);
        System.out.println(response.toString());

    }

    private static String findId() {
        return "83555812";
    }
}
