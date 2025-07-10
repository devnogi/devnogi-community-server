package until.the.eternity.dcs.domain.post.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.post.dto.request.PostCreateRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostUpdateRequest;
import until.the.eternity.dcs.domain.post.dto.response.PostDetailResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostSummaryResponse;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.exception.PostDeletionNotAllowedException;
import until.the.eternity.dcs.domain.post.exception.PostModifyForbiddenException;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.tag.entity.PostTag;
import until.the.eternity.dcs.domain.tag.entity.Tag;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostConverter postConverter;
    private final UserService fakeUserService;

    //todo 추후에 사용자 인증부분 추가해야될듯(token 유효라던가)
    //todo postTag 관련 로직도 추후 필요
    @Transactional
    public PostSummaryResponse createPost(PostCreateRequest request){
        UserSummary user = getCurrentUser();
        List<PostTag> postTagList = new ArrayList<>();

        Post post = postConverter.fromCreateRequestToPost(request,user.getId(),postTagList);
        Post savedPost = postRepository.save(post);

        return postConverter.fromPostToPostSummaryResponse(savedPost);
    }


    public PostDetailResponse findPost(Long id) {
        Post post = findById(id);

        return postConverter.fromPostToPostDetailResponse(post);
    }


    public Page<PostSummaryResponse> findPosts(CustomPageRequest request) {
        Pageable pageable = request.toPageable();

        Page<Post> posts = postRepository.findAllByIsDeletedFalseAndIsBlockedFalse(pageable);

        return posts.map(postConverter::fromPostToPostSummaryResponse);
    }

    @Transactional
    public PostSummaryResponse updatePost(Long id, PostUpdateRequest postUpdateRequest) {

        UserSummary user = getCurrentUser();

        // 우선 본인 글만 수정할 수 있게 (ADMIN 권한도 게시글 내용을 수정할 수 있게 할건지)
        if(!Objects.equals(user.getId(), postUpdateRequest.userId())){
            throw new PostModifyForbiddenException();
        }

        //todo 우선 runtimeException으로 처리 차후 customException 추가해야됨
        Post post = findById(id);

        List<PostTag> postTagList = convertStringToPostTag(post,postUpdateRequest.tags());

        post.update(postUpdateRequest.title(),postUpdateRequest.content(),postUpdateRequest.isDraft(),postTagList, user.getId());

        return postConverter.fromPostToPostSummaryResponse(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long id){
        UserSummary user = getCurrentUser();
        Post post = findById(id);

        if(!Objects.equals(user.getId(), post.getUserId())){
            throw new PostDeletionNotAllowedException();
        }

        postRepository.delete(post);
    }

    private UserSummary getCurrentUser() {
        return fakeUserService.getCurrentUser();
    }

    private Post findById(Long id) {
        return postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

//    //todo CommentController의 findByPostId API 를 이용해 프론트에서 댓글만 따로 받아오는건 어떤지
//    //우선은 기존 글에서 comment 리스트를 받아와 각 요소를 CommentPageResponseItem로 변환 한 후 반환하는 로직 사용
//    private List<CommentPageResponseItem> getCommentPageResponseItemList(List<Comment> commentList){
//
//        List<CommentPageResponseItem> commentPageResponseItemList = new ArrayList<>();
//
//        commentList.forEach(comment -> {
//
//            commentPageResponseItemList.add(CommentPageResponseItem.from(comment, ));
//        });
//
//        return commentPageResponseItemList;
//    }


    //todo Tag, PostTag service 구현 후 로직 수정 필요
    private List<PostTag> convertStringToPostTag(Post post, List<String> stringList){
        List<PostTag> postTagList = new ArrayList<>();

        //당장은 중복검사가 안됨
        stringList.forEach(string->{
            Tag tag = Tag.builder().name(string).build();
            postTagList.add(PostTag.builder().post(post).tag(tag).build());
        });

        return postTagList;
    }
}
