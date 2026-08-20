package com.uang.backend.service;

import com.uang.backend.entity.LostItem;
import com.uang.backend.entity.User;
import com.uang.backend.exception.ForbiddenException;
import com.uang.backend.repository.LostItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LostItemServiceTest {

    @Mock
    private LostItemRepository repository;

    @Mock
    private UserService userService;

    private LostItemService service;

    private LostItem sampleItem;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        service = new LostItemService(repository, userService);
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setPhone("13800001111");
        sampleUser.setNickname("张三");
        sampleUser.setStatus(User.Status.NORMAL);
        sampleUser.setCreateTime(LocalDateTime.now());
        sampleItem = new LostItem();
        sampleItem.setId(1L);
        sampleItem.setTitle("黑色钱包");
        sampleItem.setDescription("在图书馆门口捡到");
        sampleItem.setLocation("图书馆");
        sampleItem.setContact("13800001111");
        sampleItem.setImageUrl("https://example.com/img.jpg");
        sampleItem.setCreateTime(LocalDateTime.now());
        sampleItem.setUser(sampleUser);
    }

    @Test
    void updateClaimed_shouldUpdateClaimedForOwner() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(repository.save(sampleItem)).thenReturn(sampleItem);

        LostItem result = service.updateClaimed(1L, 1L, true);

        assertThat(result.isClaimed()).isTrue();
        verify(repository).save(sampleItem);
    }

    @Test
    void updateClaimed_shouldAllowUnclaimingForOwner() {
        sampleItem.setClaimed(true);
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(repository.save(sampleItem)).thenReturn(sampleItem);

        LostItem result = service.updateClaimed(1L, 1L, false);

        assertThat(result.isClaimed()).isFalse();
        verify(repository).save(sampleItem);
    }

    @Test
    void updateClaimed_shouldThrowForbiddenWhenNotOwner() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));

        assertThatThrownBy(() -> service.updateClaimed(1L, 999L, true))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("无权");
        verify(repository, never()).save(any(LostItem.class));
    }


    @Test
    void create_shouldSetIdNullSetCreateTimeAndAssociateUser() {
        LostItem input = new LostItem();
        input.setId(999L);
        input.setTitle("测试物品");
        input.setImageUrl("https://example.com/test.jpg");

        when(userService.findById(1L)).thenReturn(sampleUser);
        when(repository.save(any(LostItem.class))).thenReturn(sampleItem);

        LostItem result = service.create(input, 1L);

        verify(userService).findById(1L);
        verify(repository).save(any(LostItem.class));
        assertThat(result).isNotNull();
        assertThat(input.getUser()).isEqualTo(sampleUser);
        // contact 为空 → 默认取发布者手机号
        assertThat(input.getContact()).isEqualTo("13800001111");
    }

    @Test
    void create_shouldKeepContactWhenProvided() {
        LostItem input = new LostItem();
        input.setTitle("测试物品");
        input.setImageUrl("https://example.com/test.jpg");
        input.setContact("13900002222");

        when(userService.findById(1L)).thenReturn(sampleUser);
        when(repository.save(any(LostItem.class))).thenReturn(sampleItem);

        service.create(input, 1L);

        assertThat(input.getContact()).isEqualTo("13900002222");
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        LostItem input = new LostItem();
        input.setTitle("测试物品");
        input.setImageUrl("https://example.com/test.jpg");

        when(userService.findById(999L))
                .thenThrow(new RuntimeException("用户不存在，id: 999"));

        assertThatThrownBy(() -> service.create(input, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    void findById_shouldReturnItemWhenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));

        LostItem result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("黑色钱包");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("失物信息不存在");
    }

    @Test
    void findAll_shouldSearchByTitleWhenTitleProvided() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<LostItem> page = new PageImpl<>(List.of(sampleItem));
        when(repository.findByTitleContaining("钱包", pageable)).thenReturn(page);

        Page<LostItem> result = service.findAll("钱包", null, pageable);

        verify(repository).findByTitleContaining("钱包", pageable);
        verify(repository, never()).findAll(any(PageRequest.class));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_shouldFindAllWhenTitleIsEmpty() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<LostItem> page = new PageImpl<>(List.of(sampleItem));
        when(repository.findAll(pageable)).thenReturn(page);

        Page<LostItem> result = service.findAll("", null, pageable);

        verify(repository).findAll(pageable);
        verify(repository, never()).findByTitleContaining(anyString(), any());
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_shouldFilterByUserIdWhenUserIdProvided() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<LostItem> page = new PageImpl<>(List.of(sampleItem));
        when(repository.findByUserId(1L, pageable)).thenReturn(page);

        Page<LostItem> result = service.findAll(null, 1L, pageable);

        verify(repository).findByUserId(1L, pageable);
        verify(repository, never()).findAll(any(PageRequest.class));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_shouldFilterByUserIdAndTitleWhenBothProvided() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<LostItem> page = new PageImpl<>(List.of(sampleItem));
        when(repository.findByUserIdAndTitleContaining(1L, "钱包", pageable)).thenReturn(page);

        Page<LostItem> result = service.findAll("钱包", 1L, pageable);

        verify(repository).findByUserIdAndTitleContaining(1L, "钱包", pageable);
        verify(repository, never()).findAll(any(PageRequest.class));
        assertThat(result.getContent()).hasSize(1);
    }
}
