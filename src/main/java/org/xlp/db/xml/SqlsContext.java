package org.xlp.db.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlp.utils.io.XLPIOUtil;
import org.xlp.utils.net.XLPHttpRequestUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * sqls上下文
 * 
 * @author 徐龙平
 * 
 * @version 1.0	
 */
public class SqlsContext {
	private static final Logger LOGGER  = LoggerFactory.getLogger(SqlsContext.class);
	
	//xml默认文件名放在src下
	public static final String DEFAULT_FILE_NAME = "xlp_sql.xml";
	public volatile static Document document;
	//SAXReader解析器
	private static volatile SAXReader reader;
	//构造标记
	private static boolean isUseEmptyConstructor = true;
	
	public SqlsContext(){
		
	}
	
	public SqlsContext(File file) throws XMLSQLException {
		try {
			document = reader.read(file);
			sqlsMap.clear();
			initMap(document);
			isUseEmptyConstructor = false;
		} catch (DocumentException e) {
			throw new XMLSQLException("xml文件解析异常", e);
		}
	}
	
	public SqlsContext(InputStream in) throws XMLSQLException{
		try {
			document = reader.read(in);
			sqlsMap.clear();
			initMap(document);
			isUseEmptyConstructor = false;
		} catch (DocumentException e) {
			throw new XMLSQLException("xml文件解析异常", e);
		}finally{
			XLPIOUtil.closeInputStream(in);
		}
	}
	
	public SqlsContext(String fileName) throws XMLSQLException{
		//InputStream input = SqlsContext.class.getClassLoader().getResourceAsStream(fileName);
		this(SqlsContext.class.getClassLoader().getResourceAsStream(fileName));
	}
	
	/**
	 * 存储sqls元素
	 */
	static Map<String, Element> sqlsMap = new ConcurrentHashMap<String, Element>();
	
	static{
		//sqlsMap = new ConcurrentHashMap<String, Element>();
		reader = new SAXReader();
		
		InputStream input = null;
		try {
			//设置读取xml文件时不对约束文件进行验证
			reader.setEntityResolver(new  EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId, String systemId)
						throws SAXException, IOException {
					return new InputSource(new ByteArrayInputStream("".getBytes()));
				}
			});
			
			input = SqlsContext.class.getClassLoader().getResourceAsStream(DEFAULT_FILE_NAME);
			document = reader.read(input);
			sqlsMap.clear();
			initMap(document);
		} catch (DocumentException e) {
			LOGGER.warn(DEFAULT_FILE_NAME + "该文件不存在");
		}catch (Exception e) {
			LOGGER.warn("xml文件解析异常");
		}finally{
			XLPIOUtil.closeInputStream(input);
		}
	}
	
	/**
	 * 初始化map
	 */
	private static void initMap(Document document){
		 // 获取根元素
		Element rootElement = document.getRootElement();
		
		//处理sqls元素
		getSqlsEle(rootElement);
		
		//处理file元素
		getFileEle(rootElement);
	}

	/**
	 * 获取file元素，并对其进行解析
	 * 
	 * @param rootElement
	 */
	private static void getFileEle(Element rootElement) {
		// 获取特定名称的子元素集合（file）
		List<Element> fileList = rootElement.elements("file");
		//存储属性ref-file的值
		String ref_file = null;
		Document d1 = null;
		InputStream input = null;
		
		for (Element ele : fileList) {
			ref_file = ele.attributeValue("ref-file");
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("正在解析的数据源来自：" + ref_file);
			
			try {
				if(ref_file.startsWith("http://")){
					try {
						d1 = DocumentHelper.parseText(XLPHttpRequestUtil.httpRequest(ref_file,
								XLPHttpRequestUtil.GET, null));
					} catch (Exception e) {
						d1 = DocumentHelper.parseText(XLPHttpRequestUtil.httpRequest(ref_file,
								XLPHttpRequestUtil.POST, null));
					}
				
				} else if (ref_file.startsWith("https://")) {
					try {
						d1 = DocumentHelper.parseText(XLPHttpRequestUtil.httpsRequest(ref_file,
								XLPHttpRequestUtil.GET, null));
					} catch (Exception e) {
						d1 = DocumentHelper.parseText(XLPHttpRequestUtil.httpsRequest(ref_file,
								XLPHttpRequestUtil.POST, null));
					}
					
				}else if (ref_file.contains(":")) {
					d1 = reader.read(new File(ref_file));
				}else {
					char ch = ref_file.charAt(0);
					if(ch == '/' || ch == '\\')
						ref_file = ref_file.substring(1);
					input = SqlsContext.class.getClassLoader().getResourceAsStream(ref_file);
					d1 = reader.read(input);
				}
			} catch (Exception e) {
				LOGGER.warn(ref_file + "数据来源解析异常");
			}finally{
				XLPIOUtil.closeInputStream(input);
			}
			
			initMap(d1);
		}
	}

	/**
	 * 获取sqls元素，并储存到map中
	 * 
	 * @param rootElement
	 */
	private static void getSqlsEle(Element rootElement) {
		// 获取特定名称的子元素集合（sqls）
		List<Element> sqlsElements = rootElement.elements("sqls");
		//XLPOutputInfoUtil.println(sqlsElements);
		//属性id值
		String attrIdV = null;
		for (Element element : sqlsElements) {
			attrIdV = element.attributeValue("id");
			sqlsMap.put(attrIdV, element);
		}
	}
	
	/**
	 * 重置map值
	 */
	public static void reset() {
		sqlsMap.clear();
		if(isUseEmptyConstructor){
			InputStream input = null;
			try {
				input = SqlsContext.class.getClassLoader().getResourceAsStream(DEFAULT_FILE_NAME);
				document = reader.read(input);
			} catch (DocumentException e) {
				LOGGER.warn("重置失败");
			}
			XLPIOUtil.closeInputStream(input);
		}
		initMap(document);
	}
	
	/**
	 * 获取map值
	 * 
	 * @return
	 */
	public Map<String, Element> getSqlsMap(){
		return sqlsMap;
	}
}
