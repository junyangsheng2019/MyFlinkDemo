package com.example.es.movie.service;

import com.example.es.movie.MovieIndexTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;

public class MovieSearchServiceImpl  implements MovieSearchService{
    @Autowired
    private TransportClient transportClient;

    @Override
    public void buldOption(MovieIndexTemplate indexTemplate, String id) throws JsonProcessingException {

    }
}
