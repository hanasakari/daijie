package org.daijie.social.login.weixin.service;

import org.daijie.core.util.http.HttpConversationUtil;
import org.daijie.social.login.AbstractLoginService;
import org.daijie.social.login.LoginResult;
import org.daijie.social.login.weixin.WeixinLoginConstants;
import org.daijie.social.login.weixin.WeixinLoignProperties;
import org.daijie.social.login.weixin.model.WeixinAccessToken;
import org.daijie.social.login.weixin.model.WeixinError;
import org.daijie.social.login.weixin.model.WeixinUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * 微信登录服务
 * @author daijie_jay
 * @since 2017年11月28日
 */
@Service
public class WeixinLoginService extends AbstractLoginService<WeixinLoignProperties> {
	
	private static final Logger logger = LoggerFactory.getLogger(WeixinLoginService.class);
	
	public LoginResult getAccessToken(String code) {
		StringBuilder uri = new StringBuilder();
		uri.append(WeixinLoginConstants.HOST_API + WeixinLoginConstants.ACCESS_TOKEN + "?appid=");
		uri.append(properties.getAppid());
		uri.append("&secret=").append(properties.getAppsecret());
		uri.append("&code=" + code).append("&grant_type=authorization_code");
		try {
			String result = restTemplate.getForObject(uri.toString(), String.class);
			JSONObject json = JSONUtil.parseObj(result);
			if(json.getStr("access_token") != null){
				WeixinAccessToken accessToken = new WeixinAccessToken();
				accessToken.setAccess_token(json.getStr("access_token"));
				accessToken.setOpenid(json.getStr("open_id"));
				accessToken.setExpires_in(json.getLong("expires_in"));
				accessToken.setRefresh_token(json.getStr("refresh_token"));
				accessToken.setScope(json.getStr("scope"));
				return accessToken;
			}else{
				WeixinError error = new WeixinError();
				error.setErrcode(json.getStr("errcode"));
				error.setErrmsg(json.getStr("errmsg"));
				return error;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public LoginResult refreshToken(String refresh_token) {
		StringBuilder uri = new StringBuilder();
		uri.append(WeixinLoginConstants.HOST_API + WeixinLoginConstants.REFRESH_TOKEN + "?appid=");
		uri.append(properties.getAppid());
		uri.append("&refresh_token=" + refresh_token).append("&grant_type=refresh_token");
		try {
			String result = restTemplate.getForObject(uri.toString(), String.class);
			JSONObject json = JSONUtil.parseObj(result);
			if(json.getStr("access_token") != null){
				WeixinAccessToken accessToken = new WeixinAccessToken();
				accessToken.setAccess_token(json.getStr("access_token"));
				accessToken.setOpenid(json.getStr("open_id"));
				accessToken.setExpires_in(json.getLong("expires_in"));
				accessToken.setRefresh_token(json.getStr("refresh_token"));
				accessToken.setScope(json.getStr("scope"));
				return accessToken;
			}else{
				WeixinError error = new WeixinError();
				error.setErrcode(json.getStr("errcode"));
				error.setErrmsg(json.getStr("errmsg"));
				return error;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public LoginResult verifyToken(String access_token) {
		StringBuilder uri = new StringBuilder();
		uri.append(WeixinLoginConstants.HOST_API + WeixinLoginConstants.VERIFY_AUTH + "?appid=");
		uri.append(properties.getAppid());
		uri.append("&access_token=" + access_token);
		try {
			String result = restTemplate.getForObject(uri.toString(), String.class);
			JSONObject json = JSONUtil.parseObj(result);
			WeixinError error = new WeixinError();
			error.setErrcode(json.getStr("errcode"));
			error.setErrmsg(json.getStr("errmsg"));
			return error;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public LoginResult getUserInfo(String access_token) {
		StringBuilder uri = new StringBuilder();
		uri.append(WeixinLoginConstants.HOST_API + WeixinLoginConstants.USER_INFO + "?appid=");
		uri.append(properties.getAppid());
		uri.append("&access_token=" + access_token);
		try {
			String result = restTemplate.getForObject(uri.toString(), String.class);
			JSONObject json = JSONUtil.parseObj(result);
			ObjectMapper mapper = new ObjectMapper();
			if(json.getStr("openid") != null){
				WeixinUserInfo userInfo = mapper.readValue(result, WeixinUserInfo.class);
				return userInfo;
			}else{
				WeixinError error = mapper.readValue(result, WeixinError.class);
				return error;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public String loadQrcode(String state) {
		String callback = WeixinLoginConstants.LOGIN_CALLBACK;
		if(!StringUtils.isEmpty(properties.getCallbackUri())){
			callback = properties.getCallbackUri();
		}
		if(!callback.contains("http")){
			String serverName = HttpConversationUtil.getRequest().getServerName();
			callback = serverName + callback;
		}
		StringBuilder uri = new StringBuilder();
		uri.append(WeixinLoginConstants.HOST_OPEN + WeixinLoginConstants.QR_CONNECT + "?appid=");
		uri.append(properties.getAppid());
		uri.append("&redirect_uri=" + callback);
		uri.append("&response_type=code&scope=snsapi_login");
		uri.append("&state=" + state);
		return REDIRECT + uri.toString();
	}

	@Override
	public String loadAuthPage(String state) {
		return null;
	}
}
