package org.xlp.db.sql;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.xlp.assertion.AssertUtils;
import org.xlp.db.exception.EntityException;
import org.xlp.db.sql.item.ComplexQueryFieldItem;
import org.xlp.db.sql.item.ComplexQueryFieldItem.ValueType;
import org.xlp.db.sql.item.FieldDescriptors;
import org.xlp.db.sql.item.QueryColumnProperty;
import org.xlp.db.sql.item.QueryColumnProperty.QueryColumnPropertyType;
import org.xlp.db.sql.statisticsfun.DistinctCount;
import org.xlp.db.sql.statisticsfun.SQLStatisticsType;
import org.xlp.db.sql.table.Table;
import org.xlp.db.utils.BeanUtil;
import org.xlp.utils.XLPArrayUtil;
import org.xlp.utils.XLPStringUtil;
import org.xlp.utils.collection.XLPCollectionUtil;

/**
 * <p>创建时间：2022年5月18日 下午12:01:04</p>
 * @author xlp
 * @version 1.0 
 * @Description sql片段形成类
*/
class SQLPartCreator {
	/**
	 * 创建SQL查询字段描述
	 */
	private FieldDescriptors fieldDescriptors = new FieldDescriptors();
	
	/**
	 * 表别名与表对象映射关系
	 */
	private Map<String, Table<?>> tableAliasMap;
	
	/**
	 * 形成条件语句
	 * 
	 * @param complexQuerySQL
	 * @param exeCount 标记是否是执行count语句
	 * @throws NullPointerException 假如参数为null，则抛出该异常
	 * @return
	 */
	public String formatTablePartSql(ComplexQuerySQL complexQuerySQL, boolean exeCount){
		AssertUtils.isNotNull(complexQuerySQL, "complexQuerySQL parameter is null!");
		complexQuerySQL = complexQuerySQL.getTopComplexQuerySQL();
		StringBuilder sb = new StringBuilder();
		sb.append("select");
		if (complexQuerySQL.isDistinct()) {
			sb.append(" distinct");
		}
		tableAliasMap = complexQuerySQL.getTableAliasMap();
		//拼接每个查询字段的SQL片段
		joinQueryFieldSql(sb, complexQuerySQL, exeCount);
		//获取实体对应的表对象
		Table<?> table = complexQuerySQL.getTable();
		sb.append(" from ").append(table.getTableName()).append(" ");
		if (!XLPStringUtil.isEmpty(table.getAlias())) {
			sb.append(table.getAlias()).append(" ");
		}
		//处理字表SQL片段
		List<ComplexQuerySQL> childrenComplexQuerySQL = complexQuerySQL.getChildrenComplexQuerySQL();
		for (ComplexQuerySQL child : childrenComplexQuerySQL) {
			deepParseComplexQuerySQL(child, sb);
		}
		List<ComplexQueryFieldItem> fieldItems = complexQuerySQL.getFieldItems();
		if (!XLPCollectionUtil.isEmpty(fieldItems)) {
			//处理where 条件SQL片段
			sb.append(" where ");
			joinConditionFieldsSql(sb, fieldItems); 
		}
		
		//拼接group by SQL片段
		joinGroupBySql(complexQuerySQL, sb);
		
		fieldItems = complexQuerySQL.getHavingFieldItems();
		if (!XLPCollectionUtil.isEmpty(fieldItems)) {
			//拼接having条件SQL片段
			sb.append(" having ");
			joinConditionFieldsSql(sb, fieldItems);
		}
		
		//拼接order by SQL片段
		joinOrderBySql(complexQuerySQL, sb);
		
		//拼接limit语句
		if (complexQuerySQL.getLimit() != null && !exeCount) {
			sb.append(" limit ?,?");
		}
		
		return sb.toString();
	}
	
