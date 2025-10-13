package project.swp.spring.sebt_platform.service.impl;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.swp.spring.sebt_platform.config.VnpayConfig;
import project.swp.spring.sebt_platform.repository.WalletRepository;
import project.swp.spring.sebt_platform.repository.WalletTransactionRepository;
import project.swp.spring.sebt_platform.service.WalletLedgerService;
import project.swp.spring.sebt_platform.service.VnpayService;
import project.swp.spring.sebt_platform.util.Utils;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VnpayServiceImpl implements VnpayService {

    @Autowired
    private VnpayConfig config;

    private final WalletLedgerService walletLedgerService;

    @Autowired
    public VnpayServiceImpl(WalletTransactionRepository walletTransactionRepository,
                            WalletRepository walletRepository,
                            WalletLedgerService walletLedgerService) {
        this.walletLedgerService = walletLedgerService;
    }

    @Override
    public String createPaymentUrl(double amount, Long userId, HttpServletRequest request) throws UnsupportedEncodingException {

        String vnp_TxnRef = "ORDER" + System.currentTimeMillis() + "USER" + String.format("%05d", userId);
        String vnp_OrderInfo = "To up wallet of platform " + vnp_TxnRef;
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_IpAddr = "127.0.0.1";
        String vnp_TmnCode = config.getTmnCode();
        String orderType = "100000";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((long) amount*100));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        String urlReturn = config.getReturnUrl();
        vnp_Params.put("vnp_ReturnUrl", urlReturn);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

    walletLedgerService.createPendingTopUp(userId, vnp_TxnRef, BigDecimal.valueOf(amount));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = Utils.hmacSHA512(config.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return config.getPayUrl() +"?"+queryUrl;
    }

    @Override
    public TopUpIntent createTopUpIntent(Double amount, Long userId, HttpServletRequest request) throws UnsupportedEncodingException {
        String paymentUrl = createPaymentUrl(amount, userId, request);
        // Expiry: 15 minutes from now (VNPay expire date logic above)
        java.time.OffsetDateTime expiresAt = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(15);
        // Extract orderId from paymentUrl param vnp_TxnRef for returning to client
        String orderId = null;
        int idx = paymentUrl.indexOf("vnp_TxnRef=");
        if (idx >= 0) {
            String fragment = paymentUrl.substring(idx + 11);
            int amp = fragment.indexOf('&');
            orderId = amp > 0 ? fragment.substring(0, amp) : fragment;
        }
        return new TopUpIntent(orderId, paymentUrl, amount, expiresAt);
    }

    @Override
    public boolean validateReturn(Map<String, String> params) throws UnsupportedEncodingException {
        String secureHash = params.remove("vnp_SecureHash");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String checkHash = Utils.hmacSHA512(config.getHashSecret(), String.valueOf(hashData));
        return secureHash != null && secureHash.equalsIgnoreCase(checkHash);
    }

    @Override
    public void updateTransactionStatus(String orderId, boolean isSuccess) {
        walletLedgerService.completeTopUp(orderId, isSuccess, null);
    }

    @Override
    public void handleRefund(double amount, Long userId, HttpServletRequest request, boolean isRollback) throws IOException {
        String vnp_RequestId = Utils.getRandomNumber(8);
        String vnp_Version = "2.1.0";
        String vnp_Command = "refund";
        String vnp_TmnCode = config.getTmnCode();
        String vnp_TransactionType = request.getParameter("trantype");
        String vnp_TxnRef = "ORDER" + System.currentTimeMillis() + "USER" + String.format("%05d", userId);

        String vnp_Amount = String.valueOf((long) amount*100);
        String vnp_OrderInfo = "Refund transaction OrderId:" + vnp_TxnRef;
        String vnp_TransactionNo = ""; //Assuming value of the parameter "vnp_TransactionNo" does not exist on your system.
        String vnp_TransactionDate = request.getParameter("trans_date");
        String vnp_CreateBy = request.getParameter("user");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());

        String vnp_IpAddr = Utils.getIpAddress(request);

        JsonObject  vnp_Params = new JsonObject();

        vnp_Params.addProperty("vnp_RequestId", vnp_RequestId);
        vnp_Params.addProperty("vnp_Version", vnp_Version);
        vnp_Params.addProperty("vnp_Command", vnp_Command);
        vnp_Params.addProperty("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.addProperty("vnp_TransactionType", vnp_TransactionType);
        vnp_Params.addProperty("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.addProperty("vnp_Amount", vnp_Amount);
        vnp_Params.addProperty("vnp_OrderInfo", vnp_OrderInfo);

        if(vnp_TransactionNo != null && !vnp_TransactionNo.isEmpty())
        {
            vnp_Params.addProperty("vnp_TransactionNo", "{get value of vnp_TransactionNo}");
        }

        vnp_Params.addProperty("vnp_TransactionDate", vnp_TransactionDate);
        vnp_Params.addProperty("vnp_CreateBy", vnp_CreateBy);
        vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.addProperty("vnp_IpAddr", vnp_IpAddr);

        String hash_Data= String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode,
                vnp_TransactionType, vnp_TxnRef, vnp_Amount, vnp_TransactionNo, vnp_TransactionDate,
                vnp_CreateBy, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);

        String vnp_SecureHash = Utils.hmacSHA512(config.getHashSecret(), hash_Data.toString());

        vnp_Params.addProperty("vnp_SecureHash", vnp_SecureHash);

        if(!isRollback){
            // Refund initiation flow TBD; for now we skip creating a negative pending transaction
        }

        URL url = new URL (config.getApiUrl());
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(vnp_Params.toString());
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        System.out.println("nSending 'POST' request to URL : " + url);
        System.out.println("Post Data : " + vnp_Params);
        System.out.println("Response Code : " + responseCode);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String output;
        StringBuilder response = new StringBuilder();
        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        in.close();
        System.out.println(response.toString());
    }
}