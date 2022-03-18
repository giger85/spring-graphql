/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.graphql.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.graphql.GraphQlResponseError;
import org.springframework.graphql.GraphQlResponseField;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;


/**
 * Default implementation of {@link ClientGraphQlResponseField} that wraps the
 * field from {@link org.springframework.graphql.GraphQlResponse} and adds
 * support for decoding.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
final class DefaultClientGraphQlResponseField implements ClientGraphQlResponseField {

	private final DefaultClientGraphQlResponse response;

	private final GraphQlResponseField field;


	DefaultClientGraphQlResponseField(DefaultClientGraphQlResponse response, GraphQlResponseField field) {
		this.response = response;
		this.field = field;
	}


	@Override
	public boolean hasValue() {
		return this.field.hasValue();
	}

	@Override
	public String getPath() {
		return this.field.getPath();
	}

	@Override
	public List<Object> getParsedPath() {
		return this.field.getParsedPath();
	}

	@Override
	public <T> T getValue() {
		return this.field.getValue();
	}

	@Override
	public GraphQlResponseError getError() {
		return this.field.getError();
	}

	@Override
	public List<GraphQlResponseError> getErrors() {
		return this.field.getErrors();
	}

	@Override
	public <D> D toEntity(Class<D> entityType) {
		return toEntity(ResolvableType.forType(entityType));
	}

	@Override
	public <D> D toEntity(ParameterizedTypeReference<D> entityType) {
		return toEntity(ResolvableType.forType(entityType));
	}

	@Override
	public <D> List<D> toEntityList(Class<D> elementType) {
		return toEntity(ResolvableType.forClassWithGenerics(List.class, elementType));
	}

	@Override
	public <D> List<D> toEntityList(ParameterizedTypeReference<D> elementType) {
		return toEntity(ResolvableType.forClassWithGenerics(List.class, ResolvableType.forType(elementType)));
	}

	@SuppressWarnings({"unchecked", "ConstantConditions"})
	private <T> T toEntity(ResolvableType targetType) {
		if (!hasValue()) {
			throw new FieldAccessException(this.response, this);
		}

		DataBufferFactory bufferFactory = DefaultDataBufferFactory.sharedInstance;
		MimeType mimeType = MimeTypeUtils.APPLICATION_JSON;
		Map<String, Object> hints = Collections.emptyMap();

		DataBuffer buffer = ((Encoder<T>) this.response.getEncoder()).encodeValue(
				(T) getValue(), bufferFactory, ResolvableType.forInstance(getValue()), mimeType, hints);

		return ((Decoder<T>) this.response.getDecoder()).decode(buffer, targetType, mimeType, hints);
	}

}
