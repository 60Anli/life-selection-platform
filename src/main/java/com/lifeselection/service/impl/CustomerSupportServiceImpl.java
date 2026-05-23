package com.lifeselection.service.impl;

import cn.hutool.core.util.StrUtil;
import com.lifeselection.dto.Result;
import com.lifeselection.dto.SupportChatRequest;
import com.lifeselection.dto.SupportChatResponse;
import com.lifeselection.dto.UserDTO;
import com.lifeselection.entity.Shop;
import com.lifeselection.entity.Voucher;
import com.lifeselection.service.ICustomerSupportService;
import com.lifeselection.service.IShopService;
import com.lifeselection.service.IVoucherService;
import com.lifeselection.utils.UserHolder;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lifeselection.utils.RedisConstants.CUSTOMER_SERVICE_SESSION_KEY;
import static com.lifeselection.utils.RedisConstants.CUSTOMER_SERVICE_SESSION_TTL;

@Service
public class CustomerSupportServiceImpl implements ICustomerSupportService {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");
    private static final int MAX_SESSION_MESSAGES = 20;

    @Resource
    private IShopService shopService;
    @Resource
    private IVoucherService voucherService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired(required = false)
    private ChatLanguageModel chatLanguageModel;

    @Override
    public Result chat(SupportChatRequest request) {
        if (request == null || StrUtil.isBlank(request.getMessage())) {
            return Result.fail("\u8BF7\u8F93\u5165\u54A8\u8BE2\u5185\u5BB9");
        }

        String message = request.getMessage().trim();
        String sessionId = buildSessionId(request.getSessionId());
        remember(sessionId, "user", message);

        String localAnswer = answer(message);
        String reply = answerWithLangChain4j(message, localAnswer);
        remember(sessionId, "assistant", reply);

        SupportChatResponse response = new SupportChatResponse(
                sessionId,
                reply,
                Arrays.asList(
                        "\u67E5\u8BE2\u5546\u5BB6 1",
                        "\u67E5\u770B\u5E97\u94FA 1 \u7684\u4F18\u60E0\u5238",
                        "\u79D2\u6740\u89C4\u5219"
                )
        );
        return Result.ok(response);
    }

    private String buildSessionId(String requestedSessionId) {
        if (StrUtil.isNotBlank(requestedSessionId)) {
            return requestedSessionId;
        }
        UserDTO user = UserHolder.getUser();
        if (user != null && user.getId() != null) {
            return "user:" + user.getId();
        }
        return UUID.randomUUID().toString();
    }

    private void remember(String sessionId, String role, String content) {
        String key = CUSTOMER_SERVICE_SESSION_KEY + sessionId;
        stringRedisTemplate.opsForList().leftPush(key, role + ":" + content);
        stringRedisTemplate.opsForList().trim(key, 0, MAX_SESSION_MESSAGES - 1);
        stringRedisTemplate.expire(key, CUSTOMER_SERVICE_SESSION_TTL, TimeUnit.MINUTES);
    }

    private String answer(String message) {
        Long id = firstNumber(message);
        if (containsAny(message, "\u4EBA\u5DE5", "\u5BA2\u670D", "\u8F6C\u4EBA\u5DE5")) {
            return "\u5DF2\u4E3A\u4F60\u8BB0\u5F55\u4EBA\u5DE5\u5BA2\u670D\u9700\u6C42\uFF0C\u8BF7\u7559\u4E0B\u624B\u673A\u53F7\u6216\u5728\u8BA2\u5355\u9875\u53D1\u8D77\u552E\u540E\uFF0C\u6211\u4EEC\u4F1A\u4F18\u5148\u5904\u7406\u3002";
        }
        if (containsAny(message, "\u4F18\u60E0", "\u5238", "\u4EE3\u91D1\u5238")) {
            return answerVoucher(id);
        }
        if (containsAny(message, "\u5E97\u94FA", "\u5546\u5BB6", "\u5730\u5740", "\u8425\u4E1A", "\u8BC4\u5206")) {
            return answerShop(id);
        }
        if (containsAny(message, "\u79D2\u6740", "\u62A2\u8D2D", "\u4E0B\u5355")) {
            return "\u79D2\u6740\u4F1A\u5148\u7528 Redis \u548C Lua \u6821\u9A8C\u5E93\u5B58\u53CA\u4E00\u4EBA\u4E00\u5355\uFF0C\u518D\u5199\u5165 Kafka \u5F02\u6B65\u843D\u5E93\u3002\u82E5\u8FD4\u56DE\u8BA2\u5355\u53F7\uFF0C\u8BF4\u660E\u5DF2\u8FDB\u5165\u6392\u961F\u5904\u7406\u3002";
        }
        if (containsAny(message, "\u4F60\u597D", "\u60A8\u597D", "\u5728\u5417")) {
            return "\u4F60\u597D\uFF0C\u6211\u662F\u751F\u6D3B\u4F18\u9009\u5E73\u53F0\u667A\u80FD\u5BA2\u670D\u3002\u4F60\u53EF\u4EE5\u95EE\u6211\u5546\u5BB6\u5730\u5740\u3001\u4F18\u60E0\u5238\u3001\u79D2\u6740\u89C4\u5219\u6216\u8BA2\u5355\u6392\u961F\u95EE\u9898\u3002";
        }
        return "\u6211\u5DF2\u6536\u5230\u4F60\u7684\u95EE\u9898\u3002\u4F60\u53EF\u4EE5\u8865\u5145\u5E97\u94FA\u6216\u4F18\u60E0\u5238\u7F16\u53F7\uFF0C\u6211\u80FD\u5E2E\u4F60\u67E5\u8BE2\u5546\u5BB6\u4FE1\u606F\u3001\u4F18\u60E0\u5238\u5217\u8868\u548C\u79D2\u6740\u89C4\u5219\u3002";
    }

