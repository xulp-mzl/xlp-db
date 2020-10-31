package org.xlp.db.xml;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xlp.db.sql.SQL;
import org.xlp.db.sql.SQLUtil;
import org.xlp.db.utils.BeanUtil;
import org.xlp.db.xml.ele.ParamEle;
import org.xlp.db.xml.ele.SQLEle;
import org.xlp.db.xml.ele.ParamConstants.Connector;
import org.xlp.db.xml.ele.ParamConstants.Flag;
import org.xlp.db.xml.ele.ParamConstants.Op;
import org.xlp.db.xml.ele.ParamConstants.Type;
import org.xlp.javabean.JavaBeanPropertiesDescriptor;
import org.xlp.utils.XLPBooleanUtil;
import org.xlp.utils.XLPStringUtil;

/**
 * 把xml中的sql语句转换成SQL对象
 * 
 * @author 徐龙平
 * 
 * @version 1.0
 */
public class XmlSQL implements SQL {
	private Class<?> entityClass;
	// 预处理值
	private Object[] params;
	// SQLEle对象
	private SQLEle sqlEle;
	// SQL语句
	private String sql;

	/**
	 * 当SQLEle中的ParamEle对象为null时，即在xml文件中没param元素时，使用该构造函数
	 * 
	 * @param sqlEle
	 *            SQLEle对象，该对象的应该用<code>SQLEleHelper</code>得到
	 * @param params
	 *            预处理参数
	 * @param cs
	 *            实体类型
	 * @throws NullPointerException
	 *             假如参数sqlEle为null，抛出该异常
	 */
	public XmlSQL(SQLEle sqlEle, Object[] params, Class<?> cs) {
		this.sqlEle = sqlEle;
		this.params = params;
		this.entityClass = cs;
		this.sql = this.sqlEle.getSqlContent();
	}

	/**
	 * 当SQLEle中的ParamEle对象为null时，即在xml文件中没param元素时，使用该构造函数
	 * 
	 * @param sqlEle
	 *            SQLEle对象，该对象的应该用<code>SQLEleHelper</code>得到
	 * @param params
	 *            预处理参数
	 * @throws NullPointerException
	 *             假如参数sqlEle为null，抛出该异常
	 */
	public XmlSQL(SQLEle sqlEle, Object[] params) {
		this(sqlEle, params, null);
	}

	/**
	 * 当SQLEle中的ParamEle对象为null时，即在xml文件中没param元素时，使用该构造函数
	 * 
	 * @param sqlEle
	 *            SQLEle对象，该对象的应该用<code>SQLEleHelper</code>得到
	 * @param params
	 *            预处理参数
	 * @param cs
	 *            实体类型
	 * @throws NullPointerException
	 *             假如参数sqlEle或params为null，抛出该异常
	 */
	public XmlSQL(SQLEle sqlEle, List<Object> params, Class<?> cs) {
		this(sqlEle, params.toArray(), cs);
	}

	/**
	 * 当SQLEle中的ParamEle对象为null时，即在xml文件中没param元素时，使用该构造函数
	 * 
	 * @param sqlEle
	 *            SQLEle对象，该对象的应该用<code>SQLEleHelper</code>得到
	 * @param params
	 *            预处理参数
	 * @throws NullPointerException
	 *             假如参数sqlEle或params为null，抛出该异常
	 */
	public XmlSQL(SQLEle sqlEle, List<Object> params) {
		this(sqlEle, params, null);
	}

	/**
	 * 当SQLEle中的ParamEle对象为null时，即在xml文件中没param元素时，使用该构造函数
	 * 
	 * @param sqlEle
	 *            SQLEle对象，该对象的应该用<code>SQLEleHelper</code>得到
	 * @throws NullPointerException
	 *             假如参数sqlEle为null，抛出该异常
	 */
	public XmlSQL(SQLEle sqlEle) {
		this(sqlEle, (Object[]) null, null);
	}

	/**
	 * 当SQLEle中的ParamEle对象为null时，即在xml文件中没param元素时，使用该构造函数
	 * 
	 * @param sqlEle
	 *            SQLEle对象，该对象的应该用<code>SQLEleHelper</code>得到
	 * @param cs
	 *            实体类型
	 * @throws NullPointerException
	 *             假如参数cs或sqlEle为null，抛出该异常
	 */
	public XmlSQL(SQLEle sqlEle, Class<?> cs) {
		this(sqlEle, (Object[]) null, cs);
	}

