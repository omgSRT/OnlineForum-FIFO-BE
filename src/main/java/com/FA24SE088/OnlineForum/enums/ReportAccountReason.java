package com.FA24SE088.OnlineForum.enums;

import lombok.Getter;

@Getter
public enum ReportAccountReason {
    HATE("Slurs, Racist or sexist stereotypes, Dehumanization, Incitement of fear or discrimination, Hateful references, Hateful symbols & logos"),
    ABUSE_AND_HARASSMENT("Insults, Unwanted Sexual Content & Graphic Objectification, Unwanted NSFW & Graphic Content, Violent Event Denial, Targeted Harassment and Inciting Harassment"),
    VIOLENT_SPEECH("Violent Threats, Wish of Harm, Glorification of Violence, Incitement of Violence, Coded Incitement of Violence"),
    CHILD_SAFETY("Child sexual exploitation, grooming, physical child abuse, underage user"),
    PRIVACY("Sharing private information, threatening to share/expose private information, sharing non-consensual intimate images, sharing images of me that I don’t want on the platform"),
    SPAM("Fake engagement, scams, fake accounts, malicious links"),
    SUICIDE_OR_SELF_HARM("Encouraging, promoting, providing instructions or sharing strategies for self-harm."),
    SENSITIVE_OR_DISTURBING_MEDIA("Graphic Content, Gratuitous Gore, Adult Nudity & Sexual Behavior, Violent Sexual Conduct, Bestiality & Necrophilia, Media depicting a deceased individual"),
    IMPERSONATION("Pretending to be someone else, including non-compliant parody/fan accounts"),
    VIOLENT_AND_HATEFUL_ENTITIES("Violent extremism and terrorism, hate groups & networks"),
    ;

    private String message;

    ReportAccountReason(String message){
        this.message = message;
    }
}
