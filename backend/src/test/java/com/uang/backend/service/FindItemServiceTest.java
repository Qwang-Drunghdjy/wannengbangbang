package com.uang.backend.service;

import com.uang.backend.entity.FindItem;
import com.uang.backend.entity.User;
import com.uang.backend.repository.FindItemRepository;
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
class FindItemServiceTest {

    @Mock
    private FindItemRepository repository;

    @Mock
    private UserService userService;

    private FindItemService service;

    private FindItem sampleItem;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        service = new FindItemService(repository, userService);
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setPhone("13800001111");
        sampleUser.setNickname("张三");
        sampleUser.setStatus(User.Status.NORMAL);
        sampleUser.setCreateTime(LocalDateTime.now());
        sampleItem = new FindItem();
        sampleItem.setId(1L);
        sampleItem.setTitle("黑色钱包");
        sampleItem.setDescription("在食堂门口丢失");
        sampleItem.setLocation("食堂");
        sampleItem.setContact("13800001111");
        sampleItem.setImageUrl("https://example.com/img.jpg");
        sampleItem.setCreateTime(LocalDateTime.now());
    }

    @Test
    void create_shouldSetIdNullSetCreateTimeAndAssociateUser() {
        FindItem input = new FindItem();
        input.setId(999L);
        input.setTitle("测试物品");

        when(userService.findById(1L)).thenReturn(sampleUser);
        when(repository.save(any(FindItem.class))).thenReturn(sampleItem);

        FindItem result = service.create(input, 1L);

        verify(userService).findById(1L);
        verify(repository).save(any(FindItem.class));
        assertThat(result).isNotNull();
        assertThat(input.getUser()).isEqualTo(sampleUser);
        // contact 为空 → 默认取发布者手机号
        assertThat(input.getContact()).isEqualTo("13800001111");
    }

    @Test
    void create_shouldKeepContactWhenProvided() {
        FindItem input = new FindItem();
        input.setTitle("测试物品");
        input.setContact("13900002222");

        when(userService.findById(1L)).thenReturn(sampleUser);
        when(repository.save(any(FindItem.class))).thenReturn(sampleItem);

        service.create(input, 1L);

        assertThat(input.getContact()).isEqualTo("13900002222");
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        FindItem input = new FindItem();
        input.setTitle("测试物品");

        when(userService.findById(999L))
                .thenThrow(new RuntimeException("用户不存在，id: 999"));

        assertThatThrownBy(() -> service.create(input, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    void findById_shouldReturnItemWhenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));

        FindItem result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("黑色钱包");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("寻物信息不存在");
    }

    @Test
    void findAll_shouldSearchByTitleWhenTitleProvided() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<FindItem> page = new PageImpl<>(List.of(sampleItem));
        when(repository.findByTitleContaining("钱包", pageable)).thenReturn(page);

        Page<FindItem> result = service.findAll("钱包", pageable);

        verify(repository).findByTitleContaining("钱包", pageable);
        verify(repository, never()).findAll(any(PageRequest.class));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_shouldFindAllWhenTitleIsEmpty() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<FindItem> page = new PageImpl<>(List.of(sampleItem));
        when(repository.findAll(pageable)).thenReturn(page);

        Page<FindItem> result = service.findAll("", pageable);

        verify(repository).findAll(pageable);
        verify(repository, never()).findByTitleContaining(anyString(), any());
        assertThat(result.getContent()).hasSize(1);
    }
}
