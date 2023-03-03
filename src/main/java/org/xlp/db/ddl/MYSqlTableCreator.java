package org.xlp.db.ddl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlp.assertion.AssertUtils;
import org.xlp.db.ddl.annotation.XLPCompoundIndex;
import org.xlp.db.ddl.annotation.XLPIndex;
import org.xlp.db.ddl.type.IndexType;
import org.xlp.db.tableoption.annotation.XLPColumn;
import org.xlp.db.tableoption.annotation.XLPEntity;
import org.xlp.db.tableoption.annotation.XLPId;
import org.xlp.db.tableoption.xlpenum.DataType;
import org.xlp.db.tableoption.xlpenum.PrimaryKeyDataType;
import org.xlp.db.tableoption.xlpenum.PrimaryKeyType;
import org.xlp.db.tableoption.xlpenum.TableType;
import org.xlp.db.utils.XLPDBUtil;
import org.xlp.javabean.JavaBeanPropertiesDescriptor;
import org.xlp.javabean.PropertyDescriptor;
import org.xlp.scanner.pkg.ClassPathPkgScanner;
import org.xlp.utils.XLPStringUtil;
import org.xlp.utils.collection.XLPCollectionUtil;

/**
 * <p>
 * 创建时间：2021年3月19日 下午9:57:46
 * </p>
 * 
 * @author xlp
 * @version 1.0
 * @Description 数据库表创建器
 */
public class MYSqlTableCreator implements TableCreator{
	private final static Logger LOGGER = LoggerFactory.getLogger(MYSqlTableCreator.class);

	/**
	 * 标记创建某个数据表时，是否跳过异常，继续创建接下来的表，true：跳过，false：抛出异常, 默认跳过异常
	 */
	private boolean isSkipCreateTableException = true;

	/**
	 * 未定义字段长度值
	 */
	public static final int NOT_DEFINE_LEN = -1;

	/**
	 * 默认特殊字符的包装字符串
	 */
	private static final String DEFAULT_WRAP_STR = "`";

	/**
	 * 特殊字符的包装字符串, 默认包装字符串是`
	 */
	private String warpStr = DEFAULT_WRAP_STR;
	
	/**
	 * 字符串类型字段默认长度
	 */
	private static final int CHAR_PRIMARY_KEY_DEFAULT_LENGTH = 64;

	/**
	 * 数据库连接
	 */
	private Connection connection;

	/**
	 * 存储需要创建数据库表的实体类
	 */
	private Set<Class<?>> entityClasses = new HashSet<>();

	/**
	 * 构造函数
	 * 
	 * @param connection
	 *            数据库连接
	 * @param entityClasses
	 *            需要创建数据库表的实体类集合
	 * @throws NullPointerException
	 *             假如参数为null，则抛出该异常
	 */
	public MYSqlTableCreator(Connection connection, Set<Class<?>> entityClasses) {
		this(connection);
		AssertUtils.isNotNull(entityClasses, "entityClasses parameter is null!");
		this.entityClasses = entityClasses;
	}

	/**
	 * 构造函数
	 * 
	 * @param connection
	 *            数据库连接
	 * @param packageNames
	 *            需要创建数据库表的实体类所在的包名称
	 * @throws NullPointerException
	 *             假如参数为空，则抛出该异常
	 * @throws TableCreateException
	 *             假如通过包名或是实体类失败，则抛出该异常
	 */
	public MYSqlTableCreator(Connection connection, String... packageNames) {
		this(connection);
		AssertUtils.isNotNull(packageNames, "packageNames parameter is null!");
		entityClasses = fromPackageNames(packageNames);
	}

	/**
	 * 构造函数
	 * 
	 * @param connection
	 *            数据库连接
	 * @throws NullPointerException
	 *             假如参数为null，则抛出该异常
	 */
	public MYSqlTableCreator(Connection connection) {
		AssertUtils.isNotNull(connection, "connection parameter is null!");
		this.connection = connection;
	}

