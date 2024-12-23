package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.service.*;
import com.FA24SE088.OnlineForum.utils.*;
import com.github.junrar.exception.RarException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/test")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TestController {
    //region DI
    AccountService accountService;
    AuthenticateService authenticateService;
    BlockUserService blockUserService;
    BookMarkService bookMarkService;
    CategoryService categoryService;
    CommentService commentService;
    CustomOAuth2UserService customOAuth2UserService;
    DailyPointService dailyPointService;
    EventService eventService;
    FeedbackService feedbackService;
    FollowService followService;
    ImageService imageService;
    MonkeyCoinPackService monkeyCoinPackService;
    NotificationService notificationService;
    OrderPointService orderPointService;
    PointService pointService;
    PostFileService postFileService;
    PostService postService;
    PostViewService postViewService;
    RedeemService redeemService;
    ReportService reportService;
    RewardService rewardService;
    StatisticService statisticService;
    TagService tagService;
    TopicService topicService;
    TransactionService transactionService;
    TypeBonusService typeBonusService;
    UpvoteService upvoteService;
    WalletService walletService;
    ContentFilterUtil contentFilterUtil;
    DetectProgrammingLanguageUtil detectProgrammingLanguageUtil;
    EmailUtil emailUtil;
    TikaUtil tikaUtil;
    OtpUtil otpUtil;
    PaginationUtils paginationUtils;
    OpenAIUtil openAIUtil;
    SocketIOUtil socketIOUtil;
    //endregion

//    @GetMapping("/test")
//    public String test(String filePath){
//        try {
//            var blob = getByteFromFilePath(filePath);
//            if(blob == null){
//                return null;
//            }
//            //tikaUtil.detectMimeType(bytes);
////            var map = detectProgrammingLanguageUtil.countLanguagesInRar(blob.getContent());
////            return detectProgrammingLanguageUtil.countLanguages(map);
//            return detectProgrammingLanguageUtil.determineProgrammingLanguage(blob.getContent(), blob.getContentType());
//        } catch (IOException | RarException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private Blob getByteFromFilePath(String path) throws IOException {
        Bucket bucket = StorageClient.getInstance().bucket();

        Blob blob = bucket.get(path);

        if (blob == null) {
            return null;
            //throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }

        System.out.println(blob.getContentType());

        return blob;
    }
}
