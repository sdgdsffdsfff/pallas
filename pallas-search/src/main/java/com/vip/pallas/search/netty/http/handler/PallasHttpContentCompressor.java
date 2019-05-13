/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.search.netty.http.handler;

import java.util.List;

import com.vip.pallas.search.utils.LogUtils;
import com.vip.pallas.search.utils.SearchLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.ReferenceCountUtil;

public class PallasHttpContentCompressor extends HttpContentCompressor {

	private static Logger logger = LoggerFactory.getLogger(PallasHttpContentCompressor.class);

	private int gzipMinLength = -1; // 默认为全部压缩

	public PallasHttpContentCompressor() {
		super();
	}

	public PallasHttpContentCompressor(int compressionLevel, int windowBits, int memLevel) {
		super(compressionLevel, windowBits, memLevel);
	}

	public PallasHttpContentCompressor(int compressionLevel) {
		super(compressionLevel);
	}

	public PallasHttpContentCompressor(int compressionLevel, int gzipMinLength) {
		super(compressionLevel);
		this.gzipMinLength = gzipMinLength;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
		try {
			super.encode(ctx, msg, out);
		} catch (IllegalStateException e) {
			LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, e.getMessage(), e);
			out.add(ReferenceCountUtil.retain(msg));
		}

	}

	@Override
	protected Result beginEncode(HttpResponse response, String acceptEncoding) throws Exception {
		if (acceptEncoding != null && acceptEncoding.toLowerCase().contains("lz4")) {
			return new Result("lz4", new EmbeddedChannel( new Lz4Encoder()));
		}
		
		if (gzipMinLength == -1) {
			return super.beginEncode(response, acceptEncoding);
		} else {
			String contentLengthStr = response.headers().get(Names.CONTENT_LENGTH);
			if (contentLengthStr != null) {
				int contentLength = Integer.parseInt(contentLengthStr);
				if (contentLength < gzipMinLength) {
					return null;
				}
			}
			return super.beginEncode(response, acceptEncoding);
		}

	}

}
