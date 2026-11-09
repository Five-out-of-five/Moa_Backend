package com.fiveoutoffive.moa_backend.auth.application.usecase;

import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import com.fiveoutoffive.moa_backend.auth.application.command.SignupCommand;
import com.fiveoutoffive.moa_backend.auth.application.result.IssuedTokens;
import com.fiveoutoffive.moa_backend.auth.application.result.OAuthLoginResult;
import com.fiveoutoffive.moa_backend.auth.application.result.SignupTicketResponse;

public interface AuthUseCase {

    OAuthLoginResult handleOAuthLogin(SocialProvider provider, String providerUid, String email, String name);

    SignupTicketResponse getSignupTicket(String ticket);

    IssuedTokens signup(SignupCommand command);

    IssuedTokens refresh(String refreshToken);

    void logout(String refreshToken);
}
