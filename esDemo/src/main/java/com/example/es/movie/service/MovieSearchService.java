package com.example.es.movie.service;

import com.example.es.movie.MovieIndexTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface MovieSearchService {
    void buldOption(MovieIndexTemplate indexTemplate, String id) throws JsonProcessingException;

}
