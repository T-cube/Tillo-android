package utils;

import android.text.TextUtils;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by Administrator on 2016/07/26.
 */
public class StringUtils {
    /**
     * 将每三个数字加上逗号处理（通常使用金额方面的编辑）
     *
     * @param str 无逗号的数字
     * @return 加上逗号的数字
     */
    public static String addComma(String str) {
        long round = Math.round(Double.parseDouble(str));

        // 将传进数字反转
        String reverseStr = new StringBuilder(round + "").reverse().toString();
        String strTemp = "";
        for (int i = 0; i < reverseStr.length(); i++) {
            if (i * 3 + 3 > reverseStr.length()) {
                strTemp += reverseStr.substring(i * 3, reverseStr.length());
                break;
            }
            strTemp += reverseStr.substring(i * 3, i * 3 + 3) + ",";
        }
        // 将 【789,456,】 中最后一个【,】去除
        if (strTemp.endsWith(",")) {
            strTemp = strTemp.substring(0, strTemp.length() - 1);
        }
        // 将数字重新反转
        String resultStr = new StringBuilder(strTemp).reverse().toString();
        return resultStr;
    }


    /**
     * 转换
     *
     * @param number
     */
    public static String generateNumber(String number) {
        Double target = Double.parseDouble(number);
        if (target > 100000000) {
            return (target / 100000000) + "亿";
        } else if (target > 10000000) {
            return (target / 10000000) + "千万";
        } else if (target > 1000000) {
            return (target / 1000000) + "百万";
        } else if (target > 10000) {
            return (target / 10000) + "万";
        }
        return number;
    }

    public static String getRate(double amount, double total) {
        if ((int) total == 0) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("0.0000");
        return df.format(amount / total);
    }

    public static String removeZero(double d) {
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(d);
    }

    public static String removeZero(String s) {
        if (!TextUtils.isEmpty(s)) {
            try {

                double d = Double.parseDouble(s);
                DecimalFormat format = new DecimalFormat("0.#");
                return format.format(d);
            } catch (Exception e) {
                return "0";
            }
        } else {
            return "0";
        }

    }

    /**
     * 隐藏手机号中间四位
     */
    public static String generatePhoneNumber(String phone) {
        String maskNumber = phone.substring(0, 3) + "****" + phone.substring(7, phone.length());
        return maskNumber;
    }

    /**
     * 银行卡总共19位
     * 隐藏前15
     */
    public static String generateBankCardNubmer(String bankCardNumber) {
        String str = "***************" + bankCardNumber.substring(bankCardNumber.length() - 4, bankCardNumber.length());
        return str;
    }

    /**
     * 银行卡总共19位
     * 获取最后四位
     */
    public static String getLast4BankCardNubmer(String bankCardNumber) {
        String str = bankCardNumber.substring(bankCardNumber.length() - 4, bankCardNumber.length());
        return str;
    }

    /**
     * 银行卡总共19位
     * 每4位增加一个空格，最后不加
     */
    public static String setBlankPer4Number(String bankCardNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bankCardNumber.length(); i++) {
            if ((i == 4 || i == 8 || i == 12 || i == 16)) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(bankCardNumber.substring(i, i + 1));
        }
        return stringBuilder.toString();
    }


    /**
     * 判断邮箱是否合法
     */
    public static boolean isEmail(String email) {
        if (null == email || "".equals(email)) return false;
        //Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }


    //查看是否有空的值
    public static boolean isEmptys(String... editTexts) {
        if (editTexts == null) {
            return true;
        }
        for (String ed : editTexts) {
            String str = ed.trim();
            if (TextUtils.isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty(String... strings) {
        if (strings == null || strings.length == 0)
            return true;
        for (String ed : strings) {
            String str = ed.trim();
            if (TextUtils.isEmpty(str)) {
                return true;
            }
        }
        return false;
    }


    //其中一项是否有值
    public static boolean isHasValue(String... editTexts) {
        for (String ed : editTexts) {
            String str = ed.trim();
            if (!TextUtils.isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    //是否手机号
    public static boolean isPhone(String phoneStr) {
        String regex = "((\\d{11})|^((\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1})|(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1}))$)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(phoneStr);
        if (m.matches()) {
            return true;
        }
        return false;
    }

    /**
     * 匹配Luhn算法：可用于检测银行卡卡号
     *
     * @param cardNo
     * @return
     */
    public static boolean matchLuhn(String cardNo) {
        if (StringUtils.isEmptys(cardNo))
            return false;
        int[] cardNoArr = new int[cardNo.length()];
        for (int i = 0; i < cardNo.length(); i++) {
            cardNoArr[i] = Integer.valueOf(String.valueOf(cardNo.charAt(i)));
        }
        for (int i = cardNoArr.length - 2; i >= 0; i -= 2) {
            cardNoArr[i] <<= 1;
            cardNoArr[i] = cardNoArr[i] / 10 + cardNoArr[i] % 10;
        }
        int sum = 0;
        for (int i = 0; i < cardNoArr.length; i++) {
            sum += cardNoArr[i];
        }
        return sum % 10 == 0;
    }


    public static boolean paramsIsEmpty(String params, String type) {
        if (TextUtils.isEmpty(params)) {
            ToastUtils.getInstance().shortToast(type + "不能为空！");
            return true;
        } else
            return false;
    }

    public static String StringFilter(String str) throws PatternSyntaxException {
        // 只允许字母和数字 // String regEx ="[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

}
