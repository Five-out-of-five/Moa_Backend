package com.fiveoutoffive.moa_backend.global.infrastructure.s3;

import java.util.List;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

/**
 * {@link CdnMappable}을 구현한 응답 DTO의 프로퍼티 중,
 * 이름이 {@code ~Url}/{@code ~Urls}로 끝나는 String / List&lt;String&gt; 필드에
 * {@link CdnUrlSerializer}를 자동으로 할당한다. ({@link NoCdnUrl} 필드는 제외)
 */
public class CdnUrlSerializerModifier extends ValueSerializerModifier {

    private final CdnUrlSerializer serializer = new CdnUrlSerializer();

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription.Supplier beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        if (!CdnMappable.class.isAssignableFrom(beanDesc.getBeanClass())) {
            return beanProperties;
        }

        for (BeanPropertyWriter writer : beanProperties) {
            if (writer.getAnnotation(NoCdnUrl.class) != null) {
                continue;
            }
            if (isUrlName(writer.getName()) && isStringOrStringCollection(writer.getType())) {
                writer.assignSerializer(serializer);
            }
        }
        return beanProperties;
    }

    private boolean isUrlName(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith("url") || lower.endsWith("urls");
    }

    private boolean isStringOrStringCollection(JavaType type) {
        if (type.getRawClass() == String.class) {
            return true;
        }
        return type.isCollectionLikeType()
                && type.getContentType() != null
                && type.getContentType().getRawClass() == String.class;
    }
}
