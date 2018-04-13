package com.e2e.hemaos;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.e2e.epd.core.model.EpdAccount;
import com.e2e.epd.plugin.AbstractWebLogin;
import com.e2e.epd.utils.WebUtils;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import net.sf.json.JSONObject;

@Component("HemaosWebLogin")
public class HemaosLogin extends AbstractWebLogin {

	public boolean login(WebClient webClient, Map<String, Object> context, Map<String, String> params) {
		
		EpdAccount epdAccout = (EpdAccount) context.get(WebUtils.EPDACCOUNT);
		String username = epdAccout.getAccountName();
		String password = epdAccout.getAccountPassword();
		String[][] loginParams = { 
					{ "loginId", username }, 
					{ "password", password }, 
					{ "appKey", "23576226" },
					{ "lang", "zh_CN" } 
				};
		Map<String, String> header = new HashMap<>();
		header.put("Host", "login-openaccount.taobao.com");
		header.put("Connection", "keep-alive");
		header.put("Referer",
				"https://login-openaccount.taobao.com/login/mini_login.htm?lang=zh_CN&appKey=23576226&styleType=auto&notLoadSsoView=false&notKeepLogin=true&isMobile=false&addRegisterUrl=false&addResetPwdUrl=false&loginIdPlaceHolder=%E8%AF%B7%E8%BE%93%E5%85%A5%E7%94%A8%E6%88%B7%E5%90%8D%2F%E6%89%8B%E6%9C%BA%E5%8F%B7%2F%E9%82%AE%E7%AE%B1&loginButtonValue=%E7%99%BB%E5%BD%95&rnd=0.669325429299051");
		try {
			HtmlPage resultP = getPage(webClient, "https://login-openaccount.taobao.com/login/login.do?fromSite=6",
					HttpMethod.POST, loginParams, header, null, null);
			JSONObject jso = JSONObject.fromObject(resultP.asText());
			String token = "";
			token = jso.getJSONObject("content").getJSONObject("data").getString("token");
			header.clear();
			header.put("Host", "portal.hemaos.com");
			header.put("Referer", "https://portal.hemaos.com/login");
			UnexpectedPage pp = getPage(webClient,
					"https://portal.hemaos.com/account/viewNodes/notLogin?token=".concat(token), HttpMethod.GET, null, header,
					null, null);
			JSONObject jo = JSONObject.fromObject(pp.getWebResponse().getContentAsString());
			String data = jo.getString("data");
			JSONObject dataJson = JSONObject.fromObject(data.substring(1, data.length() - 1));
			String codeJsonStr = dataJson.getString("nodes");
			JSONObject codeJson = JSONObject.fromObject(codeJsonStr.substring(1, codeJsonStr.length() - 1));
			String code = codeJson.getString("code");
			String[][] loginParamss = { 
						{ "merchantCode", code }, 
						{ "from", "" }, 
						{ "token", token } 
					};
			Map<String, String> headers = new HashMap<>();
			headers.put("Content-Type", "application/x-www-form-urlencoded");
			getPage(webClient, "https://portal.hemaos.com/login?_input_charset=utf-8", HttpMethod.POST, loginParamss,
					headers, null, null);
		} catch (Throwable e) {
			logger.error("登陆失败", e);
			return false;
		}

		return isLogin(webClient, context, params);
	}

	public boolean isLogin(WebClient webClient, Map<String, Object> context, Map<String, String> params) {
		try {
			Page page = webClient.getPage("https://portal.hemaos.com/account/username");
			if (!(page instanceof UnexpectedPage)) {
				return false;
			}
			UnexpectedPage up = (UnexpectedPage) page;
			JSONObject jo = JSONObject.fromObject(up.getWebResponse().getContentAsString());
			String status = jo.getString("success");
			String msg = jo.getString("message");
			String errCode = jo.getString("errorCode");
			if ("true".equalsIgnoreCase(status) && msg.contains("ok") && "null".equalsIgnoreCase(errCode)) {
				return true;
			}
		} catch (Throwable e) {
			logger.error("登陆失败", e);
		}
		return false;
	}
}
