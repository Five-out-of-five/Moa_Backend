package com.fiveoutoffive.moa_backend.ideacard.application.usecase;

import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.command.AttachFileCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.command.CardMoveCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.command.CommentCreateCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.command.CommentUpdateCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.command.IdeaCardCreateCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.command.IdeaCardUpdateCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.result.CommentResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.EditLockResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardDetailResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardSummaryResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.ReactionDetailResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.ReactionSummaryResponse;
import java.util.List;

public interface IdeaCardUseCase {

    List<IdeaCardSummaryResponse> getCards(Long accountId, Long roomId, Long sessionId);

    IdeaCardDetailResponse create(Long accountId, Long roomId, IdeaCardCreateCommand command);

    IdeaCardDetailResponse getDetail(Long accountId, Long roomId, Long cardId);

    IdeaCardDetailResponse update(Long accountId, Long roomId, Long cardId, IdeaCardUpdateCommand command);

    EditLockResponse lock(Long accountId, Long roomId, Long cardId);

    void unlock(Long accountId, Long roomId, Long cardId);

    void delete(Long accountId, Long roomId, Long cardId);

    List<IdeaCardSummaryResponse> move(Long accountId, Long roomId, Long cardId, CardMoveCommand command);

    List<ReactionSummaryResponse> addReaction(Long accountId, Long roomId, Long cardId, String emoji);

    List<ReactionDetailResponse> getReactions(Long accountId, Long roomId, Long cardId);

    List<ReactionSummaryResponse> removeReaction(Long accountId, Long roomId, Long cardId, String emoji);

    List<CommentResponse> getComments(Long accountId, Long roomId, Long cardId);

    CommentResponse addComment(Long accountId, Long roomId, Long cardId, CommentCreateCommand command);

    CommentResponse updateComment(Long accountId, Long roomId, Long cardId, Long commentId, CommentUpdateCommand command);

    void deleteComment(Long accountId, Long roomId, Long cardId, Long commentId);

    List<FileResponse> attachFile(Long accountId, Long roomId, Long cardId, AttachFileCommand command);

    void removeFile(Long accountId, Long roomId, Long cardId, Long fileId);
}
