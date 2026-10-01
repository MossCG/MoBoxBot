package org.moboxlab.moboxbot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * API 0.1 冻结检查
 * 公共 API 形状一旦变化，本测试必须同步更新版本与文档。
 */
public class ApiFreezeTest {
    private static final String EXPECTED_HASH = "66ed0cd0c9f0d425f03e3460ac2328ebcd9e27c880519a87a877e9456a1d2faf";
    private static final String[] API_CLASS_NAMES = new String[]{
            "org.moboxlab.moboxbot.API.MoBoxBotAPI",
            "org.moboxlab.moboxbot.API.Server",
            "org.moboxlab.moboxbot.API.Plugin",
            "org.moboxlab.moboxbot.API.PluginDescription",
            "org.moboxlab.moboxbot.API.PluginInfo",
            "org.moboxlab.moboxbot.API.PluginLogger",
            "org.moboxlab.moboxbot.API.PluginManager",
            "org.moboxlab.moboxbot.API.PluginState",
            "org.moboxlab.moboxbot.API.Command.BotCommand",
            "org.moboxlab.moboxbot.API.Command.CommandInfo",
            "org.moboxlab.moboxbot.API.Command.CommandPermission",
            "org.moboxlab.moboxbot.API.Command.CommandSender",
            "org.moboxlab.moboxbot.API.Event.Event",
            "org.moboxlab.moboxbot.API.Event.Listener",
            "org.moboxlab.moboxbot.API.Event.EventHandler",
            "org.moboxlab.moboxbot.API.Event.EventPriority",
            "org.moboxlab.moboxbot.API.Event.EventBus",
            "org.moboxlab.moboxbot.API.Event.MessageEvent",
            "org.moboxlab.moboxbot.API.Event.GroupMessageEvent",
            "org.moboxlab.moboxbot.API.Event.PrivateMessageEvent",
            "org.moboxlab.moboxbot.API.Event.NoticeEvent",
            "org.moboxlab.moboxbot.API.Event.RequestEvent",
            "org.moboxlab.moboxbot.API.Event.MetaEvent",
            "org.moboxlab.moboxbot.API.Event.RawEvent",
            "org.moboxlab.moboxbot.API.OneBot.OneBotClient",
            "org.moboxlab.moboxbot.API.OneBot.MessageUtil",
            "org.moboxlab.moboxbot.API.Storage.StorageService",
            "org.moboxlab.moboxbot.API.Util.ImageUtil",
            "org.moboxlab.moboxbot.API.Util.PluginConfig",
            "org.moboxlab.moboxbot.API.Util.Text"
    };

    public static void main(String[] args) throws Exception {
        List<String> signatures = collectSignatures();
        String actualHash = sha256(join(signatures));
        if (args != null && args.length > 0 && "--print".equals(args[0])) {
            for (String signature : signatures) System.out.println(signature);
            System.out.println("API_FREEZE_HASH="+actualHash);
            return;
        }
        if (!EXPECTED_HASH.equals(actualHash)) {
            System.out.println("API_FREEZE_HASH="+actualHash);
            throw new IllegalStateException("API 0.1 冻结检查失败，请确认是否属于破坏性变更！");
        }
        System.out.println("API 0.1 冻结检查通过！");
    }

    private static List<String> collectSignatures() throws Exception {
        List<String> signatures = new ArrayList<>();
        for (String className : API_CLASS_NAMES) {
            Class<?> type = Class.forName(className);
            signatures.add("CLASS "+type.toGenericString());
            for (Field field : type.getDeclaredFields()) {
                if (!isPublic(field.getModifiers())) continue;
                signatures.add("FIELD "+field.toGenericString());
            }
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (!isPublic(constructor.getModifiers())) continue;
                signatures.add("CTOR "+constructor.toGenericString());
            }
            for (Method method : type.getDeclaredMethods()) {
                if (!isPublic(method.getModifiers())) continue;
                signatures.add("METHOD "+method.toGenericString());
            }
        }
        Collections.sort(signatures);
        return signatures;
    }

    private static boolean isPublic(int modifiers) {
        return (modifiers & java.lang.reflect.Modifier.PUBLIC) != 0;
    }

    private static String join(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) builder.append(value).append("\n");
        return builder.toString();
    }

    private static String sha256(String text) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte value : bytes) builder.append(String.format("%02x",value));
        return builder.toString();
    }
}
