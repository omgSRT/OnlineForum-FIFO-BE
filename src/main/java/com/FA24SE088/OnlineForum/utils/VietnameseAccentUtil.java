package com.FA24SE088.OnlineForum.utils;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Component
public class VietnameseAccentUtil {
    private static final String VIETNAMESE_VOWELS = "aăâeêioôơuưy";
    private static final String[] VIETNAMESE_COMPOUND_VOWELS = {
            "ươ", "uôi", "ươi", "oai", "oe", "ai", "ao", "ay", "âu", "eo", "ia", "iu", "oa", "uy"
    };
    private static final String VIETNAMESE_DIACRITIC_CHARACTERS
            = "ẮẰẲẴẶĂẤẦẨẪẬÂÁÀÃẢẠĐẾỀỂỄỆÊÉÈẺẼẸÍÌỈĨỊỐỒỔỖỘÔỚỜỞỠỢƠÓÒÕỎỌỨỪỬỮỰƯÚÙỦŨỤÝỲỶỸỴ";
    private static final Pattern VIETNAMESE_WORD_PATTERN =
            Pattern.compile("(?:[" + VIETNAMESE_DIACRITIC_CHARACTERS + "] | [A-Z])++",
                    Pattern.CANON_EQ |
                            Pattern.CASE_INSENSITIVE |
                            Pattern.UNICODE_CASE);

    public static String addVietnameseAccents(String input) {
        String[] words = input.split("\\s+"); // Split into words
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            result.append(processWord(word)).append(" ");
        }

        return result.toString().trim();
    }

    private static String processWord(String word) {
        String normalizedWord = normalize(word);
        String compoundVowel = detectCompoundVowel(normalizedWord);

        if (compoundVowel != null) {
            return applyAccentToCompoundVowel(normalizedWord, compoundVowel);
        } else {
            return applyAccentToSimpleVowel(normalizedWord);
        }
    }

    private static String normalize(String word) {
        return Normalizer.normalize(word, Normalizer.Form.NFD);
    }

    private static String detectCompoundVowel(String word) {
        for (String compound : VIETNAMESE_COMPOUND_VOWELS) {
            if (word.contains(compound)) {
                return compound;
            }
        }
        return null;
    }

    private static String applyAccentToCompoundVowel(String word, String compoundVowel) {
        char[] compoundChars = compoundVowel.toCharArray();

        for (char primaryVowel : compoundChars) {
            if (VIETNAMESE_VOWELS.indexOf(primaryVowel) >= 0) {
                char accentedVowel = addToneToVowel(primaryVowel, 0);
                return word.replaceFirst(compoundVowel,
                        compoundVowel.replace(primaryVowel, accentedVowel));
            }
        }

        return word;
    }

    private static String applyAccentToSimpleVowel(String word) {
        char[] characters = word.toCharArray();

        // Search for main vowel nucleus (e.g., 'a', 'o', 'e')
        for (int i = 0; i < characters.length; i++) {
            char ch = characters[i];
            if (VIETNAMESE_VOWELS.indexOf(ch) >= 0) {
                characters[i] = addToneToVowel(ch, 0); // 0 = sắc (́)
                break;
            }
        }

        return new String(characters);
    }

    private static char addToneToVowel(char vowel, int toneIndex) {
        return switch (vowel) {
            case 'a' -> "áàảãạ".charAt(toneIndex);
            case 'ă' -> "ắằẳẵặ".charAt(toneIndex);
            case 'â' -> "ấầẩẫậ".charAt(toneIndex);
            case 'e' -> "éèẻẽẹ".charAt(toneIndex);
            case 'ê' -> "ếềểễệ".charAt(toneIndex);
            case 'i' -> "íìỉĩị".charAt(toneIndex);
            case 'o' -> "óòỏõọ".charAt(toneIndex);
            case 'ô' -> "ốồổỗộ".charAt(toneIndex);
            case 'ơ' -> "ớờởỡợ".charAt(toneIndex);
            case 'u' -> "úùủũụ".charAt(toneIndex);
            case 'ư' -> "ứừửữự".charAt(toneIndex);
            case 'y' -> "ýỳỷỹỵ".charAt(toneIndex);
            default -> vowel;
        };
    }
}
