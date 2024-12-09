package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.enums.AccountStatus;
import com.FA24SE088.OnlineForum.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final AccountRepository accountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String avatar = oAuth2User.getAttribute("picture");

        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            account = new Account();
            account.setEmail(email);
            account.setUsername(email.split("@")[0]);
            account.setAvatar(avatar);
            account.setStatus(AccountStatus.ACTIVE.name());
            account.setCreatedDate(LocalDateTime.now());
            accountRepository.save(account);
        }

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("accountId", account.getAccountId());

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "email");
    }
}
