package com.votescroll.service;

import com.votescroll.dto.*;
import com.votescroll.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class ApprovalService {

    public ApprovalSummaryDto getSummary() {
        List<ApprovalItemDto> pending = new ArrayList<>();
        List<ApprovalItemDto> deletions = new ArrayList<>();

        // Pending characters (user-created, awaiting approval)
        AnimeCharacter.<AnimeCharacter>list("status = ?1 AND ownerId IS NOT NULL", ContentStatus.PENDING)
            .forEach(c -> pending.add(toItem(c.id, "CHARACTER", c.name, c.anime, c.ownerId, c.createdAt.toString())));

        // Pending polls
        Poll.<Poll>list("status = ?1 AND ownerId IS NOT NULL AND isPrivate = false", ContentStatus.PENDING)
            .forEach(p -> pending.add(toItem(p.id, "POLL", p.question, p.anime, p.ownerId, p.createdAt.toString())));

        // Pending multi-polls
        MultiPoll.<MultiPoll>list("status = ?1 AND ownerId IS NOT NULL AND isPrivate = false", ContentStatus.PENDING)
            .forEach(mp -> pending.add(toItem(mp.id, "MULTI_POLL", mp.question, mp.anime, mp.ownerId, mp.createdAt.toString())));

        // Deletion requests
        Poll.<Poll>list("deletePending = true")
            .forEach(p -> deletions.add(toDeletion(p.id, "POLL", p.question, p.anime, p.ownerId, p.createdAt.toString())));

        MultiPoll.<MultiPoll>list("deletePending = true")
            .forEach(mp -> deletions.add(toDeletion(mp.id, "MULTI_POLL", mp.question, mp.anime, mp.ownerId, mp.createdAt.toString())));

        return ApprovalSummaryDto.builder()
            .pendingContent(pending)
            .pendingDeletions(deletions)
            .build();
    }

    // ── Content approvals ─────────────────────────────────────────────────────

    @Transactional
    public void approveCharacter(String id) {
        AnimeCharacter c = findChar(id);
        c.status = ContentStatus.APPROVED;
        log.info("Admin approved character {}", id);
    }

    @Transactional
    public void rejectCharacter(String id) {
        AnimeCharacter c = findChar(id);
        c.status = ContentStatus.REJECTED;
        log.info("Admin rejected character {}", id);
    }

    @Transactional
    public void approvePoll(String id) {
        Poll p = findPoll(id);
        p.status = ContentStatus.APPROVED;
        log.info("Admin approved poll {}", id);
    }

    @Transactional
    public void rejectPoll(String id) {
        Poll p = findPoll(id);
        p.status = ContentStatus.REJECTED;
        log.info("Admin rejected poll {}", id);
    }

    @Transactional
    public void approveMultiPoll(String id) {
        MultiPoll mp = findMp(id);
        mp.status = ContentStatus.APPROVED;
        log.info("Admin approved multi-poll {}", id);
    }

    @Transactional
    public void rejectMultiPoll(String id) {
        MultiPoll mp = findMp(id);
        mp.status = ContentStatus.REJECTED;
        log.info("Admin rejected multi-poll {}", id);
    }

    // ── Deletion approvals ────────────────────────────────────────────────────

    @Transactional
    public void approvePollDeletion(String id) {
        Poll p = findPoll(id);
        Vote.delete("pollId", id);
        VoteHistory.delete("pollId", id);
        p.delete();
        log.info("Admin approved deletion of poll {}", id);
    }

    @Transactional
    public void rejectPollDeletion(String id) {
        Poll p = findPoll(id);
        p.deletePending = false;
        log.info("Admin rejected deletion of poll {}", id);
    }

    @Transactional
    public void approveMultiPollDeletion(String id) {
        MultiPoll mp = findMp(id);
        MultiPollVote.delete("pollId", id);
        VoteHistory.delete("pollId", id);
        mp.delete();
        log.info("Admin approved deletion of multi-poll {}", id);
    }

    @Transactional
    public void rejectMultiPollDeletion(String id) {
        MultiPoll mp = findMp(id);
        mp.deletePending = false;
        log.info("Admin rejected deletion of multi-poll {}", id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AnimeCharacter findChar(String id) {
        return AnimeCharacter.<AnimeCharacter>findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Character not found: " + id));
    }

    private Poll findPoll(String id) {
        return Poll.<Poll>findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Poll not found: " + id));
    }

    private MultiPoll findMp(String id) {
        return MultiPoll.<MultiPoll>findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Multi-poll not found: " + id));
    }

    private ApprovalItemDto toItem(String id, String type, String title, String anime,
                                   String ownerId, String createdAt) {
        String ownerUsername = resolveUsername(ownerId);
        return ApprovalItemDto.builder().id(id).type(type).title(title).anime(anime)
            .ownerId(ownerId).ownerUsername(ownerUsername).createdAt(createdAt).isDeletion(false).build();
    }

    private ApprovalItemDto toDeletion(String id, String type, String title, String anime,
                                        String ownerId, String createdAt) {
        String ownerUsername = resolveUsername(ownerId);
        return ApprovalItemDto.builder().id(id).type(type).title(title).anime(anime)
            .ownerId(ownerId).ownerUsername(ownerUsername).createdAt(createdAt).isDeletion(true).build();
    }

    private String resolveUsername(String ownerId) {
        if (ownerId == null) return null;
        AppUser u = AppUser.findById(ownerId);
        return u != null ? u.username : ownerId;
    }
}
