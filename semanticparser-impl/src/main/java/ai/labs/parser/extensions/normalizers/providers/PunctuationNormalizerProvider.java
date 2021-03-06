package ai.labs.parser.extensions.normalizers.providers;

import ai.labs.resources.rest.extensions.model.ExtensionDescriptor.ConfigValue;
import ai.labs.parser.extensions.normalizers.INormalizer;
import ai.labs.parser.extensions.normalizers.PunctuationNormalizer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static ai.labs.resources.rest.extensions.model.ExtensionDescriptor.FieldType.BOOLEAN;
import static ai.labs.resources.rest.extensions.model.ExtensionDescriptor.FieldType.STRING;

public class PunctuationNormalizerProvider implements INormalizerProvider {
    public static final String ID = "ai.labs.parser.normalizers.punctuation";

    private static final String KEY_REMOVE_PUNCTUATION = "removePunctuation";
    private static final String KEY_PUNCTUATION_REGEX_PATTERN = "punctuationRegexPattern";
    private boolean removePunctuation;
    private String punctuationRegexPattern = PunctuationNormalizer.PUNCTUATION;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Punctuation Normalizer";
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if (config.containsKey(KEY_REMOVE_PUNCTUATION)) {
            removePunctuation = Boolean.parseBoolean(config.get(KEY_REMOVE_PUNCTUATION).toString());
        }

        if (config.containsKey(KEY_PUNCTUATION_REGEX_PATTERN)) {
            punctuationRegexPattern = config.get(KEY_PUNCTUATION_REGEX_PATTERN).toString();
        }
    }

    @Override
    public INormalizer provide() {
        return new PunctuationNormalizer(toRegexPattern(punctuationRegexPattern), removePunctuation);
    }

    @Override
    public Map<String, ConfigValue> getConfigs() {
        Map<String, ConfigValue> ret = new HashMap<>();

        ret.put(KEY_REMOVE_PUNCTUATION, new ConfigValue("Remove Punctuation", BOOLEAN, true, false));
        ret.put(KEY_PUNCTUATION_REGEX_PATTERN, new ConfigValue("Punctuation RegEx Pattern", STRING, true, punctuationRegexPattern));

        return ret;
    }

    public Pattern toRegexPattern(String punctuation) {
        return Pattern.compile("[" + punctuation + "]");
    }
}