	/**
	 * 拼接order by SQL片段
	 * 
	 * @param complexQuerySQL
	 * @param sb
	 */
	private void joinOrderBySql(ComplexQuerySQL complexQuerySQL, StringBuilder sb) {
		Map<String, String> sortFields = complexQuerySQL.getSortFields();
		if (sortFields != null && !sortFields.isEmpty()) {
			sb.append(" order by ");
			boolean start = true;
			for (Entry<String, String> entry : sortFields.entrySet()) {
				if (!start) {
					sb.append(SQL.COMMA).append(" ");
				}
				sb.append(getColumnName(entry.getKey())).append(" ")
					.append(entry.getValue());
				start = false;
			}
		}
	}

	/**
	 * 拼接group by SQL片段
	 * 
	 * @param complexQuerySQL
	 */
	private void joinGroupBySql(ComplexQuerySQL complexQuerySQL, StringBuilder sb) {
		Set<String> groupFields = complexQuerySQL.getGroupFields();
		if (!XLPCollectionUtil.isEmpty(groupFields)) {
			sb.append(" gruop by ");
			boolean first = true;
			for (String groupField : groupFields) { 
				if (!first) {
					sb.append(SQL.COMMA).append(" ");
				}
				first = false;
				sb.append(getColumnName(groupField));
			}
		}
	}

	/**
	 * 拼接每个查询字段的SQL片段
	 * 
	 * @param sb
	 * @param topComplexQuerySQL
	 * @param exeCount 标记是否是执行count语句
	 */
	private void joinQueryFieldSql(StringBuilder sb, ComplexQuerySQL topComplexQuerySQL,
			boolean exeCount){
		List<SQLStatisticsType> sqlStatisticsTypes = topComplexQuerySQL.getSqlStatisticsType();
		//判断是否是统计数据条数
		if (exeCount) {
			SQLStatisticsType count = null; 
			for (SQLStatisticsType sqlStatisticsType : sqlStatisticsTypes) {
				if (sqlStatisticsType instanceof DistinctCount) {
					count = sqlStatisticsType;
					break;
				}
			}
			if (count != null) {
				sb.append(" ");
				formatCountStatisticsPartSql(sb, (DistinctCount) count);
			} else {
				sb.append(" count(*)");
				fieldDescriptors.addAlias("count(*)");
			}
			return;
		}
		
		//获取查询出的字段名称
		List<QueryColumnProperty> queryColumns = topComplexQuerySQL.getQueryColumns();
		if (XLPCollectionUtil.isEmpty(queryColumns) 
				&& XLPCollectionUtil.isEmpty(sqlStatisticsTypes)) {
				//查询字段为空时，查询所有字段值
				sb.append(" *");
				return;
		}
		
		// 拼接统计查询列
		for (SQLStatisticsType sqlStatisticsType : sqlStatisticsTypes) {
			sb.append(" ");
			if (sqlStatisticsType instanceof DistinctCount) {
				formatCountStatisticsPartSql(sb, (DistinctCount) sqlStatisticsType);
			} else {
				formatOtherStatisticsPartSql(sb, sqlStatisticsType);
			}
		}
		
		boolean start = XLPCollectionUtil.isEmpty(sqlStatisticsTypes);
		String alias, fieldName;
		for (QueryColumnProperty queryColumnProperty : queryColumns) {
			if (!start) {
				sb.append(SQL.COMMA);
			}
			start = false;
			//拼接每个查询
			fieldName = queryColumnProperty.getFieldName();
			sb.append(" ");
			if (queryColumnProperty.getQueryColumnPropertyType() == QueryColumnPropertyType.FIELD) {
				fieldName = getColumnName(fieldName);
				sb.append(fieldName);
			} else {
				sb.append(queryColumnProperty.getCustomValue());
			}
			
			alias = queryColumnProperty.getAlias();
			if (!XLPStringUtil.isEmpty(alias)) {
				sb.append(" ").append(alias);
				fieldDescriptors.addAlias(alias); 
			}
		}
	}

