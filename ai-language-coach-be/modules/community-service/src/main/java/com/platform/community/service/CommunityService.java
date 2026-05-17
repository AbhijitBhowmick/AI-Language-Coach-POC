package com.platform.community.service;

import com.platform.community.dto.*;
import com.platform.community.entity.*;
import com.platform.community.entity.CommunityMember.MemberRole;
import com.platform.community.repository.*;
import com.platform.common.exception.AILanguageBusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommunityService {

    private static final Logger log = LoggerFactory.getLogger(CommunityService.class);

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository memberRepository;
    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final CommunityActivityRepository activityRepository;

    public CommunityService(CommunityRepository communityRepository,
                           CommunityMemberRepository memberRepository,
                           CommunityPostRepository postRepository,
                           CommunityCommentRepository commentRepository,
                           CommunityActivityRepository activityRepository) {
        this.communityRepository = communityRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional
    public CommunityResponseDTO createCommunity(CreateCommunityRequest request, UUID adminTeacherId, String tenantId) {
        Community community = new Community(
            request.getName(),
            request.getDescription(),
            tenantId,
            adminTeacherId,
            adminTeacherId
        );
        community = communityRepository.save(community);

        CommunityMember member = new CommunityMember(community, adminTeacherId, MemberRole.ADMIN);
        memberRepository.save(member);

        log.info("Community created: {} by admin teacher: {}", community.getName(), adminTeacherId);
        return toCommunityResponse(community, adminTeacherId);
    }

    public Page<CommunityResponseDTO> getCommunities(String tenantId, UUID userId, String role, Pageable pageable) {
        List<Community> communities;
        String upperRole = role != null ? role.toUpperCase() : "";

        if ("SUPER_ADMIN".equals(upperRole) || "BUSINESS_ADMIN".equals(upperRole)) {
            communities = communityRepository.findAll().stream().filter(Community::isActive).toList();
        } else if ("USER_STUDENT".equals(upperRole)) {
            List<UUID> joinedIds = memberRepository.findByUserIdAndBlockedFalse(userId).stream()
                .map(m -> m.getCommunity().getId())
                .toList();
            communities = communityRepository.findAllById(joinedIds).stream().filter(Community::isActive).toList();
        } else {
            communities = communityRepository.findByTenantIdAndIsActiveTrue(tenantId, Pageable.unpaged()).getContent();
        }

        List<CommunityResponseDTO> dtos = communities.stream()
            .map(c -> toCommunityResponse(c, userId))
            .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<CommunityResponseDTO> pageContent = start >= dtos.size() ? List.of() : dtos.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, dtos.size());
    }

    public CommunityResponseDTO getCommunity(UUID communityId, UUID userId) {
        Community community = communityRepository.findById(communityId)
            .orElseThrow(() -> new RuntimeException("Community not found"));
        return toCommunityResponse(community, userId);
    }

    @Transactional
    public void deleteCommunity(UUID communityId, UUID adminTeacherId) {
        if (!communityRepository.existsByIdAndAdminTeacherId(communityId, adminTeacherId)) {
            throw new RuntimeException("Not authorized to delete this community");
        }
        communityRepository.deleteById(communityId);
        log.info("Community deleted: {}", communityId);
    }

    @Transactional
    public CommunityMemberResponseDTO joinCommunity(UUID communityId, UUID userId) {
        if (memberRepository.existsByCommunityIdAndUserId(communityId, userId)) {
            throw new AILanguageBusinessException("Already a member", "ERR_COMM_001", 409);
        }

        Community community = communityRepository.findById(communityId)
            .orElseThrow(() -> new RuntimeException("Community not found"));

        CommunityMember member = new CommunityMember(community, userId, MemberRole.MEMBER);
        member = memberRepository.save(member);

        log.info("User {} joined community {}", userId, communityId);
        return toMemberResponse(member);
    }

    @Transactional
    public void leaveCommunity(UUID communityId, UUID userId) {
        memberRepository.deleteByCommunityIdAndUserId(communityId, userId);
        log.info("User {} left community {}", userId, communityId);
    }

    @Transactional
    public CommunityMemberResponseDTO addMemberAsAdmin(UUID communityId, UUID userId, UUID adminTeacherId) {
        if (!communityRepository.existsByIdAndAdminTeacherId(communityId, adminTeacherId)) {
            throw new RuntimeException("Not authorized");
        }

        Community community = communityRepository.findById(communityId)
            .orElseThrow(() -> new RuntimeException("Community not found"));

        CommunityMember member = new CommunityMember(community, userId, MemberRole.ADMIN);
        member = memberRepository.save(member);
        return toMemberResponse(member);
    }

    @Transactional
    public void blockMember(UUID communityId, UUID userId, UUID adminTeacherId) {
        if (!communityRepository.existsByIdAndAdminTeacherId(communityId, adminTeacherId)) {
            throw new RuntimeException("Not authorized");
        }

        CommunityMember member = memberRepository.findByCommunityIdAndUserId(communityId, userId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        
        member.setBlocked(true);
        memberRepository.save(member);
        log.info("User {} blocked from community {}", userId, communityId);
    }

    @Transactional
    public void unblockMember(UUID communityId, UUID userId, UUID adminTeacherId) {
        if (!communityRepository.existsByIdAndAdminTeacherId(communityId, adminTeacherId)) {
            throw new RuntimeException("Not authorized");
        }

        CommunityMember member = memberRepository.findByCommunityIdAndUserId(communityId, userId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        
        member.setBlocked(false);
        memberRepository.save(member);
    }

    public Page<CommunityMemberResponseDTO> getMembers(UUID communityId, Pageable pageable) {
        return memberRepository.findByCommunityIdAndBlockedFalse(communityId, pageable)
            .map(this::toMemberResponse);
    }

    public boolean isMember(UUID communityId, UUID userId) {
        return memberRepository.existsByCommunityIdAndUserIdAndBlockedFalse(communityId, userId);
    }

    public boolean isAdmin(UUID communityId, UUID userId) {
        return memberRepository.findByCommunityIdAndUserId(communityId, userId)
            .map(m -> m.getRole() == MemberRole.ADMIN && !m.isBlocked())
            .orElse(false);
    }

    @Transactional
    public PostResponseDTO createPost(UUID communityId, UUID userId, String content) {
        if (!isMember(communityId, userId)) {
            throw new RuntimeException("Must be a member to post");
        }

        Community community = communityRepository.findById(communityId)
            .orElseThrow(() -> new RuntimeException("Community not found"));

        CommunityPost post = new CommunityPost(community, userId, content);
        post = postRepository.save(post);

        recordActivity(userId, community.getTenantId(), CommunityActivity.ActivityType.POST_CREATED, 10, community);

        log.info("Post created in community {} by user {}", communityId, userId);
        return toPostResponse(post);
    }

    public Page<PostResponseDTO> getPosts(UUID communityId, Pageable pageable) {
        return postRepository.findByCommunityIdOrderByCreatedAtDesc(communityId, pageable)
            .map(this::toPostResponse);
    }

    @Transactional
    public void deletePost(UUID postId, UUID userId, boolean isAdmin) {
        CommunityPost post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUserId().equals(userId) && !isAdmin) {
            throw new RuntimeException("Not authorized to delete this post");
        }
        postRepository.delete(post);
    }

    @Transactional
    public CommentResponseDTO addComment(UUID postId, UUID userId, String content) {
        CommunityPost post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!isMember(post.getCommunity().getId(), userId)) {
            throw new RuntimeException("Must be a member to comment");
        }

        CommunityComment comment = new CommunityComment(post, userId, content);
        comment = commentRepository.save(comment);

        post.incrementComments();
        postRepository.save(post);

        recordActivity(userId, post.getCommunity().getTenantId(), CommunityActivity.ActivityType.COMMENT_CREATED, 5, post.getCommunity());

        return toCommentResponse(comment);
    }

    public Page<CommentResponseDTO> getComments(UUID postId, Pageable pageable) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable)
            .map(this::toCommentResponse);
    }

    @Transactional
    public void likePost(UUID postId, UUID userId) {
        CommunityPost post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        post.incrementLikes();
        postRepository.save(post);

        recordActivity(userId, post.getCommunity().getTenantId(), CommunityActivity.ActivityType.POST_LIKED, 2, post.getCommunity());
    }

    @Transactional
    public void recordActivity(UUID userId, String tenantId, CommunityActivity.ActivityType type, int score, Community community) {
        CommunityActivity activity = new CommunityActivity(userId, tenantId, type, score, community);
        activityRepository.save(activity);
    }

    private CommunityResponseDTO toCommunityResponse(Community c) {
        return toCommunityResponse(c, null);
    }

    private CommunityResponseDTO toCommunityResponse(Community c, UUID userId) {
        Boolean joined = userId != null ? memberRepository.existsByCommunityIdAndUserIdAndBlockedFalse(c.getId(), userId) : null;
        return new CommunityResponseDTO(
            c.getId(), c.getName(), c.getDescription(), c.getTenantId(),
            c.getCreatedBy(), c.getAdminTeacherId(), c.isActive(), c.getCreatedAt(), joined
        );
    }

    private CommunityMemberResponseDTO toMemberResponse(CommunityMember m) {
        return new CommunityMemberResponseDTO(
            m.getId(), m.getCommunity().getId(), m.getUserId(),
            m.getRole().name(), m.isBlocked(), m.getJoinedAt()
        );
    }

    private PostResponseDTO toPostResponse(CommunityPost p) {
        return new PostResponseDTO(
            p.getId(), p.getCommunity().getId(), p.getUserId(),
            p.getContent(), p.getLikesCount(), p.getCommentsCount(), p.getCreatedAt()
        );
    }

    private CommentResponseDTO toCommentResponse(CommunityComment c) {
        return new CommentResponseDTO(
            c.getId(), c.getPost().getId(), c.getUserId(),
            c.getContent(), c.getCreatedAt()
        );
    }
}