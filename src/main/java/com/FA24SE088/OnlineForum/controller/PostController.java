package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {
    final PostService postService;
}
