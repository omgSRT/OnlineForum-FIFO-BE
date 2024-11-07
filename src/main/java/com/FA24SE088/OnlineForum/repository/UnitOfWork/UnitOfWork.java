package com.FA24SE088.OnlineForum.repository.UnitOfWork;

import com.FA24SE088.OnlineForum.repository.Repository.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@RequiredArgsConstructor
public class UnitOfWork {
    final AccountRepository accountRepository;
    final InvalidateTokenRepository invalidateTokenRepository;
    final RoleRepository roleRepository;
    final CategoryRepository categoryRepository;
    final CommentRepository commentRepository;
    final DailyPointRepository dailyPointRepository;
    final FeedbackRepository feedbackRepository;
    final FollowRepository followRepository;
    final ImageRepository imageRepository;
    final NotificationRepository notificationRepository;
    final PointRepository pointRepository;
    final PostRepository postRepository;
    final RedeemRepository redeemRepository;
    final RewardRepository rewardRepository;
    final ImageSectionRepository imageSectionRepository;
    final VideoSectionRepository videoSectionRepository;
    final SectionRepository sectionRepository;
    final TagRepository tagRepository;
    final TopicRepository topicRepository;
    final TransactionRepository transactionRepository;
    final UpvoteRepository upvoteRepository;
    final WalletRepository walletRepository;
    final EventRepository eventRepository;
    final BlockedAccountRepository blockedAccountRepository;
    final ReportRepository reportRepository;
    final BookMarkRepository bookMarkRepository;
    final OtpRepository otpRepository;
    final ContentSectionRepository contentSectionRepository;
    final MediaRepository mediaRepository;
    final TypeBonusRepository typeBonusRepository;
    final PricingRepository pricingRepository;
    final OrderRepository orderRepository;
}
