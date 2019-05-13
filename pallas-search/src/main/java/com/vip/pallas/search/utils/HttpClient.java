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

package com.vip.pallas.search.utils;

import java.io.IOException;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpClient {
	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

	public static String httpDelete(String urlStr) throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			HttpDelete httpDelete = new HttpDelete(urlStr);
			CloseableHttpResponse httpResponse = httpClient.execute(httpDelete);
			return getResponseBody(httpResponse, urlStr);
		} catch (IOException e) {
			LogUtils.error(logger, SearchLogEvent.IO_EVENT, e.getClass() + " " + e.getMessage(), e);
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
				LogUtils.error(logger, SearchLogEvent.IO_EVENT, e.getClass() + " " + e.getMessage(), e);
            }
        }
	}
	
	public static String httpPost(String urlStr, String content) throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
			HttpPost httpPost = new HttpPost(urlStr);
			httpPost.setHeader("Accept", "application/json");

			if (content != null) {
				httpPost.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
			}
        	
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            return getResponseBody(httpResponse, urlStr);
        } catch (IOException e) {
			LogUtils.error(logger, SearchLogEvent.IO_EVENT, e.getClass() + " " + e.getMessage(), e);
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
				LogUtils.error(logger, SearchLogEvent.IO_EVENT, e.getClass() + " " + e.getMessage(), e);
            }
        }
	}

	public static String httpGet(String urlStr) throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
			HttpGet httpGet = new HttpGet(urlStr);
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if(entity != null) {
            	String responseBody = EntityUtils.toString(httpResponse.getEntity(), Consts.UTF_8);
                StatusLine statusLine = httpResponse.getStatusLine();
                if(statusLine != null && statusLine.getStatusCode() >= 200 && statusLine.getStatusCode() < 300) {
                	return responseBody;
                } else {
                	throw new Exception(statusLine + " " + responseBody);
                }
            } else {
            	throw new Exception("httpGet error");
            }
        } catch (IOException e) {
			LogUtils.error(logger, SearchLogEvent.IO_EVENT, e.getClass() + " " + e.getMessage(), e);
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
				LogUtils.error(logger, SearchLogEvent.IO_EVENT, e.getClass() + " " + e.getMessage(), e);
            }
        }
	}

	private static String getResponseBody(CloseableHttpResponse httpResponse, String urlStr) throws Exception {
        HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			String responseBody = EntityUtils.toString(httpResponse.getEntity(), Consts.UTF_8);
			StatusLine statusLine = httpResponse.getStatusLine();
			if (statusLine != null && statusLine.getStatusCode() >= 200 && statusLine.getStatusCode() < 300) {
				return responseBody;
			} else {
				throw new Exception(statusLine + " " + responseBody);
			}
        } else {
            StatusLine statusLine = httpResponse.getStatusLine();
            if(statusLine != null && statusLine.getStatusCode() >= 200 && statusLine.getStatusCode() < 300) {
                return null;
            } else {
                throw new Exception("fail to access url: " + urlStr + " with statusCode: " + statusLine);
            }
        }
    }
}