    private String answerWithLangChain4j(String message, String localAnswer) {
        if (chatLanguageModel == null) {
            return localAnswer;
        }
        String prompt = "You are the customer-service assistant for the Life Selection Platform. "
                + "Answer in concise Chinese. Use the business lookup result as trusted context. "
                + "If the context already answers the question, polish it without changing facts.\n"
                + "User question: " + message + "\n"
                + "Business lookup result: " + localAnswer;
        try {
            return chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            return localAnswer;
        }
    }

    private String answerShop(Long shopId) {
        if (shopId == null) {
            return "\u8BF7\u63D0\u4F9B\u5E97\u94FA\u7F16\u53F7\uFF0C\u4F8B\u5982\u201C\u67E5\u8BE2\u5546\u5BB6 1 \u7684\u8425\u4E1A\u65F6\u95F4\u201D\u3002";
        }
        Result result = shopService.queryById(shopId);
        if (!Boolean.TRUE.equals(result.getSuccess()) || !(result.getData() instanceof Shop)) {
            return "\u6CA1\u6709\u67E5\u5230\u8BE5\u5E97\u94FA\uFF0C\u8BF7\u786E\u8BA4\u5E97\u94FA\u7F16\u53F7\u662F\u5426\u6B63\u786E\u3002";
        }
        Shop shop = (Shop) result.getData();
        return "\u5E97\u94FA\uFF1A" + shop.getName()
                + "\uFF0C\u5730\u5740\uFF1A" + valueOrDefault(shop.getAddress(), "\u6682\u65E0\u5730\u5740")
                + "\uFF0C\u8425\u4E1A\u65F6\u95F4\uFF1A" + valueOrDefault(shop.getOpenHours(), "\u6682\u65E0\u8425\u4E1A\u65F6\u95F4")
                + "\uFF0C\u8BC4\u5206\uFF1A" + (shop.getScore() == null ? "\u6682\u65E0\u8BC4\u5206" : shop.getScore() / 10.0 + "\u5206")
                + "\u3002";
    }

    private String answerVoucher(Long shopId) {
        if (shopId == null) {
            return "\u8BF7\u63D0\u4F9B\u5E97\u94FA\u7F16\u53F7\uFF0C\u4F8B\u5982\u201C\u67E5\u770B\u5E97\u94FA 1 \u7684\u4F18\u60E0\u5238\u201D\u3002";
        }
        Result result = voucherService.queryVoucherOfShop(shopId);
        Object data = result.getData();
        if (!(data instanceof Collection) || ((Collection<?>) data).isEmpty()) {
            return "\u5F53\u524D\u5E97\u94FA\u6682\u65E0\u53EF\u5C55\u793A\u4F18\u60E0\u5238\u3002";
        }
        List<String> titles = new ArrayList<>();
        for (Object item : (Collection<?>) data) {
            if (item instanceof Voucher) {
                Voucher voucher = (Voucher) item;
                titles.add(voucher.getTitle());
            }
            if (titles.size() >= 3) {
                break;
            }
        }
        return "\u5F53\u524D\u5E97\u94FA\u6709 " + ((Collection<?>) data).size()
                + " \u5F20\u4F18\u60E0\u5238\uFF0C\u53EF\u4F18\u5148\u770B\u770B\uFF1A"
                + StrUtil.join("\u3001", titles) + "\u3002";
    }

    private Long firstNumber(String message) {
        Matcher matcher = NUMBER_PATTERN.matcher(message);
        if (!matcher.find()) {
            return null;
        }
        return Long.valueOf(matcher.group(1));
    }

    private boolean containsAny(String message, String... keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return StrUtil.isBlank(value) ? defaultValue : value;
    }
}
