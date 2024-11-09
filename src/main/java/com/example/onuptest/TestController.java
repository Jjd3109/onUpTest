package com.example.onuptest;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

@RestController
@Slf4j
public class TestController {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    private final HashMap<Integer, HashMap<String, String>> blogCheckName = new HashMap<>();

    /*
     * 테스트 1번
     */
    @GetMapping("/api/v1/test")
    public ResponseEntity<?> getTest(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "10") int display,
            @RequestParam(required = false, defaultValue = "1") int start) {

        String apiURL = "https://openapi.naver.com/v1/search/blog?query=" + query
                + "&display=" + display
                + "&start=" + start;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(apiURL, HttpMethod.GET, entity, String.class);
        return ResponseEntity.ok().body(response.getBody());

    }

    /*
     * 테스트 2번
     */

    @GetMapping("/api/v1/test2")
    public ResponseEntity<?> getTest(
            @RequestParam String blogId,
            @RequestParam(required = false, defaultValue = "10") int viewdate,
            @RequestParam(required = false, defaultValue = "1") int currentPage,
            @RequestParam(required = false, defaultValue = "0") int categoryNo,
            @RequestParam(required = false, defaultValue = "0") int parentCategoryNo,
            @RequestParam(required = false, defaultValue = "30") int countPerPage) {
        try {
            String encodedQuery = URLEncoder.encode(blogId, "UTF-8");
            String apiURL = "https://blog.naver.com/PostTitleListAsync.naver?blogId=" + encodedQuery
                    + "&viewdate=" + viewdate
                    + "&currentPage=" + currentPage
                    + "&categoryNo=" + categoryNo
                    + "&parentCategoryNo=" + parentCategoryNo
                    + "&countPerPage=" + countPerPage;


            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(apiURL, HttpMethod.GET, entity, String.class);
            return ResponseEntity.ok().body(response.getBody());

        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("검색어 인코딩 실패");
        }
    }

    /*
     * 테스트 3번
     */


    @GetMapping("/api/v1/test3")
    public ResponseEntity<?> getTestThree(
            @RequestParam String blogId,
            @RequestParam(required = false, defaultValue = "10") int viewdate,
            @RequestParam(required = false, defaultValue = "1") int currentPage,
            @RequestParam(required = false, defaultValue = "0") int categoryNo,
            @RequestParam(required = false, defaultValue = "0") int parentCategoryNo,
            @RequestParam(required = false, defaultValue = "10") int countPerPage) {
        try {
            String encodedQuery = URLEncoder.encode(blogId, "UTF-8");
            String apiURL = "https://blog.naver.com/PostTitleListAsync.naver?blogId=" + encodedQuery
                    + "&viewdate=" + viewdate
                    + "&currentPage=" + currentPage
                    + "&categoryNo=" + categoryNo
                    + "&parentCategoryNo=" + parentCategoryNo
                    + "&countPerPage=" + countPerPage;


            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(apiURL, HttpMethod.GET, entity, String.class);

            JSONObject jsonObject = new JSONObject(response.getBody());
            JSONArray postList = jsonObject.getJSONArray("postList");

            for (int i = 0; i < postList.length(); i++) {
                blogCheckName.put(i, getTitle(postList, i, blogId));
            }



            return ResponseEntity.ok().body(blogCheckName);

        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("검색어 인코딩 실패");
        }
    }

    private HashMap<String, String> getTitle(JSONArray postList, int i, String blogId) throws UnsupportedEncodingException {

        JSONObject post = postList.getJSONObject(i);

        // URL 디코딩된 제목과 날짜를 가져오기
        String decodeTitle = java.net.URLDecoder.decode(post.getString("title"), "UTF-8");

//        String title = post.getString("title");
//        String addDate = post.getString("addDate");
//
//        System.out.println("Title: " + title);
//        System.out.println("decodeTitle: " + decodeTitle);
//        System.out.println("Date: " + addDate);
//        System.out.println();


        int display = 100;

        String titleApiUrl = "https://openapi.naver.com/v1/search/blog?query=\"" + decodeTitle + "\""
                + "&display=" + display;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> titleResponse = restTemplate.exchange(titleApiUrl, HttpMethod.GET, entity, String.class);

        // JSON 파싱하여 디코딩된 제목 출력
        JSONObject responseBody = new JSONObject(titleResponse.getBody());
        JSONArray items = responseBody.getJSONArray("items");

        HashMap<String, String> putName = new HashMap<>();

        for (int j = 0; j < items.length(); j++) {
            JSONObject item = items.getJSONObject(j);
            String decodedItemTitle = java.net.URLDecoder.decode(item.getString("title"), "UTF-8");



            if(item.getString("bloggerlink").contains(blogId)) {
                putName.put("title", decodedItemTitle);
                putName.put("titleBool", "true");

                return putName;

            }

        }

        putName.put("title", decodeTitle);
        putName.put("titleBool", "false");


        return putName;

    }


}
