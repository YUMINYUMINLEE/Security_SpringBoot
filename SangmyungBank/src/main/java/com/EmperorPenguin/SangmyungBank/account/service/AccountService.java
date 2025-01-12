package com.EmperorPenguin.SangmyungBank.account.service;

import com.EmperorPenguin.SangmyungBank.account.dto.AccountCreateReq;
import com.EmperorPenguin.SangmyungBank.account.dto.AccountInquiryRes;
import com.EmperorPenguin.SangmyungBank.account.dto.TransferReq;
import com.EmperorPenguin.SangmyungBank.account.entity.Account;
import com.EmperorPenguin.SangmyungBank.account.repository.AccountRepository;
import com.EmperorPenguin.SangmyungBank.baseUtil.config.DateConfig;
import com.EmperorPenguin.SangmyungBank.baseUtil.exception.BaseException;
import com.EmperorPenguin.SangmyungBank.baseUtil.exception.ExceptionMessages;
import com.EmperorPenguin.SangmyungBank.member.service.MemberService;
import com.EmperorPenguin.SangmyungBank.otp.dto.OtpRandomRes;
import com.EmperorPenguin.SangmyungBank.otp.dto.OtpValidReq;
import com.EmperorPenguin.SangmyungBank.otp.service.OtpService;
import com.EmperorPenguin.SangmyungBank.transaction.entity.Transaction;
import com.EmperorPenguin.SangmyungBank.member.entity.Member;
import com.EmperorPenguin.SangmyungBank.transaction.service.TransactionService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import javax.transaction.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final TransactionService transactionService;
    private final OtpService otpService;
    // 계좌 예시102-82-01669
    private Long StartAccountNumber = 1028201669L;

    @Transactional
    public void createAccount(AccountCreateReq accountCreateReq) {
        String loginId = accountCreateReq.getLoginId();
        String password = accountCreateReq.getAccountPassword();

        checkPassword(password);
        // 전달 받은 아이디를 통해 사용자가 있는지 확인 없다면 사용자가 없다는 예외를 발생.
        memberService.checkEmptyMember(loginId);

        try{
            accountRepository.save(accountCreateReq.toEntity(
                    memberService.getMember(loginId),
                    StartAccountNumber++,
                    passwordEncoder.encode(password),
                    0L));
        }catch (Exception e)
        {
            e.printStackTrace();
            throw new BaseException("계좌생성에 실패했습니다.");
        }
    }

    @Transactional
    public void validationAccount(TransferReq transferReq){
        String loginId = transferReq.getLoginId();
        Long sendAccount = transferReq.getSendAccountNumber();
        String password = transferReq.getAccountPassword();

        if(transferReq.getMyAccountNumber() == transferReq.getSendAccountNumber()){
            throw new BaseException(ExceptionMessages.ERROR_ACCOUNT_CURRING);
        }

        Account myAccount = accountRepository
                .findAccountByAccountNumber(transferReq.getMyAccountNumber())
                .orElseThrow(() -> new BaseException(ExceptionMessages.ERROR_ACCOUNT_NOT_FOUND));

        // 사용자의 아이디로부터 자신의 계좌가 맞는지 확인.
        checkAccount(myAccount, memberService.getMember(loginId));

        // 보내는 계좌 번호가 존재하는지 확인.
        if(!accountRepository.existsAccountByAccountNumber(sendAccount)){
            throw new BaseException(ExceptionMessages.ERROR_ACCOUNT_NOT_FOUND);
        }
        // 사용자의 계좌 비밀번호가 맞는지 확인.
        if(!passwordEncoder.matches(password, myAccount.getAccountPassword())) {
            throw new BaseException(ExceptionMessages.ERROR_ACCOUNT_PASSWORD_NOT_MATCH);
        }
        // 사용자의 계좌에 충분한 잔액이 있는지 확인.
        if(myAccount.getBalance() < transferReq.getBalance()) {
            throw new BaseException(ExceptionMessages.ERROR_ACCOUNT_BALANCE);
        }
    }

    @Transactional
    public void validOtp(OtpRandomRes otpRandomRes, OtpValidReq otpValidReq) throws NoSuchAlgorithmException {
        Member member = memberService.getMember(otpValidReq.getLoginId());
        otpService.validationOtp(member, otpRandomRes, otpValidReq.getHashedData());
    }

    @Transactional
    public void transaction(TransferReq transferReq)
    {
       try {
            accountRepository.updateMyBalance(transferReq.getBalance(),
                    transferReq.getMyAccountNumber());
            accountRepository.updateBalance(transferReq.getBalance(),
                    transferReq.getSendAccountNumber());
            // 전달자의 거래내역을 저장
           transactionService.saveData(Transaction.builder()
                    .sendAccount(transferReq.getMyAccountNumber())
                    .toSenderMessage(transferReq.getToSenderMessage())
                    .receiveAccount(transferReq.getSendAccountNumber())
                    .toReceiverMessage(transferReq.getToReceiverMessage())
                    .balance(-transferReq.getBalance())
                    .transactionDate(new DateConfig().getDateTime())
                    .build());
            // 받는이의 거래내역을 저장
            transactionService.saveData(Transaction.builder()
                    .sendAccount(transferReq.getSendAccountNumber())
                    .toSenderMessage(transferReq.getToSenderMessage())
                    .receiveAccount(transferReq.getMyAccountNumber())
                    .toReceiverMessage(transferReq.getToReceiverMessage())
                    .balance(transferReq.getBalance())
                    .transactionDate(new DateConfig().getDateTime())
                    .build());
        }catch (Exception e){
            e.printStackTrace();
            throw new BaseException("계좌이체에 실패했습니다.");
        }
    }

    @Transactional
    public List<AccountInquiryRes> inquiry(String loginId) {
        // 정확한 사용자를 넘겨줬는지 확인
        memberService.checkEmptyMember(loginId);

        return accountRepository
                .findAllByMemberId(memberService.getMember(loginId))
                .stream()
                .map(Account::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AccountInquiryRes> getName(Long accountNumber) {

        return accountRepository
                .findNameByAccountNumber(accountNumber)
                .stream()
                .map(Account::toDto)
                .collect(Collectors.toList());
    }

    public Account getAccount(Long accountNumber){
        return accountRepository.findAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new BaseException(ExceptionMessages.ERROR_ACCOUNT_NOT_FOUND));
    }

    private void checkPassword(String password) {
        // 계좌 비밀번호는 숫자로 6자로 구성되어있다.
        Pattern passwordExpression = Pattern.compile("[0-9]{6}");
        if(!passwordExpression.matcher(password).matches()){
            throw new BaseException(ExceptionMessages.ERROR_ACCOUNT_PASSWORD_FORMAT);
        }
    }

    private void checkAccount(Account account, Member member){
        if(!accountRepository.findAllByMemberId(member).contains(account)){
            throw new BaseException("전달받은 계좌는 사용자의 계좌가 아닙니다.");
        }
    }
}
