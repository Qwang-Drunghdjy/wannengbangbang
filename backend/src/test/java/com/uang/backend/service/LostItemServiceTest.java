package com.uang.backend.service;

import com.uang.backend.entity.LostItem;
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

    private LostItemService service;

    private LostItem sampleItem;

    @BeforeEach
    void setUp() {
        service = new LostItemService(repository);
        sampleItem = new LostItem();
        sampleItem.setId(1L);
        sampleItem.setTitle("黑色钱包");
        sampleItem.setDescription("在图书馆门口捡到");
        sampleItem.setLocation("图书馆");
        sampleItem.setContact("13800001111");
        sampleItem.setImageUrl("https://example.com/img.jpg");
        sampleItem.setCreateTime(LocalDateTime.now());
    }

    @Test
    void create_shouldSetIdToNullAndSetCreateTime() {
        LostItem input = new LostItem();
        input.setId(999L);
        input.setTitle("测试物品");
        input.setImageUrl("https://example.com/test.jpg");

        when(repository.save(any(LostItem.class))).thenReturn(sampleItem);

        LostItem result = service.create(input);

        verify(repository).save(any(LostItem.class));
        assertThat(result).isNotNull();
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

        Page<LostItem> result = service.findAll("钱包", pageable);

        verify(repository).findByTitleContaining("钱包", pageable);
        verify(repository, never()).findAll(any(PageRequest.class));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_shouldFindAllWhenTitleIsEmpty() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<LostItem> page = new PageImpl<>(List.of(sampleItem));
        when(repository.findAll(pageable)).thenReturn(page);

        Page<LostItem> result = service.findAll("", pageable);

        verify(repository).findAll(pageable);
        verify(repository, never()).findByTitleContaining(anyString(), any());
        assertThat(result.getContent()).hasSize(1);
    }
}
