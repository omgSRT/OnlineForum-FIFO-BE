package com.FA24SE088.OnlineForum.enums;

import lombok.Getter;

@Getter
public enum ReportReason {
    SPAM("SPAM", "This post contains spam content."),
    INAPPROPRIATE("INAPPROPRIATE CONTENT", "This post contains inappropriate content."),
    HARASSMENT("HARASSMENT", "This post harasses or targets individuals."),
    MISINFORMATION("MISINFORMATION", "This post spreads false or misleading information."),
    HATE_SPEECH("HATE SPEECH", "This post contains hate speech."),
    COPYRIGHT("COPYRIGHT VIOLATION", "This post infringes on copyright."),
    IMPERSONATION("IMPERSONATION", "This post pretends to be someone else."),
    VIOLENCE("VIOLENCE", "This post contains violent or graphic content."),
    SELF_HARM("SELF-HARM", "This post promotes self-harm or suicide."),
    SCAM("SCAM", "This post is trying to scam people."),
    OFFENSIVE_LANGUAGE("OFFENSIVE LANGUAGE", "This post uses offensive language."),
    ADULT_CONTENT("ADULT CONTENT", "This post contains adult or sexual content."),
    TERRORISM("TERRORISM", "This post promotes terrorism or violent extremism."),
    ILLEGAL_ACTIVITIES("ILLEGAL ACTIVITES", "This post encourages illegal activities."),
    PRIVACY_VIOLATION("PRIVACY VIOLATION", "This post violates someone's privacy."),
    FAKE_NEWS("FAKE NEWS", "This post spreads fake news."),
    BULLYING("BULLYING", "This post bullies or intimidates others."),
    FALSE_ADVERTISING("FALSE ADVERTISING", "This post contains false or deceptive advertising."),
    DRUGS("DRUGS", "This post promotes the sale or use of illegal drugs."),
    CHILD_ENDANGERMENT("CHILD ENDANGERMENT", "This post endangers children."),
    MALWARE("MALWARE", "This post contains malicious software or links."),
    FRAUD("FRAUD", "This post promotes fraudulent activity."),
    DISCRIMINATION("DISCRIMINATION", "This post promotes discrimination or bigotry."),
    IDENTITY_THEFT("IDENTITY THEFT", "This post is attempting to steal someone's identity."),
    UNAUTHORIZED_SALES("UNAUTHORIZED SALE", "This post sells items or services without authorization."),
    HARMFUL_CONTENT("HARMFUL CONTENT", "This post contains harmful or dangerous content."),
    OTHER("Other", "This post needs to be reviewed for other reasons."),
    ;

    private final String title;
    private final String description;

    ReportReason(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
