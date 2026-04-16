package com.example;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * 测试基类
 * - MockitoExtension：提供 @Mock、@InjectMocks 支持
 * - MockitoSettings(strictness = Strictness.LENIENT)：允许未 stub 的调用返回默认值
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class TestBase {
}
