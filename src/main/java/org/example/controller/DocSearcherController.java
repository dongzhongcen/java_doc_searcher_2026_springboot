package org.example.controller;

import org.example.searcher.DocSearcher;
import org.example.searcher.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DocSearcherController {
    private final DocSearcher docSearcher = new DocSearcher();

    @GetMapping("/search")
    public List<Result> search(@RequestParam("query") String query) {
        if (query == null || query.trim().equals("")) {
            throw new BadRequestException("非法输入, 未获取到 query 值");
        }
        System.out.println("query = " + query);
        return docSearcher.search(query);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }
}
