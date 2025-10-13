package project.swp.spring.sebt_platform.service.impl;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.swp.spring.sebt_platform.config.VnpayConfig;
import project.swp.spring.sebt_platform.model.WalletEntity;
import project.swp.spring.sebt_platform.model.WalletTransactionEntity;
import project.swp.spring.sebt_platform.model.enums.TransactionStatus;
import project.swp.spring.sebt_platform.repository.WalletRepository;
import project.swp.spring.sebt_platform.repository.WalletTransactionRepository;
import project.swp.spring.sebt_platform.service.VnpayService;
import project.swp.spring.sebt_platform.util.Utils;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VnpayServiceImpl implements VnpayService {

    @Autowired
    private VnpayConfig config;

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;

    @Autowired
    public VnpayServiceImpl(WalletTransactionRepository walletTransactionRepository,
                            WalletRepository walletRepository) {
        this.walletTransactionRepository = walletTransactionRepository;
        this.walletRepository = walletRepository;
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

        WalletEntity wallet = walletRepository.findByUserId(userId);

        // Lưu giao dịch vào database với trạng thái "Chưa thanh toán"
        WalletTransactionEntity transaction = new WalletTransactionEntity();
        transaction.setOrderId(vnp_TxnRef);
        transaction.setAmount(BigDecimal.valueOf(amount));
        transaction.setBalanceBefore(wallet.getBalance());
        transaction.setBalanceAfter(BigDecimal.ZERO);
        transaction.setStatus(TransactionStatus.PENDING);

        wallet.getTransactions().add(transaction);
        walletRepository.save(wallet);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
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
    public boolean validateReturn(Map<String, String> params) throws UnsupportedEncodingException {
        String secureHash = params.remove("vnp_SecureHash");

        List fieldNames = new ArrayList(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) params.get(fieldName);
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
        String checkHash = Utils.hmacSHA512(config.getHashSecret(), String.valueOf(hashData));
        return secureHash.equalsIgnoreCase(checkHash);
    }

    @Override
    public void updateTransactionStatus(String orderId, boolean isSuccess) {
        WalletTransactionEntity transaction = walletTransactionRepository.findByOrderId(orderId);
        WalletEntity wallet = walletRepository.findByOrderId(orderId);
        if (transaction != null && transaction.getStatus() == TransactionStatus.PENDING) {
            if (isSuccess) {
                transaction.setStatus(TransactionStatus.COMPLETED);
                BigDecimal newBalance = wallet.getBalance().add(transaction.getAmount());
                transaction.setBalanceAfter(newBalance);
                wallet.setBalance(newBalance);
                wallet.setUpdated_at(LocalDateTime.now());
                walletRepository.save(wallet);
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setBalanceAfter(transaction.getBalanceBefore());
            }
            walletTransactionRepository.save(transaction);
        }
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
            WalletEntity wallet = walletRepository.findByUserId(userId);
            if(wallet.getBalance().doubleValue() < amount) return;

            // Lưu giao dịch vào database với trạng thái "Chưa thanh toán"
            WalletTransactionEntity transaction = new WalletTransactionEntity();
            transaction.setOrderId(vnp_TxnRef);
            transaction.setAmount(BigDecimal.valueOf(-amount));
            transaction.setBalanceBefore(wallet.getBalance());
            transaction.setBalanceAfter(BigDecimal.ZERO);
            transaction.setStatus(TransactionStatus.PENDING);

            wallet.getTransactions().add(transaction);
            walletRepository.save(wallet);
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