	/**
	 * 当SQLEle中的ParamEle对象不为null时，即在xml文件中有param元素时，使用该构造函数
	 * 
	 * @param sqlEle
	 *            SQLEle对象，该对象的应该用<code>SQLEleHelper</code>得到
	 * @param cs
	 *            实体类型
	 * @param map
	 *            map预处理参数(可为null)
	 * @param obj
	 *            对象预处理参数(可为null)
	 * @throws XMLSQLException
	 *             假如SQL语句解析出错，抛出该异常
	 * @throws NullPointerException
	 *             假如参数sqlEle为null，抛出该异常
	 */
	public <T> XmlSQL(SQLEle sqlEle, Class<?> cs, Map<String, Object> map, T obj)
			throws XMLSQLException {
		this.entityClass = cs;
		this.sqlEle = sqlEle;
		this.sql = this.sqlEle.getSqlContent();
		initParams(map, obj);
	}

	/**
	 * @param map
	 * @param obj
	 * @throws XMLSQLException
	 *             假如SQL语句解析出错，抛出该异常
	 */
	private <T> void initParams(Map<String, Object> map, T obj)
			throws XMLSQLException {
		List<ParamEle> paramEles = sqlEle.getParamEles();

		StringBuilder sb = null;
		List<Object> paramList = new ArrayList<Object>();

		for (ParamEle paramEle : paramEles) {
			boolean joint = paramEle.isJoint();
			// 判断是否拼接到已有的SQL语句后
			if (joint && !XLPStringUtil.isEmpty(paramEle.getCol())) {
				Op op = null;
				if ((op = paramEle.getOp()) == null)
					throw newXmlsqlException(sqlEle.getId(), "op");

				String opsString = op.getOp();

				String connectorString = null;
				Connector connector = paramEle.getConnector();
				connectorString = connector == null ? null : connector
						.getConnector();

				Object value = getValue(map, obj, paramEle);
				if (XLPStringUtil.isNullOrEmpty(value) && !paramEle.isTrim())
					continue;

				Class<?> valueClass = value == null ? null : value.getClass();
				// 判断value是否是数组或集合

				if (valueClass != null && !paramEle.isTrim()) {
					if (valueClass.isArray() && Array.getLength(value) == 0)
						continue;
					else if (Collection.class.isAssignableFrom(valueClass)
							&& ((Collection<?>) value).isEmpty())
						continue;
				}

				/**
				 * 把param元素的一些属性拼接的SQL中
				 */
				sb = new StringBuilder(" ");
				if (!XLPStringUtil.isNullOrEmpty(connectorString))
					sb.append(connectorString).append(" ");
				sb.append(paramEle.getCol()).append(" ").append(opsString);

				Type type = paramEle.getType();
				if (op == Op.in || op == Op.not_in) {
					if (value == null) {
						paramList.add(value);
						sb.append(" (?)");
					} else {
						if (valueClass.isArray()) {
							int len = Array.getLength(value);
							sb.append(" (");
							for (int i = 0; i < len; i++) {
								if(i != 0)
									sb.append(",");
								sb.append("?");
								if (type == Type.bool) {
									paramList.add(XLPBooleanUtil.valueOf(Array.get(value, i)));
								} else {
									paramList.add(Array.get(value, i));
								}
							}
							sb.append(")");
						} else if (Collection.class
								.isAssignableFrom(valueClass)) {
							Collection<?> c = (Collection<?>) value;
							sb.append(" (");
							Iterator<?> it = c.iterator();
							boolean flag = false;
							while (it.hasNext()) {
								if (flag)
									sb.append(",");
								sb.append("?");
								if (type == Type.bool) {
									paramList.add(XLPBooleanUtil.valueOf(it
											.next()));
								} else {
									paramList.add(it.next());
								}
								flag = true;
							}
							sb.append(")");
						} else {
							if (type == Type.bool) {
								paramList.add(XLPBooleanUtil.valueOf(value));
							} else {
								paramList.add(value);
							}
							sb.append(" (?)");
						}
					}
				} else {
					if (opsString.equals(LIKE) && type == Type.string) {
						paramList.add("%"
								+ XLPStringUtil.nullToEmpty((String) value)
									.replace("%", "\\%").replace("_", "\\_")
								+ "%");
					} else if (type == Type.bool) {
						paramList.add(XLPBooleanUtil.valueOf(value));
					} else {
						paramList.add(value);
					}
					sb.append(" ?");
				}

				this.sql += sb.toString();
			} else {
				initEachParam(map, obj, paramList, paramEle);
			}
		}
		params = paramList.toArray();
	}

