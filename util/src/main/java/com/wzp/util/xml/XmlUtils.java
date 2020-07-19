package com.wzp.util.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * 一些处理XML的工具
 */
public class XmlUtils {
	
	public static final int HIGHEST_SPECIAL = '>';
    public static char[][] specialCharactersRepresentation = new char[HIGHEST_SPECIAL + 1][];
    static {
        specialCharactersRepresentation['&'] = "&amp;".toCharArray();
        specialCharactersRepresentation['<'] = "&lt;".toCharArray();
        specialCharactersRepresentation['>'] = "&gt;".toCharArray();
        specialCharactersRepresentation['"'] = "&#034;".toCharArray();
        specialCharactersRepresentation['\''] = "&#039;".toCharArray();
    }

    /**
	 * 创建Document
	 * @param input
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document document(InputStream input) throws SAXException, IOException {
		return documentBuilder().parse(new InputSource(input));
	}
	
	/**
	 * 创建Document
	 * @param reader
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document document(Reader reader) throws SAXException, IOException {
		return documentBuilder().parse(new InputSource(reader));
	}
	
	/**
     * 创建Document
     * @param xml 字符串
     * @return
     * @throws SAXException
     */
	public static Document document(String xml) throws SAXException {
		try {
			return document(new StringReader(xml));
		} catch (IOException e) {
			// 从字符串读取xml不应该有IOException
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * 创建DocumentBuilder
	 * @return
	 */
	public static DocumentBuilder documentBuilder() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// 使用默认配置，不应该有此异常
			throw new IllegalStateException(e);
		}
	}
	
	/**
     * 转义XML
     * @param value
     * @return
     */
    public static String escapeXml(String value) {

		StringBuilder sb = new StringBuilder();
		char[] buffer = value.toCharArray();

		for (int i = 0; i < buffer.length; i++) {
			char c = buffer[i];
			if (c <= HIGHEST_SPECIAL) {
				char[] escaped = specialCharactersRepresentation[c];
				if (escaped != null) {
					sb.append(escaped);
				} else {
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 使用xpath选择多个Node
	 * @param node
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	public static NodeList select(Node node, String xpath) throws XPathExpressionException {
		XPathExpression expr = xpath(xpath);
		return (NodeList)expr.evaluate(node, XPathConstants.NODESET);
	}
	
	/**
	 * 使用xpath选择1个Node
	 * @param node
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	public static Node selectOne(Node node, String xpath) throws XPathExpressionException {
		XPathExpression expr = xpath(xpath);
		return (Node)expr.evaluate(node, XPathConstants.NODE);
	}
    
    /**
	 * 创建XPathExpression
	 * @param expression
	 * @return
	 * @throws XPathExpressionException
	 */
	public static XPathExpression xpath(String expression) throws XPathExpressionException {
		return XPathFactory.newInstance().newXPath().compile(expression);
	}

}
