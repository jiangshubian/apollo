package com.ctrip.framework.apollo.portal.util;

import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.CommitDTO;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.entity.EntityPair;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.entity.bo.ItemBO;
import com.ctrip.framework.apollo.portal.entity.bo.KVEntity;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.entity.vo.Change;
import com.ctrip.framework.apollo.portal.entity.vo.ReleaseCompareResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @ClassName OptimizeUtils
 * @Description
 * @Author jiangshubian
 * @Date 2019/5/14 14:38
 * @Version 1.0
 */
public class OptimizeUtils {

    private static final Gson gson = new Gson();
    private static int SHOW_PASS_ORG_PREFIX_NUM = 3;
    private static String HIDE_STR = "username,password";
    private static String IGNORE_HIDE_STR = "spring,rocketmq";

    static {
        Map<String, String> envs = System.getenv();
        Properties sysProps = System.getProperties();

        if (envs.get("SHOW_PASS_ORG_PREFIX_NUM") != null && StringUtils.isNumeric(envs.get("SHOW_PASS_ORG_PREFIX_NUM"))) {
            SHOW_PASS_ORG_PREFIX_NUM = Integer.valueOf(envs.get("SHOW_PASS_ORG_PREFIX_NUM"));
        } else if (sysProps.getProperty("show_pass_org_prefix_num") != null && StringUtils.isNumeric(sysProps.getProperty("show_pass_org_prefix_num"))) {
            SHOW_PASS_ORG_PREFIX_NUM = Integer.valueOf(sysProps.getProperty("show_pass_org_prefix_num"));
        }
        if (envs.get("HIDE_STR") != null && StringUtils.isNumeric(envs.get("HIDE_STR"))) {
            HIDE_STR = envs.get("HIDE_STR");
        } else if (sysProps.getProperty("hide_str") != null && StringUtils.isNumeric(sysProps.getProperty("hide_str"))) {
            HIDE_STR = sysProps.getProperty("hide_str");
        }
    }

