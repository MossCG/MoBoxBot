package org.moboxlab.moboxbot.Util;

import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxbot.Main;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 管理员账号管理
 * 管理员列表存放在 config.yml 的 botAdmin 键中。
 */
public class AdminService {
    public static List<Long> getAdminList() {
        List<Long> result = new ArrayList<>();
        String text = BasicInfo.getConfigString("botAdmin","");
        if (text == null || text.trim().isEmpty()) return result;
        for (String item : text.split(",")) {
            try {
                long id = Long.parseLong(item.trim());
                if (id > 0 && !result.contains(id)) result.add(id);
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    public static List<Long> getOwnerList() {
        List<Long> result = new ArrayList<>();
        String text = BasicInfo.getConfigString("botOwner","");
        if (text == null || text.trim().isEmpty()) return result;
        for (String item : text.split(",")) {
            try {
                long id = Long.parseLong(item.trim());
                if (id > 0 && !result.contains(id)) result.add(id);
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    public static boolean addAdmin(long userID) {
        if (userID <= 0) return false;
        Set<Long> admins = new LinkedHashSet<>(getAdminList());
        if (!admins.add(userID)) return false;
        return writeAdmins(admins);
    }

    public static boolean removeAdmin(long userID) {
        if (userID <= 0) return false;
        Set<Long> admins = new LinkedHashSet<>(getAdminList());
        if (!admins.remove(userID)) return false;
        return writeAdmins(admins);
    }

    public static boolean isAdmin(long userID) {
        if (userID <= 0) return false;
        if (isOwner(userID)) return true;
        return getAdminList().contains(userID);
    }

    private static boolean isOwner(long userID) {
        String owners = BasicInfo.getConfigString("botOwner","");
        if (owners == null || owners.trim().isEmpty()) return false;
        for (String owner : owners.split(",")) {
            try {
                if (Long.parseLong(owner.trim()) == userID) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private static boolean writeAdmins(Set<Long> admins) {
        StringBuilder builder = new StringBuilder();
        for (Long admin : admins) {
            if (builder.length() > 0) builder.append(",");
            builder.append(admin);
        }
        if (!ConfigEditor.writeString("botAdmin",builder.toString())) return false;
        Main.reloadConfig();
        return true;
    }
}