	/**
	 * 通过给定的包名，获取该包名下的所有实体类
	 * 
	 * @param packageNames
	 * @return
	 * @throws TableCreateException
	 *             假如通过包名或是实体类失败，则抛出该异常
	 */
	protected Set<Class<?>> fromPackageNames(String[] packageNames) {
		Set<Class<?>> classes = new HashSet<>();
		for (String packageName : packageNames) {
			classes.addAll(fromPackageName(packageName));
		}
		return classes;
	}

	/**
	 * 获取给定包名下的所有实体类
	 * 
	 * @param packageName
	 * @return
	 * @throws TableCreateException
	 *             假如通过包名或是实体类失败，则抛出该异常
	 */
	private Set<Class<?>> fromPackageName(String packageName) {
		ClassPathPkgScanner classPathPkgScanner = new ClassPathPkgScanner();
		Set<Class<?>> result = new HashSet<>();
		Set<Class<?>> temp = null;
		try {
			temp = classPathPkgScanner.scannerToClass(packageName);
		} catch (IOException e) {
			throw new TableCreateException("通过包名或是实体类失败！", e);
		}
		for (Class<?> class1 : temp) {
			if (class1.getAnnotation(XLPEntity.class) != null) {
				result.add(class1);
			}
		}
		temp = null;
		return result;
	}

	/**
	 * @return 特殊字符的包装字符串
	 */
	public String getWarpStr() {
		return warpStr;
	}

	/**
	 * @param warpStr
	 *            特殊字符的包装字符串
	 */
	public void setWarpStr(String warpStr) {
		this.warpStr = warpStr == null ? XLPStringUtil.EMPTY : warpStr;
	}

	/**
	 * 创建表
	 * 
	 * @throws TableCreateException
	 *             假如创建表过程中出现错误，则抛出该异常
	 */
	@Override
	public void createTables() {
		boolean isWarn = LOGGER.isWarnEnabled();
		if (XLPCollectionUtil.isEmpty(entityClasses)) {
			if (isWarn) {
				LOGGER.warn("没有要创建的表！");
			}
			return;
		}

		boolean isInfo = LOGGER.isInfoEnabled();
		if (isInfo) {
			LOGGER.info("开始创建数据表。。。。");
		}
		boolean isDebug = LOGGER.isDebugEnabled();
		boolean isError = LOGGER.isErrorEnabled();
		String createTableSql = XLPStringUtil.EMPTY;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			for (Class<?> entityClass : entityClasses) {
				// 创建表格
				createTableSql = createTableSql(entityClass);
				if (XLPStringUtil.isEmpty(createTableSql))
					continue;
				if (isDebug) {
					LOGGER.debug("正在创建【\n" + createTableSql + "\n】的数据库表。。。");
				}
				if (isSkipCreateTableException) {
					try {
						statement.execute(createTableSql);
					} catch (Exception e) {
						if (isError) {
							LOGGER.error("创建statement失败，或创建数据库表【\n" + createTableSql + "\n】失败", e);
						}
					}
				} else {
					statement.execute(createTableSql);
				}
				if (isDebug) {
					LOGGER.debug("创建【\n" + createTableSql + "\n】的数据库表已完成");
				}
			}
		} catch (SQLException e) {
			throw new TableCreateException("创建statement失败，或创建数据库表【\n" + createTableSql + "\n】失败", e);
		} finally {
			try {
				XLPDBUtil.closeStatement(statement);
			} catch (SQLException e) {
				if (isError) {
					LOGGER.error("关闭statement失败！");
				}
			}
		}

