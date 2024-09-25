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
}
