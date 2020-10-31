package org.xlp.db.tableoption.key;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xlp.db.exception.EntityException;
import org.xlp.db.sql.SQLUtil;
import org.xlp.db.tableoption.handlers.ScalarHandle;
import org.xlp.db.utils.BeanUtil;
import org.xlp.db.utils.Constants;

/**
 * 主键信息抽象类
 * <p>
 * 注意：这个类的子类的构造方法中应该先调用父类的构造方法，在执行别的内容，否则可能会抛出异常
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-24
 *         </p>
 * @version 1.0
 * 
 */
public abstract class KeyAbstract {
	// 日志记录
	protected final static Logger LOGGER = Logger.getLogger(KeyAbstract.class);
	// bean类型
	protected Class<?> beanClass;
	// 是否去数据库中获取当前可用值
	protected boolean isToObtainCurrentKeyValue;

	/**
	 * 存储最后所用的主键值
	 */
	private static final Map<String, Long> lastPkMap = new HashMap<String, Long>();
	static{
		lastPkMap.clear();
	}
	
	/**
	 * 用bean对象构建此对象
	 * 
	 * @param beanClass
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> KeyAbstract(T bean) throws EntityException {
		this(bean, true);
	}

	/**
	 * 用bean对象和isToObtainCurrentKeyValue构建此对象
	 * 
	 * @param bean
	 * @param isToObtainCurrentKeyValue
	 *            是否去数据库中获取当前可用值
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> KeyAbstract(T bean, boolean isToObtainCurrentKeyValue)
			throws EntityException {
		if (bean == null)
			throw new NullPointerException("bean对象必须不为空");
		this.beanClass = bean.getClass();
		if (!BeanUtil.isEntity(beanClass)) {
			LOGGER.error(beanClass.getName() + ": 没有XLPEntity实体注解");
			throw new EntityException("此对象不是实体");
		}
		this.isToObtainCurrentKeyValue = isToObtainCurrentKeyValue;
		init(bean);
	}

	/**
	 * 初始化数据
	 * 
	 * @param bean
	 */
	protected abstract <T> void init(T bean) throws EntityException;

	/**
	 * 得到当前主键自动增长的值(对于单个主键)
	 * 
	 * @param name
	 *            主键名称
	 * @return 返回当前可用的主键值
	 * @throws EntityException
	 */
	protected <T> long getCurrentKeyNumber(String name) throws EntityException {
		synchronized (beanClass) {
			String mapKey = beanClass.toString() + name;
			Long curPk = lastPkMap.get(mapKey);
			if (curPk == null ) {
				String sql = SQLUtil.max(beanClass, name);

				Number number = null;
				try {
					number = (Number) Constants.BASE_DB_OPTION.query(sql,
							new ScalarHandle());
				} catch (SQLException e) {
					throw new EntityException("主键生成失败",e);
				}
				long kv = number == null ? 0 : number.longValue();
				curPk = Long.valueOf(kv);
			}
			curPk = Long.valueOf(curPk.longValue() + 1);
			lastPkMap.put(mapKey, curPk);
			return curPk.longValue();
		}
	}
}