    public static List<ItemDTO> hidePassFromItemDTOLists(List<ItemDTO> itemDTOList) {

        if (itemDTOList == null || itemDTOList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        for (Iterator<ItemDTO> it = itemDTOList.iterator(); it.hasNext(); ) {
            hidePassFromItemDTO(it.next());
        }
        return itemDTOList;
    }

    public static void hidePassFromItemDTO(ItemDTO itemDTO) {

        if (itemDTO != null && isContainPass(itemDTO.getKey())) {
            itemDTO.setValue(generateNewHideVal(itemDTO.getValue()));
            itemDTO.setSignValue(getMD5(itemDTO.getValue()));
        }

    }

    /**
     * @param newValue 新值
     * @param orgValue 原始值
     * @return 若符合比较规则，返回true;否则返回false
     */
    public static boolean compareValue(String newValue, String orgValue) {
        return !StringUtils.isEmpty(newValue) && (newValue.equals(orgValue) || newValue.equals(generateNewHideVal(orgValue)));
    }


    /**
     * 切割字符串
     *
     * @param orgVal 原始值
     * @return 返回按照一定规则隐藏值
     */
    public static String generateNewHideVal(String orgVal) {
        if (StringUtils.isEmpty(orgVal)) {
            return orgVal;
        }
        int showLen = orgVal.length() <= SHOW_PASS_ORG_PREFIX_NUM ? 0 : SHOW_PASS_ORG_PREFIX_NUM;
        return orgVal.substring(0, showLen) + generateSpecialSymbol(showLen == 0 ? orgVal.length() : orgVal.length() - showLen);
    }

    public static boolean isContainPass(String str) {
        return !StringUtils.isEmpty(str) && containPass(str);
    }

    public static boolean containPass(String key) {
        String[] passs = HIDE_STR.split(",");
        boolean hasSuffixPass = false;
        for (String pass : passs) {
            if (key.endsWith(pass)) {
                hasSuffixPass = true;
                break;
            }
        }

        if (hasSuffixPass) {
            String[] ignoreStr = IGNORE_HIDE_STR.split(",");
            for (String ignorePassKey : ignoreStr) {
                if (key.startsWith(ignorePassKey)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * 生成特殊的符号
     *
     * @param len 长度
     * @return len的特殊的符号
     */
    private static String generateSpecialSymbol(int len) {
        StringBuilder sym = new StringBuilder(len);
        for (int symInx = 0; symInx < len; symInx++) {
            sym.append("*");
        }
        return sym.toString();
    }

    public static ReleaseDTO hidePassFromReleaseDTO(ReleaseDTO transform) {
        if (transform == null) {
            return transform;
        }
        Map<String, String> baseReleaseConfiguration = gson.fromJson(transform.getConfigurations(), GsonType.CONFIG);

        if (baseReleaseConfiguration.size() <= 0) {
            return transform;
        }
        //modify release configuration
        for (Map.Entry<String, String> entry : baseReleaseConfiguration.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (isContainPass(key)) {
                baseReleaseConfiguration.put(key, generateNewHideVal(value));
            }
        }
        transform.setConfigurations(gson.toJson(baseReleaseConfiguration));
        return transform;
    }

    public static List<ReleaseDTO> hidePassFromReleasesDTOLists(List<ReleaseDTO> batchTransform) {

        if (batchTransform == null || batchTransform.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        for (Iterator<ReleaseDTO> it = batchTransform.iterator(); it.hasNext(); ) {
            hidePassFromReleaseDTO(it.next());
        }
        return batchTransform;
    }

    public static NamespaceBO hidePassFromNamespaceBO(NamespaceBO namespaceBO) {
        List<ItemBO> itemBOList = namespaceBO.getItems();
        for (Iterator<ItemBO> it = itemBOList.iterator(); it.hasNext(); ) {
            ItemBO itemBO = it.next();
            hidePassFromItemDTO(itemBO.getItem());

            //also hide old value
            if (isContainPass(itemBO.getItem().getKey()) && !StringUtils.isEmpty(itemBO.getOldValue())) {
                itemBO.setOldValue(generateNewHideVal(itemBO.getOldValue()));
            }
        }
        return namespaceBO;
    }

    public static List<ReleaseHistoryBO> hidePassFromReleaseHistoryBOLists
            (List<ReleaseHistoryBO> releaseHistoryBOList) {
        if (releaseHistoryBOList == null) {
            return Collections.emptyList();
        }
        for (Iterator<ReleaseHistoryBO> it = releaseHistoryBOList.iterator(); it.hasNext(); ) {
            ReleaseHistoryBO releaseHistoryBO = it.next();
            for (Iterator<EntityPair<String>> entityPairIterator = releaseHistoryBO.getConfiguration().iterator(); entityPairIterator.hasNext(); ) {
                EntityPair<String> entityPair = entityPairIterator.next();
                if (isContainPass(entityPair.getFirstEntity())) {
                    entityPair.setSecondEntity(generateNewHideVal(entityPair.getSecondEntity()));
                }
            }
        }
        return releaseHistoryBOList;
    }

    public static ReleaseCompareResult hidePassFromReleaseCompareResult(ReleaseCompareResult releaseCompareResult) {
        if (releaseCompareResult != null) {
            for (Iterator<Change> changeIterator = releaseCompareResult.getChanges().iterator(); changeIterator.hasNext(); ) {
                Change change = changeIterator.next();
                EntityPair<KVEntity> entityEntityPair = change.getEntity();

                if (isContainPass(entityEntityPair.getFirstEntity().getKey())) {
                    entityEntityPair.getFirstEntity().setValue(generateNewHideVal(entityEntityPair.getFirstEntity().getValue()));
                }
                if (isContainPass(entityEntityPair.getSecondEntity().getKey())) {
                    entityEntityPair.getSecondEntity().setValue(generateNewHideVal(entityEntityPair.getSecondEntity().getValue()));
                }
            }
        }
        return releaseCompareResult;
    }

    public static List<CommitDTO> hidePassFromCommitDTOLists(List<CommitDTO> commitDTOS) {
        if (commitDTOS == null) {
            Collections.emptyList();
        }

        for (Iterator<CommitDTO> commitDTOIterator = commitDTOS.iterator(); commitDTOIterator.hasNext(); ) {

            CommitDTO commitDTO = commitDTOIterator.next();

            if (StringUtils.isEmpty(commitDTO.getChangeSets())) {
                continue;
            }
            ConfigChangeContent configChangeContent = gson.fromJson(commitDTO.getChangeSets(), CusGsonType.CONFIG);

            if (configChangeContent == null) {
                continue;
            }

            for (Iterator<ItemDTO> createItemsIt = configChangeContent.createItems.iterator(); createItemsIt.hasNext(); ) {
                hidePassFromItemDTO(createItemsIt.next());
            }
            for (Iterator<ItemDTO> deleteItemsIt = configChangeContent.deleteItems.iterator(); deleteItemsIt.hasNext(); ) {
                hidePassFromItemDTO(deleteItemsIt.next());
            }
            for (Iterator<ConfigChangeContent.ItemPair> updateItemsIt = configChangeContent.updateItems.iterator(); updateItemsIt.hasNext(); ) {
                ConfigChangeContent.ItemPair itemPair = updateItemsIt.next();
                hidePassFromItemDTO(itemPair.newItem);
                hidePassFromItemDTO(itemPair.oldItem);
            }

            commitDTO.setChangeSets(configChangeContent.toStr());
        }
        return commitDTOS;
    }

    public static String getMD5(String content) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(content.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String str = Integer.toHexString(b & 0xFF);
                if (str.length() == 1) {
                    sb.append("0");
                }
                sb.append(str);
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    public interface CusGsonType {

        Type CONFIG = new TypeToken<ConfigChangeContent>() {
        }.getType();

    }

    public static class ConfigChangeContent {

        private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

        private List<ItemDTO> createItems = new LinkedList<>();
        private List<ItemPair> updateItems = new LinkedList<>();
        private List<ItemDTO> deleteItems = new LinkedList<>();

        public String toStr() {
            return gson.toJson(this);
        }

        static class ItemPair {

            ItemDTO oldItem;
            ItemDTO newItem;

            public ItemPair(ItemDTO oldItem, ItemDTO newItem) {
                this.oldItem = oldItem;
                this.newItem = newItem;
            }
        }

    }
}