	/**
	 * 初始化每个预处理参数值
	 * 
	 * @param map
	 *            map预处理参数(可为null)
	 * @param obj
	 *            对象预处理参数(可为null)
	 * @param paramList
	 *            参数集合
	 * @param paramEle
	 *            <code>ParamEle</code>对象
	 * @throws XMLSQLException
	 *             假如SQL语句解析出错，抛出该异常
	 */
	private <T> void initEachParam(Map<String, Object> map, T obj,
			List<Object> paramList, ParamEle paramEle) throws XMLSQLException {
		Type type = paramEle.getType();
		Object value = getValue(map, obj, paramEle);

		if (type == Type.bool) {
			paramList.add(XLPBooleanUtil.valueOf(value));
		} else {
			paramList.add(value);
		}
	}

	/**
	 * 获取值
	 * 
	 * @param map
	 *            map预处理参数(可为null)
	 * @param obj
	 *            对象预处理参数(可为null)
	 * @param paramEle
	 *            <code>ParamEle</code>对象
	 * @throws XMLSQLException
	 *             假如SQL语句解析出错，抛出该异常
	 */
	@SuppressWarnings("unchecked")
	private <T> Object getValue(Map<String, Object> map, T obj,
			ParamEle paramEle) throws XMLSQLException {
		Flag flag = paramEle.getFlag();
		if (flag == null)
			throw newXmlsqlException(sqlEle.getId(), "flag");

		String fn = paramEle.getFn();
		if (XLPStringUtil.isEmpty(fn))
			throw newXmlsqlException(sqlEle.getId(), "fn");

		Object value = null;
		if (flag == Flag.map) {
			if (map == null)
				throw new XMLSQLException("参数map值必须不为null");
			value = map.get(fn);
		} else {
			if (obj == null)
				throw new XMLSQLException("参数obj值必须不为null");
			String[] fns = fn.split("\\.");
			for (String fn1 : fns) {
				value = BeanUtil.callGetter(
						obj,
						new JavaBeanPropertiesDescriptor<T>((Class<T>) obj
								.getClass()).getDescriptor(fn1));
				if (value == null)
					break;
			}

		}

		String f_v = paramEle.getDefaultV();
		// 判断是否使用默认值
		if (XLPStringUtil.isNullOrEmpty(value) && f_v != null)
			value = f_v;
		return value;
	}

	/**
	 * 当SQLEle中的ParamEle对象不为null时，即在xml文件中有param元素时，使用该构造函数
	 * 
	 * @param sqlEle
	 *            SQLEle对象，该对象的应该用<code>SQLEleHelper</code>得到
	 * @param map
	 *            map预处理参数(可为null)
	 * @param obj
	 *            对象预处理参数(可为null)
	 * @throws XMLSQLException
	 *             假如SQL语句解析出错，抛出该异常
	 * @throws NullPointerException
	 *             假如参数sqlEle为null，抛出该异常
	 */
	public <T> XmlSQL(SQLEle sqlEle, Map<String, Object> map, T obj)
			throws XMLSQLException {
		this(sqlEle, null, map, obj);
	}

	@Override
	public Class<?> getEntityClass() {
		return entityClass;
	}

	@Override
	public String getParamSql() {
		String gb = sqlEle.getGroupBy();
		sql = gb == null ? sql : sql + " " + gb;
		String ob = sqlEle.getOrderBy();
		sql = ob == null ? sql : sql + " " + ob;

		return sql;
	}

	@Override
	public Object[] getParams() {
		return params;
	}

	@Override
	public String getSql() {
		String sql = SQLUtil
				.fillWithParams(new StringBuilder(this.sql), params);
		String gb = sqlEle.getGroupBy();
		sql = gb == null ? sql : sql + " " + gb;
		String ob = sqlEle.getOrderBy();
		sql = ob == null ? sql : sql + " " + ob;

		return sql;
	}

	@Override
	public String toString() {
		return getSql();
	}

	private XMLSQLException newXmlsqlException(String sqlId, String param) {
		return new XMLSQLException("id为" + sqlId + "元素中的param元素的属性[" + param
				+ "]值不存在或为空");
	}
}