	/**
	 * 递归处理sql对象
	 * 
	 * @param complexQuerySQL
	 * @param sb
	 */
	private void deepParseComplexQuerySQL(ComplexQuerySQL complexQuerySQL, StringBuilder sb) {
		//拼接字表连接语句
		sb.append(complexQuerySQL.getJoinType().getDescript()).append(" ");
		//获取实体对应的表对象
		Table<?> table = complexQuerySQL.getTable();
		sb.append(table.getTableName()).append(" ");
		if (!XLPStringUtil.isEmpty(table.getAlias())) {
			sb.append(table.getAlias()).append(" ");
		}
		
		//拼接条件SQL片段
		//.append(" on ")
		List<ComplexQueryFieldItem> fieldItems = complexQuerySQL.getFieldItems();
		if (!XLPCollectionUtil.isEmpty(fieldItems)) {
			sb.append("on ");
			joinConditionFieldsSql(sb, fieldItems);
		}
		
		//处理字表SQL片段
		List<ComplexQuerySQL> childrenComplexQuerySQL = complexQuerySQL.getChildrenComplexQuerySQL();
		for (ComplexQuerySQL child : childrenComplexQuerySQL) {
			deepParseComplexQuerySQL(child, sb);
		}
	}

	/**
	 * 拼接条件SQL片段
	 * 
	 * @param sb
	 * @param fieldItems
	 */
	public void joinConditionFieldsSql(StringBuilder sb, List<ComplexQueryFieldItem> fieldItems) {
		//标记是否是第一个条件
		boolean firstCondition = true;
		//存储左括号
		Stack<String> stack = new Stack<String>();
		for (ComplexQueryFieldItem fieldItem : fieldItems) {
			ValueType valueType = fieldItem.getValueType();
			switch (fieldItem.getOperator()) {
			case LEFT_BRACKET:
				 stack.push(fieldItem.getOperator().getOperator());
				break;
			case RIGHT_BRACKET:
				sb.append(fieldItem.getOperator().getOperator());
				firstCondition = false;
				break;
				
			case EQ: 
			case NEQ:
			case LIKE:
			case NLIKE:
			case GT:
			case GE:
			case LT:
			case LE:
				joinEveryConditionItemSql(firstCondition, sb, fieldItem, 
						stack, valueType == ValueType.VALUE ? valueType : ValueType.FIELD);
				if (valueType == ValueType.VALUE) {
					sb.append(" ?");
				} else {
					sb.append(" ").append(getColumnName(String.valueOf(fieldItem.getValue())));
				}
				firstCondition = false;
				break;

			case IS_NULL:
			case IS_NOT_NULL:
			case BETWEEN:
			case NBETWEEN:
				joinEveryConditionItemSql(firstCondition, sb, fieldItem, stack,
						ValueType.VALUE);
				firstCondition = false;
				break;
				
			case IN:
			case NIN:
				joinEveryConditionItemSql(firstCondition, sb, fieldItem, stack,
						ValueType.VALUE);
				sb.append(SQLUtil.formatInSql(fieldItem.getValues().length)); 
				firstCondition = false;
				break;
			case NOT_EXISTS:
			case EXISTS:
				if (valueType == ValueType.SQL) {
					joinEveryConditionItemSql(firstCondition, sb, fieldItem, stack,
							ValueType.SQL);
					firstCondition = false;
				}
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * 拼接每个条件SQL片段
	 * 
	 * @param firstCondition
	 * @param sb
	 * @param fieldItem
	 * @param stack
	 * @param valueType
	 */
	private void joinEveryConditionItemSql(boolean firstCondition, StringBuilder sb, 
			ComplexQueryFieldItem fieldItem, Stack<String> stack, ValueType valueType){
		if (!firstCondition) {
			sb.append(" ").append(fieldItem.getConnector().getConnector()).append(" ");
		}
		while (!stack.isEmpty()) {
			sb.append(stack.pop());
		}
		switch (valueType) {
		case VALUE:
		case FIELD:
			String columnName = getColumnName(fieldItem.getFieldName());
			sb.append(columnName).append(" ")
				.append(fieldItem.getOperator().getOperator());
			break;
		case SQL:
			sb.append(fieldItem.getOperator().getOperator()).append(SQL.LEFT_BRACKET)
				.append(((SQL)fieldItem.getValue()).getParamSql()).append(SQL.RIGHT_BRACKET);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 拼接SQL count统计函数部分SQL片段
	 * 
	 * @param sb
	 * @param distinctCount
	 */
	private void formatCountStatisticsPartSql(StringBuilder sb, DistinctCount distinctCount){
		String[] distinctFields = distinctCount.getDistinctFields();
		String alias = distinctCount.getAlias();
		String tempAlias = distinctCount.getSQLMenthodName();
		if (!XLPArrayUtil.isEmpty(distinctFields)) {
			StringBuilder tempSb = new StringBuilder();
			tempSb.append("distinct ");
			for (int i = 0, len = distinctFields.length; i < len; i++) { 
				if (i != 0) {
					tempSb.append(SQL.COMMA);
				}
				tempSb.append(getColumnName(distinctFields[i]));
			}
			tempAlias = String.format(tempAlias, tempSb.toString());
		} else if (XLPStringUtil.isEmpty(distinctCount.getFieldName())) {
			tempAlias = String.format(tempAlias, "*");
		} else {
			tempAlias = String.format(tempAlias, 
					getColumnName(distinctCount.getFieldName()));
		}
		sb.append(tempAlias);
		if (!XLPStringUtil.isEmpty(alias)) {
			sb.append(" ").append(alias);
			fieldDescriptors.addAlias(alias);
		} else {
			fieldDescriptors.addAlias(tempAlias);
		}
	}
	
	/**
	 * 拼接SQL其他统计函数部分SQL片段
	 * 
	 * @param sb
	 * @param sqlStatisticsType
	 * @param fieldDescriptors
	 * @param tableAliasMap
	 */
	private void formatOtherStatisticsPartSql(StringBuilder sb, SQLStatisticsType sqlStatisticsType){
		String alias = sqlStatisticsType.getAlias();
		String tempAlias = sqlStatisticsType.getSQLMenthodName();
		tempAlias = String.format(tempAlias, getColumnName(sqlStatisticsType.getFieldName()));
		sb.append(tempAlias);
		if (!XLPStringUtil.isEmpty(alias)) {
			sb.append(" ").append(alias);
			fieldDescriptors.addAlias(alias);
		} else {
			fieldDescriptors.addAlias(tempAlias);
		}
	}
	
	/**
	 * 获取字段名对应的类名
	 * 
	 * @param fieldName
	 * @return
	 * @throws EntityException 
	 * 				假如参数为没有该字段，则抛出该异常
	 */
	private String getColumnName(String fieldName){
		//判断是否是字段别名，是，则直接返回别名
		if (fieldDescriptors.hasAlias(fieldName)) {
			return fieldName;
		}
		//判断是否存在字段名对应的列名, 存在字节返回对应的列名
		if (fieldDescriptors.hasColumnName(fieldName)) {
			return fieldDescriptors.getColumnName(fieldName);
		}
		
		int index = fieldName.indexOf(".");
		//判断字段名是否带了实体别名前缀
		if (index >= 1) {
			String tableAlias = fieldName.substring(0, index);
			Table<?> table = tableAliasMap.get(tableAlias);
			if (table == null) {
				throw new EntityException("不存在[" + fieldName + "]字段");
			}
			//获取列名
			String columnName = BeanUtil.getFieldAlias(table, fieldName.substring(index + 1));
			columnName = tableAlias + "." + columnName;
			fieldDescriptors.putFieldColumn(fieldName, columnName);
			return columnName;
		} else {
			String columnName = null;
			for(Table<?> table : tableAliasMap.values()){
				columnName = table.getBeanFieldNameMapperDbColumnNameMap().get(fieldName);
				if (!XLPStringUtil.isEmpty(columnName)) break;
			}
			if (XLPStringUtil.isEmpty(columnName)) {
				throw new EntityException("不存在[" + fieldName + "]字段");
			}
			fieldDescriptors.putFieldColumn(fieldName, columnName);
			return columnName;
		}
	}
}