		if (isInfo) {
			LOGGER.info("创建数据表完成。。。。");
		}
	}

	/**
	 * 通过实体对象创建数据库表
	 * 
	 * @param entity
	 *            实体对象
	 * @throws NullPointerException
	 *             假如参数为null，贼抛出该异常
	 * @throws TableCreateException
	 *             假如创建表过程中出现错误，则抛出该异常
	 */
	@Override
	public <T> void createTableByEntity(T entity) {
		AssertUtils.isNotNull(entity, "entity paramter is null!");
		createTableByEntityClass(entity.getClass());
	}

	/**
	 * 通过实体类创建数据库表
	 * 
	 * @param entityClass
	 *            实体类
	 * @throws NullPointerException
	 *             假如参数为null，贼抛出该异常
	 * @throws TableCreateException
	 *             假如创建表过程中出现错误，则抛出该异常
	 */
	@Override
	public <T> void createTableByEntityClass(Class<T> entityClass) {
		AssertUtils.isNotNull(entityClass, "entityClass paramter is null!");
		XLPEntity xlpEntity = entityClass.getAnnotation(XLPEntity.class);
		if (xlpEntity == null) {
			throw new TableCreateException("给定的对象不是实体类，缺失【XLPEntity】注解！");
		}
		String createTableSql = createTableSql(entityClass);
		if (!XLPStringUtil.isEmpty(createTableSql)) {
			Statement statement = null;
			try {
				statement = connection.createStatement();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("正在创建【\n" + createTableSql + "\n】的数据库表。。。");
				}
				statement.execute(createTableSql);
			} catch (SQLException e) {
				throw new TableCreateException("创建statement失败，或创建数据库表【\n" + createTableSql + "\n】失败", e);
			} finally {
				try {
					XLPDBUtil.closeStatement(statement);
				} catch (SQLException e) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("关闭statement失败！");
					}
				}
			}
		}
	}

	/**
	 * 根据实体类得到创建表的sql语句
	 * 
	 * @param entityClass
	 *            实体类
	 * @return 假如参数为null，则返回""
	 */
	protected String createTableSql(Class<?> entityClass) {
		if (entityClass == null) {
			return XLPStringUtil.EMPTY;
		}

		StringBuilder tableSql = new StringBuilder();
		XLPEntity xlpEntity = entityClass.getAnnotation(XLPEntity.class);
		if (xlpEntity == null) {
			return XLPStringUtil.EMPTY;
		}

		if (xlpEntity.tableType() != TableType.TABLE) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("该实体" + entityClass.getName() + "为视图，无需创建表！");
			}
			return XLPStringUtil.EMPTY;
		}

		String tableName = xlpEntity.tableName();

		// 形成table部分sql
		tableSql.append("CREATE TABLE ")
				// 给表的名称加上包装字符
				.append(wrap(tableName)).append("(");
		// 存储主键名称
		List<String> primaryKeys = new ArrayList<>();

		JavaBeanPropertiesDescriptor<?> javaBeanPropertiesDescriptor = new JavaBeanPropertiesDescriptor<>(entityClass);
		PropertyDescriptor<?>[] columns = javaBeanPropertiesDescriptor.getPds();
		// 处理成数据库表相应的字段
		XLPColumn xlpColumn;
		XLPId xlpId;
		String columnName = null;
		String columnSql;
		boolean isFirstColumn = true;

		// 存储索引列，key：列名， value：XLPIndex
		Map<String, XLPIndex> indexMap = new HashMap<String, XLPIndex>();

		for (PropertyDescriptor<?> propertyDescriptor : columns) {
			xlpColumn = propertyDescriptor.getFieldAnnotation(XLPColumn.class);
			if (xlpColumn == null) {
				xlpId = propertyDescriptor.getFieldAnnotation(XLPId.class);
				if (xlpId != null) {
					columnName = xlpId.columnName();
					columnName = XLPStringUtil.isEmpty(columnName) ? propertyDescriptor.getFieldName() : columnName;
					// 给表的列名加上包装字符
					columnName = wrap(columnName);
					primaryKeys.add(columnName);
					columnSql = createColumnSql(xlpId, columnName, xlpEntity);
				} else {
					columnSql = XLPStringUtil.EMPTY;
				}
			} else {
				columnName = xlpColumn.columnName();
				columnName = XLPStringUtil.isEmpty(columnName) ? propertyDescriptor.getFieldName() : columnName;
				// 给表的列名加上包装字符
				columnName = wrap(columnName);
				columnSql = createColumnSql(xlpColumn, columnName);
			}

			if (XLPStringUtil.isEmpty(columnSql))
				continue;

			// 拼接每列sql
			if (!isFirstColumn) {
				tableSql.append(",");
			}
			tableSql.append("\n").append(columnSql);
			isFirstColumn = false;

			// 处理索引列
			XLPIndex index = propertyDescriptor.getFieldAnnotation(XLPIndex.class);
			if (index != null) {
				indexMap.put(columnName, index);
			}
		}

		if (!primaryKeys.isEmpty()) {
			tableSql.append(",\n").append("PRIMARY KEY (")
					.append(XLPCollectionUtil.toString(primaryKeys, XLPStringUtil.EMPTY, ",", XLPStringUtil.EMPTY))
					.append(")");
		}

		// 单列索引
		for (Entry<String, XLPIndex> entry : indexMap.entrySet()) {
			tableSql.append(",\n").append(createIndexSql(entry.getKey(), entry.getValue()));
		}
		
		//组合索引
		XLPCompoundIndex compoundIndex = entityClass.getAnnotation(XLPCompoundIndex.class);
		if (compoundIndex != null) {
			appendCompoundIndexSql(compoundIndex, tableSql);
		}

		tableSql.append("\n)ENGINE=").append(xlpEntity.dbEngine().getDbEngineName()).append(" CHARSET=")
				.append(xlpEntity.chartsetName());
		String tableComment = xlpEntity.descriptor();
		if (!XLPStringUtil.isEmpty(tableComment)) {
			tableSql.append(" COMMENT='")
					// 转义特殊字符
					.append(transferredChar(tableComment)).append("'");
		}
		return tableSql.toString();
	}

	/**
	 * 拼接复合组件SQL片段
	 * 
	 * @param compoundIndex
	 * @param sb
	 */
	private void appendCompoundIndexSql(XLPCompoundIndex compoundIndex, StringBuilder sb) {
		String[] columns = compoundIndex.column();
		String[] names = compoundIndex.name();
		IndexType[] indexTypes = compoundIndex.indexType();
		int itypeLen = indexTypes.length, nameLen = names.length;
		for(int i = 0, len = columns.length; i < len; i++){
			sb.append(",\n");
			IndexType indexType = i < itypeLen ? indexTypes[i] : IndexType.NORMAL;
			switch (indexType) {
				case FULLTEXT:
					sb.append("FULLTEXT INDEX ");
					break;
				case NORMAL:
					sb.append("INDEX ");		
					break;
				case UNIQUE:
					sb.append("UNIQUE INDEX ");
					break;
				default:
					break;
			}
			String name = i < nameLen ? names[i] : XLPStringUtil.EMPTY; 
			if (!XLPStringUtil.isEmpty(name)) { 
				sb.append(wrap(name)).append(" ");
			}
			String[] _columns = columns[i].split(",");
			sb.append(XLPStringUtil.join(_columns, "(" + warpStr, warpStr + ")", ",", true, true));
		}
	}

	/**
	 * 创建索引列sql
	 * 
	 * @param columnName
	 *            数据表列名称(已被处理过， 无需在处理)
	 * @param xlpIndex
	 * @return
	 */
	private String createIndexSql(String columnName, XLPIndex xlpIndex) {
		StringBuilder sb = new StringBuilder();
		switch (xlpIndex.indexType()) {
			case FULLTEXT:
				sb.append("FULLTEXT INDEX ");
				break;
			case NORMAL:
				sb.append("INDEX ");		
				break;
			case UNIQUE:
				sb.append("UNIQUE INDEX ");
				break;
			default:
				break;
		}
		//索引名称
		String name = xlpIndex.name();
		if (!XLPStringUtil.isEmpty(name)) { 
			sb.append(wrap(name)).append(" ");
		}
		sb.append("(").append(columnName).append(")");
		return sb.toString();
	}

	/**
	 * 根据java XLPColumn注解创建数据库表字段sql
	 * 
	 * @param xlpColumn
	 * @param columnName
	 *            数据表列名称(已被处理过， 无需在处理)
	 * @return 假如参数为null，则返回""
	 */
	protected String createColumnSql(XLPColumn xlpColumn, String columnName) {
		StringBuilder sb = new StringBuilder(columnName).append(" ");
		if (xlpColumn != null) {
			DataType dataType = xlpColumn.dataType();
			int len = xlpColumn.length();
			int decimalLength = xlpColumn.decimalLength();
			boolean zeroFill = xlpColumn.zeroFill();
			boolean isNull = xlpColumn.isNull();
			String defaultValue = xlpColumn.defaultValue();
			String comment = xlpColumn.descriptor();
			addTypeSql(sb, dataType, len, decimalLength);
			addZeroFillSql(sb, dataType, zeroFill);
			// 追加字段是否不为空
			if (!isNull) {
				sb.append("NOT NULL ");
			}
			addDefaultValueSql(sb, dataType, defaultValue);
			// 判断字段描述是否为空，假如非空，追加字段描述
			if (!XLPStringUtil.isEmpty(comment)) {
				sb.append("COMMENT '").append(transferredChar(comment)).append("'");
			}
		}
		return sb.toString();
	}

	/**
	 * 添加字段默认值sql(暂时只支持数字类型和字符串类型)
	 * 
	 * @param sb
	 * @param dataType
	 * @param defaultValue
	 */
	private void addDefaultValueSql(StringBuilder sb, DataType dataType, String defaultValue) {
		switch (dataType) {
		case DECIMAL:
		case DOUBLE:
		case FLOAT:
		case NUMERIC:
		case BOOL:
		case BOOLEAN:
		case BIGINT:
		case INT:
		case SMALLINT:
		case TINYINT:
			if (!XLPStringUtil.NULL_STRING.equals(defaultValue)) {
				sb.append("DEFAULT ").append(defaultValue).append(" ");
			}
			break;
		case CHAR:
		case VARCHAR:
		case TEXT:
		case LONGTEXT:
		case MEDIUMTEXT:
		case TINYTEXT:
			if (!XLPStringUtil.NULL_STRING.equals(defaultValue)) {
				sb.append("DEFAULT '").append(transferredChar(defaultValue)).append("' ");
			}
			break;
		default:
			break;
		}

	}

	/**
	 * 添加字段是否填充0sql
	 * 
	 * @param sb
	 * @param dataType
	 * @param zeroFill
	 */
	private void addZeroFillSql(StringBuilder sb, DataType dataType, boolean zeroFill) {
		switch (dataType) {
		case DECIMAL:
		case DOUBLE:
		case FLOAT:
		case NUMERIC:
		case BOOL:
		case BOOLEAN:
		case BIGINT:
		case INT:
		case SMALLINT:
		case TINYINT:
			if (zeroFill) {
				sb.append("ZEROFILL ");
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 添加字段类型所需的sql
	 * 
	 * @param sb
	 * @param dataType
	 *            字段类型
	 * @param len
	 *            字段长度
	 * @param decimalLength
	 *            小数部分长度
	 * @return
	 */
	private void addTypeSql(StringBuilder sb, DataType dataType, int len, int decimalLength) {
		switch (dataType) {
		case DECIMAL:
		case DOUBLE:
		case FLOAT:
		case NUMERIC:
			sb.append(dataType.getDataTypeName()).append("(").append(len).append(",").append(decimalLength)
					.append(") ");
			break;
		case LONGBLOB:
		case LONGTEXT:
		case BOOL:
		case BOOLEAN:
		case MEDIUMBLOB:
		case MEDIUMTEXT:
		case TINYTEXT:
		case TINYBLOB:
			sb.append(dataType.getDataTypeName()).append(" ");
			break;
		default:
			sb.append(dataType.getDataTypeName());
			// 判断是否定义了字段长度
			if (len != NOT_DEFINE_LEN) {
				sb.append("(").append(len).append(")");
			}
			sb.append(" ");
			break;
		}
	}

	/**
	 * 根据java XLPId注解创建数据库表字段sql
	 * 
	 * @param xlpId
	 * @param columnName
	 *            数据表列名称(已被处理过， 无需在处理)
	 * @param entity
	 * 			  XLPEntity注解
	 * @return 假如参数为null，则返回""
	 */
	protected String createColumnSql(XLPId xlpId, String columnName, XLPEntity entity) {
		StringBuilder sb = new StringBuilder(columnName).append(" ");
		if (xlpId != null) {
			PrimaryKeyDataType pkDataType = xlpId.dataType();
			pkDataType = pkDataType == PrimaryKeyDataType.NONE ? entity.primaryKeyDataType() : pkDataType;
			
			int len = xlpId.length();
			len = len == NOT_DEFINE_LEN ? entity.primaryKeyLength() : len;
			
			//主键类型
			PrimaryKeyType keyType = xlpId.type();
			keyType = keyType == PrimaryKeyType.NONE ? entity.primaryKeyType() : keyType;
			if (keyType == PrimaryKeyType.AUTO) {
				if (pkDataType != PrimaryKeyDataType.BIGINT && pkDataType != PrimaryKeyDataType.INT) {
					pkDataType = PrimaryKeyDataType.BIGINT;
					len = NOT_DEFINE_LEN;
				}
			}
			
			DataType dataType;
			switch (pkDataType) {
				case BIGINT:
					dataType = DataType.BIGINT;
					break;
				case INT:
					dataType = DataType.INT;
					break;
				case CHAR:
					dataType = DataType.CHAR;
					len = len == NOT_DEFINE_LEN ? CHAR_PRIMARY_KEY_DEFAULT_LENGTH : len;
					break;
				case VARCHAR:
					dataType = DataType.VARCHAR;
					len = len == NOT_DEFINE_LEN ? CHAR_PRIMARY_KEY_DEFAULT_LENGTH : len;
					break;
				default:
					dataType = DataType.VARCHAR;
					len = len == NOT_DEFINE_LEN ? CHAR_PRIMARY_KEY_DEFAULT_LENGTH : len;
					break;
			}
			
			int decimalLength = 0;
			
			String comment = xlpId.descriptor();
			comment = XLPStringUtil.isEmpty(comment) ? entity.primaryKeyDescriptor() : comment;
			
			addTypeSql(sb, dataType, len, decimalLength);
			// 追加字段是否不为空
			sb.append("NOT NULL ");
			// 添加自增功能
			if (keyType == PrimaryKeyType.AUTO) {
				sb.append("auto_increment ");
			}
			
			// 判断字段描述是否为空，假如非空，追加字段描述
			if (!XLPStringUtil.isEmpty(comment)) {
				sb.append("COMMENT '").append(transferredChar(comment)).append("'");
			}
		}
		return sb.toString();
	}

	/**
	 * 转义特殊字符
	 * 
	 * @param str
	 *            要转义的字符串
	 * @return
	 */
	private String transferredChar(String str) {
		return str.replace("\"", "\\\"").replace("'", "\\'").replace("\r", "\\r").replace("\n", "\\n").replace("\t",
				"\\t");
	}

	/**
	 * 包装字符串
	 * 
	 * @param str
	 *            要包装的字符串
	 * @return 返回包装后的字符串
	 */
	private String wrap(String str) {
		return warpStr + str.replace(warpStr, warpStr + warpStr) + warpStr;
	}

	/**
	 * 标记创建某个数据表时，是否跳过异常，继续创建接下来的表，true：跳过，false：抛出异常, 默认跳过异常
	 * 
	 * @return
	 */
	public boolean isSkipCreateTableException() {
		return isSkipCreateTableException;
	}

	/**
	 * 标记创建某个数据表时，是否跳过异常，继续创建接下来的表，true：跳过，false：抛出异常, 默认跳过异常
	 * 
	 * @param isSkipCreateTableException
	 */
	public void setSkipCreateTableException(boolean isSkipCreateTableException) {
		this.isSkipCreateTableException = isSkipCreateTableException;
	}

	/**
	 * 关闭资源
	 */
	public void close() {
		try {
			XLPDBUtil.close(connection);
		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("关闭数据库连接失败！");
			}
		}
	}
}
