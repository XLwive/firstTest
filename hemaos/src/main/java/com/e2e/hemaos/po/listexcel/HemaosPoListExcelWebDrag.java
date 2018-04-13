package com.e2e.hemaos.po.listexcel;

import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.e2e.epd.plugin.AbstractWebDrag;
import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component("HemaosPoListexcelWebDrag")
public class HemaosPoListExcelWebDrag extends AbstractWebDrag {

	@Override
	public List<Page> drag(WebClient webClient, Map<String, Object> context, Map<String, String> params) {
		List<Page> pages = new ArrayList<>();
		String totalSize = "1000";
		try {
			URL url = new URL("https://scm-purchase.hemaos.com/purchase/order/supplier/select?_input_charset=utf-8");
			WebRequest request = new WebRequest(url);
			
			Map<String, String> requesParams = new HashMap<String, String>();
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			String beginDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
			String endDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			requesParams.put("status", "3");
			requesParams.put("excelWithAllocate", "true");
			requesParams.put("gmtArrivalStart", beginDate);
			requesParams.put("gmtArrivalEnd", endDate);
			requesParams.put("currentPage", "1");
			requesParams.put("pageSize", totalSize);
			requesParams.put("Content-Type", "application/json");
			request.setRequestBody(JSONObject.fromObject(requesParams).toString());
			
			request.setAdditionalHeader("Referer",
					"https://portal.hemaos.com/pages/supplierPlatformNew/purchaseList.html");
			request.setAdditionalHeader("Host","scm-purchase.hemaos.com");
			request.setAdditionalHeader("Accept","application/json, text/javascript");
			request.setAdditionalHeader("Content-Type","application/json");
			request.setHttpMethod(HttpMethod.POST);
			request.setCharset(Charset.forName("UTF-8"));
			request.setEncodingType(FormEncodingType.URL_ENCODED);
			UnexpectedPage p = webClient.getPage(webClient.getCurrentWindow(), request);
			
			String jsonStr = p.getWebResponse().getContentAsString();
			JSONObject jsonObject = JSONObject.fromObject(jsonStr);
			JSONArray jsonarray = jsonObject.getJSONArray("data");
			Iterator<?> itera = jsonarray.iterator();
			StringBuffer sb = new StringBuffer();
			String val;
			while(itera.hasNext()){
				Object obj = itera.next();
				if (obj == null){
					continue;
				}
				JSONObject data = (JSONObject) obj;
				sb.append(data.getString("purchaseOrderNo")+ ",");
			}
			
			int endIndex = sb.length()-1;
			if (sb.charAt(endIndex) == ','){
				val = sb.substring(0, endIndex);
			}else{
				val = sb.toString();
			}
			StringBuffer exportExcelUrl = new StringBuffer("https://scm-purchase.hemaos.com/purchase/order/supplier/exportExcel");
			exportExcelUrl.append("?purchaseOrderNos=")
			.append(val)
			.append("&excelWithAllocate=true");
			UnexpectedPage excelPage = webClient.getPage(exportExcelUrl.toString());
			pages.add(excelPage);
		}catch (Throwable e) {
			logger.error("PO抓取失败!!", e);
		}
		return pages;
	}

	@Override
	public String getFileSuffix() {
		return "xlsx";
	}

}
